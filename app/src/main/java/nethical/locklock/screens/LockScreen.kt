@file:OptIn(ExperimentalMaterial3Api::class)
package nethical.locklock.screens

import android.R.attr.text
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nethical.locklock.R
import nethical.locklock.data.AppInfo
import nethical.locklock.utils.loadAppInfo
import androidx.core.content.edit

enum class ChangePinStep {
                         INCORRECT,
    WRITE_INITIALLY,
    REWRITE,
    REWRITE_DONE,
}
@Composable
fun LockScreen(
    packageName: String,
    isChangePin: Boolean,
    onUnlocked: () -> Unit,
    onPinChanged: ()->Unit,
) {
    val context = LocalContext.current
    var passcode = remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    val currentPinSp = context.getSharedPreferences("pin",MODE_PRIVATE)
    val currentPin = currentPinSp.getString("pin","0000")

    var pinChangeStep by remember { mutableStateOf<ChangePinStep>(ChangePinStep.WRITE_INITIALLY) }
    var lastEnteredPin by remember { mutableStateOf("") }

    var appInfo by remember { mutableStateOf<AppInfo?>(null) }
    // Auto-focus effect
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        isVisible = true
        if(!isChangePin){
            appInfo = loadAppInfo(context,packageName)
        }
    }

    // Handle passcode submission
    fun handleSubmit() {
        Log.d("last", "run")

        if (isChangePin) {
            when (pinChangeStep) {
                ChangePinStep.WRITE_INITIALLY -> {
                    Log.d("last", "init")

                    // First time entering new PIN
                    lastEnteredPin = passcode.value
                    passcode.value = ""
                    Log.d("lastPin", lastEnteredPin)

                    pinChangeStep = ChangePinStep.REWRITE
                }

                ChangePinStep.REWRITE -> {
                    Log.d("last", "rewrite")

                    Log.d("lastPin", "$lastEnteredPin == ${passcode.value}")

                    // Confirming the new PIN
                    if (lastEnteredPin == passcode.value) {
                        // PINs match - save and complete
                        currentPinSp.edit(commit = true) {
                            putString("pin", passcode.value)
                        }
                        passcode.value = ""
                        pinChangeStep = ChangePinStep.REWRITE_DONE
                        onPinChanged()
                    } else {
                        // PINs don't match - show error and restart
                        passcode.value = ""
                        pinChangeStep = ChangePinStep.INCORRECT
                    }
                }

                ChangePinStep.INCORRECT -> {
                    Log.d("last", "inc")
                    // User acknowledged error, restart PIN setup
                    passcode.value = ""
                    pinChangeStep = ChangePinStep.WRITE_INITIALLY
                }

                ChangePinStep.REWRITE_DONE -> {
                    Log.d("last", "done")
                    onPinChanged()
                    return
                }
            }
            return
        }

        // Simulate authentication
        if (passcode.value == currentPin) {
            // Success - would navigate to main app
            passcode.value = ""
            onUnlocked()
        } else {
            passcode.value = ""
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Lock Icon

            Box(
                contentAlignment = Alignment.Center
            ) {
                if (appInfo != null) {
                    Image(
                        bitmap = appInfo!!.icon,
                        modifier = Modifier
                            .size(60.dp),
                        contentDescription = appInfo!!.name
                    )
                }

                // Lock badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(if (isChangePin) 60.dp else 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = if (isChangePin) {
                        when (pinChangeStep) {
                            ChangePinStep.WRITE_INITIALLY -> "Setup New Passcode"
                            ChangePinStep.REWRITE -> "Confirm New Passcode"
                            ChangePinStep.INCORRECT -> "Passcodes Don't Match"
                            ChangePinStep.REWRITE_DONE -> "Passcode Updated!"
                        }
                    } else "Enter Passcode",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Passcode Input

            OutlinedTextField(
                value = passcode.value,
                onValueChange = { if (it.length <= 6) passcode.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                label = { Text("Passcode") },
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            painter = painterResource(if (isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24),
                            contentDescription = if (isPasswordVisible)
                                "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleSubmit() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { handleSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Unlock",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric option
            TextButton(
                onClick = { /* Handle biometric */ },
            ) {
                Icon(
                    imageVector = Icons.Default.Lock, // You can replace with fingerprint icon
                    contentDescription = "Biometric",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Use Biometric",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
