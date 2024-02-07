package com.mytiki.publish.client.auth

import android.util.Log
import com.mytiki.publish.client.email.EmailProviderEnum
import org.json.JSONObject
import java.util.Date

class AuthToken (val username: String, val auth: String, val refresh: String, val expiration: Date, val provider: EmailProviderEnum){
    companion object{
        fun fromString(data: String?, key: String): AuthToken{
            val json = JSONObject(data)
            return AuthToken(
                key,
                json.getString("auth"),
                json.getString("refresh"),
                Date(json.getLong("expiration")),
                EmailProviderEnum.fromString(json.getString("provider"))
            )
        }
    }
    override fun toString(): String {
        return JSONObject()
            .put("auth", auth)
            .put("refresh", refresh)
            .put("expiration", expiration.time)
            .put("provider" , provider.toString())
            .toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthToken

        if (username != other.username) return false
        if (auth != other.auth) return false
        if (refresh != other.refresh) return false
        if (expiration != other.expiration) return false
        return provider == other.provider
    }
}