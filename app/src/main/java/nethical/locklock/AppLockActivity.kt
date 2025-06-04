package nethical.locklock

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import nethical.locklock.screens.LockScreen
import nethical.locklock.services.INTENT_ACTION_APP_UNLOCKED
import nethical.locklock.ui.theme.LockLockTheme


class AppLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isChangePin = intent.getBooleanExtra("is_change_pin",false)
        val lockedPackage = intent.getStringExtra("locked_package") ?: "Unknown App"

        setContent {
            LockLockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LockScreen(lockedPackage,isChangePin, onUnlocked = {
                        // Unlock logic
                        val intent = Intent(INTENT_ACTION_APP_UNLOCKED).apply {
                            putExtra("packageName", lockedPackage)
                        }
                        sendBroadcast(intent)
                        Toast.makeText(this, "$lockedPackage Unlocked", Toast.LENGTH_SHORT).show()
                        finishAffinity() // Closes this activity and any parent activities in the task
                    },
                        onPinChanged = {
                            finish()
                            Toast.makeText(this, "Passcode Change Success", Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }
    }
}
