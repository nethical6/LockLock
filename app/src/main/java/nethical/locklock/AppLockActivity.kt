package nethical.locklock

import android.content.Intent
import android.os.Bundle
import android.provider.LiveFolders.INTENT
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nethical.locklock.services.AppLockerService
import nethical.locklock.services.INTENT_ACTION_APP_UNLOCKED
import nethical.locklock.ui.theme.LockLockTheme

class AppLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lockedPackage = intent.getStringExtra("locked_package") ?: ""

        setContent {
            LockLockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LockScreen(packageNameToUnlock = lockedPackage) {
                        // Unlock logic
                        val intent = Intent(INTENT_ACTION_APP_UNLOCKED).apply {
                            putExtra("packageName", lockedPackage)
                        }
                        sendBroadcast(intent)

                        finishAffinity()
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreen(packageNameToUnlock: String, onUnlock: () -> Unit) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter password to unlock", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (input == "123") {
                    onUnlock()
                } else {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }
    }
}
