/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in the root directory.
 */

package com.mytiki.publish.client.ui.license.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mytiki.publish.client.ui.components.BottomSheetHeader
import com.mytiki.publish.client.ui.components.DisplayCard
import com.mytiki.publish.client.ui.components.MainButton
import com.mytiki.publish.client.ui.components.bottomSheet.HideableBottomSheetValue
import com.mytiki.publish.client.ui.components.bottomSheet.rememberHideableBottomSheetState
import com.mytiki.publish.client.ui.components.bottomSheet.ui.HideableBottomSheetScaffold
import com.mytiki.publish.client.ui.license.LicenseViewModel

@Composable
fun LicenseView(
    activity: AppCompatActivity,
    licenseViewModel: LicenseViewModel,
    onGetEstimate: () -> Unit,
) {

    val bottomSheetState = rememberHideableBottomSheetState(initialValue = HideableBottomSheetValue.HalfExpanded){
        activity.finish()
    }
    LaunchedEffect(Unit){
        licenseViewModel.updateIsLicensed()
    }

    HideableBottomSheetScaffold(
        bottomSheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize(),
        bottomSheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                BottomSheetHeader("CASHBACK CONNECTIONS", "Share data. Earn cash.")
                Spacer(modifier = Modifier.height(56.dp))
                DisplayCard(height = 201.dp, horizontalPadding = 15.dp, verticalPadding = 0.dp) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Earn monthly",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${licenseViewModel.estimate().min} - \$${licenseViewModel.estimate().max}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "for your shopping habits",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 30.dp),
                    text = "Estimate based on similar users spending habits and market price for shopping data. ",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                MainButton(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = "Get estimate", isfFilled = true
                ) {
                    onGetEstimate()
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    ) {}
}
