/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in the root directory.
 */

package com.mytiki.publish.client.ui.merchant.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mytiki.publish.client.TikiClient
import com.mytiki.publish.client.clo.merchant.MerchantEnum
import com.mytiki.publish.client.ui.account.Account
import com.mytiki.publish.client.ui.account.ui.AccountDisplay
import com.mytiki.publish.client.ui.components.Header
import com.mytiki.publish.client.ui.components.MainButton
import com.mytiki.publish.client.ui.merchant.MerchantViewModel

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun MerchantView(
    merchantViewModel: MerchantViewModel,
    activity: AppCompatActivity,
    merchant: MerchantEnum,
    onBackButton: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()

        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Spacer(modifier = Modifier.height(64.dp))
                    Header(text = merchant.displayName()) {
                        onBackButton()
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                AccountDisplay(
                    merchant,
                    239.dp,
                    "3% cashback on all purchases",
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                MainButton(
                    modifier = Modifier.padding(horizontal = 21.dp),
                    text = "Scan receipt",
                    isfFilled = false
                ) {
                    TikiClient.capture.camera(activity)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "More Offers",
                    modifier = Modifier
                        .padding(horizontal = 21.dp)
                        .height(36.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            items(merchantViewModel.offers(merchant)) {
                OfferCard(it, merchant) {
                    merchantViewModel.webView(it.clickUrl, activity)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}