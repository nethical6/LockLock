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
import nethical.locklock.ui.theme.LockLockTheme
import nethical.locklock.screens.AppSelectionScreen
import nethical.locklock.screens.onboard.OnBoardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var isOnboardDone = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                val setupSp = context.getSharedPreferences("isSetupDone", MODE_PRIVATE)
                isOnboardDone.value = setupSp.contains("isSetupDone")
            }
            LockLockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
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
