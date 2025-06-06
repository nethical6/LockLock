@file:OptIn(ExperimentalMaterial3Api::class)
package nethical.locklock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import nethical.locklock.screens.AppSelectionScreen
import nethical.locklock.screens.DonationDialog
import nethical.locklock.screens.onboard.OnBoardScreen
import nethical.locklock.services.AppLockerService
import nethical.locklock.ui.theme.LockLockTheme
import nethical.locklock.utils.isAccessibilityServiceEnabled

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var isOnboardDone = remember { mutableStateOf(false) }
            var shouldShowDonationDialog = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                val setupSp = context.getSharedPreferences("isSetupDone", MODE_PRIVATE)
                isOnboardDone.value = setupSp.contains("isSetupDone")


                val isAccessibilityEnabled = isAccessibilityServiceEnabled(context,
                    AppLockerService::class.java)
                if(isOnboardDone.value) isOnboardDone.value = isAccessibilityEnabled

                val userInfo = context.getSharedPreferences("user",MODE_PRIVATE)
                if(userInfo.contains("installed_on") && !userInfo.contains("do_not_show_donation_dialog")){
                    val startedUsing = userInfo.getLong("installed_on",System.currentTimeMillis())
                    val currentTime = System.currentTimeMillis()
                    val fiveDaysInMillis = 5 * 24 * 60 * 60 * 1000L // 5 days in milliseconds
                    shouldShowDonationDialog.value = (currentTime - startedUsing) >= fiveDaysInMillis
                } else {
                    userInfo.edit(commit = true) {
                        putLong(
                            "installed_on",
                            System.currentTimeMillis()
                        )
                    }
                }


            }
            LockLockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if(shouldShowDonationDialog.value){
                            DonationDialog {
                                shouldShowDonationDialog.value = false
                                val userInfo = context.getSharedPreferences("user",MODE_PRIVATE)
                                userInfo.edit(commit = true) {
                                    putBoolean(
                                        "do_not_show_donation_dialog",
                                        true
                                    )
                                }
                            }
                        }

                        if (isOnboardDone.value) {
                            AppSelectionScreen()
                        }else{
                            OnBoardScreen(isOnboardDone)
                        }
                    }
                }
            }
        }
    }
}
