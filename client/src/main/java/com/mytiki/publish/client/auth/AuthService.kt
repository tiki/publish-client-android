package com.mytiki.publish.client.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.mytiki.publish.client.TikiClient
import com.mytiki.publish.client.utils.apiService.ApiService
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.json.JSONObject
import java.security.*

/**
 * Service class for authentication with TIKI.
 */
class AuthService {

    /**
     * Provides a token for the address.
     * @return A Deferred object that will resolve to the token string.
     */
    fun addressToken(): Deferred<String> = CoroutineScope(Dispatchers.IO).async {
        val keyPair = getKey() ?: throw Exception("error on getting key")
        val address = address(keyPair) ?: throw Exception("error on getting address")
        val signature = Base64.encodeToString(
            signMessage(address, keyPair.private)?: throw Exception("error on signing message"),
            Base64.DEFAULT or Base64.NO_PADDING or Base64.NO_WRAP
        )
        Log.d("*******signature*******", signature)
        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id","addr:${TikiClient.config.providerId}:$address")
            .add("client_secret", signature)
            .add("scope", "trail publish")
            .add("expires", "600")
            .build()
        val response = ApiService.post(
            header =  mapOf(
                "Accept" to "application/json",
                "Content-Type" to "application/x-www-form-urlencoded"
            ),
            endPoint = "https://account.mytiki.com/api/latest/auth/token",
            onError = Exception("error on getting token"),
            body,
        ).await()
        AuthTokenRsp.fromJson(JSONObject(response?.string()!!)).access_token
    }

    /**
     * Gets the RSA key pair.
     * @return The RSA key pair or null if an error occurred.
     */
    fun getKey(): KeyPair? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val entry = keyStore.getEntry("TikiKeyPair", null) as? KeyStore.PrivateKeyEntry

            entry?.let {
                KeyPair(keyStore.getCertificate("TikiKeyPair").publicKey, it.privateKey)
            } ?: generateKeyPair()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates the address.
     * @param keyPair The RSA key pair.
     * @return The address as a string or null if an error occurred.
     */
    fun address(keyPair: KeyPair): String? {
        try {
            val publicKeyBytes = keyPair.public.encoded

            val digest = SHA3.Digest256()
            val addressBytes = digest.digest(publicKeyBytes)

            return Base64.encodeToString(addressBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Signs the message.
     * @param message The message to sign.
     * @param privateKey The private key to use for signing.
     * @return The signature as a byte array or null if an error occurred.
     */
    fun signMessage(message: String, privateKey: PrivateKey): ByteArray? {
        val signature = Signature
            .getInstance("SHA256withRSA")
            .apply {
                initSign(privateKey)
                update(message.toByteArray())
            }
        return signature.sign()
    }

    /**
     * Registers the address.
     * @return A CompletableDeferred object that will resolve to the registration response.
     */
    fun registerAddress(): CompletableDeferred<AuthAddrRsp> {

        val registerAddress = CompletableDeferred<AuthAddrRsp>()
        try {
            MainScope().async {
                val keyPair = getKey()
                val address = keyPair?.let { address(it) }
                val signature = address?.let { address ->
                    signMessage("${TikiClient.userID}.$address", keyPair.private)?.let {
                        Base64.encodeToString(it, Base64.DEFAULT or Base64.NO_PADDING or Base64.NO_WRAP)
                    }
                }
                val pubKey = keyPair?.let {
                    Base64.encodeToString(it.public.encoded, Base64.DEFAULT or Base64.NO_PADDING or Base64.NO_WRAP)
                }

                if (!address.isNullOrEmpty() && !pubKey.isNullOrEmpty() && !signature.isNullOrEmpty()) {
                    val jsonBody = JSONObject()
                        .put("id", TikiClient.userID)
                        .put("address", address)
                        .put("pubKey", pubKey)
                        .put("signature", signature)
                        .toString()
                        .toRequestBody("application/json".toMediaTypeOrNull())

                    val token = providerToken().await()

                    val response = ApiService.post(
                        mapOf(
                            "Authorization" to "Bearer $token",
                            "accept" to "application/json"
                        ),
                        "https://account.mytiki.com/api/latest/provider/${TikiClient.config.providerId}/user",
                        Exception("error on registerAddress"),
                        jsonBody
                    ).await()
                    registerAddress.complete(AuthAddrRsp.fromJson(JSONObject(response?.string()!!)))
                } else registerAddress.completeExceptionally(Exception("error on registerAddress"))
            }
        } catch (e: Exception) {
            registerAddress.completeExceptionally(Exception("error on registerAddress"))
        }
        return registerAddress
    }

    /**
     * Provides a token for the provider.
     * @return A Deferred object that will resolve to the token string.
     */
    private fun providerToken(): Deferred<String> = CoroutineScope(Dispatchers.IO).async {
        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id","provider:${TikiClient.config.providerId}")
            .add("client_secret", TikiClient.config.publicKey)
            .add("scope", "account:provider")
            .add("expires", "600")
            .build()
        val response = ApiService.post(
            header =  mapOf(
                "Accept" to "application/json",
                "Content-Type" to "application/x-www-form-urlencoded"
            ),
            endPoint = "https://account.mytiki.com/api/latest/auth/token",
            onError = Exception("error on getting token"),
            body
        ).await()
        AuthTokenRsp.fromJson(JSONObject(response?.string()!!)).access_token
    }

    /**
     * Generates a new RSA key pair.
     * @return The newly generated RSA key pair.
     */
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )

        val builder = KeyGenParameterSpec.Builder(
            "TikiKeyPair",
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)

        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

}
