@file:OptIn(ExperimentalMaterial3Api::class)
package nethical.locklock.screens

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    val currentPinSp = context.getSharedPreferences("pin", MODE_PRIVATE)
    val currentPin = currentPinSp.getString("pin", "0000")
    val cooldownSp = context.getSharedPreferences("cooldown", MODE_PRIVATE)

    var pinChangeStep by remember { mutableStateOf<ChangePinStep>(ChangePinStep.WRITE_INITIALLY) }
    var lastEnteredPin by remember { mutableStateOf("") }

    var appInfo by remember { mutableStateOf<AppInfo?>(null) }

    var coolDownEndsOn by remember { mutableLongStateOf(0L) }
    var attempts by remember { mutableLongStateOf(0L) }
    var cooldownRemaining by remember { mutableStateOf(0L) }

    var isSecurityQuestionDialogVisible by remember { mutableStateOf(false) }
    // Auto-focus effect
    LaunchedEffect(Unit) {
        isVisible = true
        if (!isChangePin) {
            appInfo = loadAppInfo(context, packageName)
        }
        if (cooldownSp.contains("cooldown")) {
            val cooldown = cooldownSp.getLong("cooldown", 0L)
            if (cooldown < System.currentTimeMillis()) {
                cooldownSp.edit(commit = true) {
                    remove("cooldown")
                }
            } else {
                coolDownEndsOn = cooldown
                attempts = 5
            }
            return@LaunchedEffect
        }
        delay(300)
        focusRequester.requestFocus()

    }

    LaunchedEffect(attempts) {
        if(attempts>=5){
            while (coolDownEndsOn > System.currentTimeMillis()) {
                cooldownRemaining = coolDownEndsOn - System.currentTimeMillis()
                delay(1000)
            }

            // Cooldown is over
            cooldownSp.edit(commit = true) {
                remove("cooldown") }
            coolDownEndsOn = 0L
            attempts = 0
            cooldownRemaining = 0L
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
            cooldownSp.edit(commit = true) {
                remove("cooldownLevel")
            }
        } else {
            attempts++
            if(attempts>=5){
                val cooldownLevel = cooldownSp.getInt("cooldownLevel",1)
                coolDownEndsOn = System.currentTimeMillis() + ((cooldownLevel*2) * 60 * 1000)
                cooldownSp.edit(commit = true) {
                    putLong("cooldown", coolDownEndsOn)
                    putInt("cooldownLevel",cooldownLevel+1)
                }

            }
            passcode.value = ""
        }
    }
    
    if(isSecurityQuestionDialogVisible){
        AnswerSecurityQuestionDialog(
            question = currentPinSp.getString("recovery_question","The default pin is 0000").toString(),
            onDismiss = {
                isSecurityQuestionDialogVisible = false
            },
            onConfirm = { answer ->
                val correctA = currentPinSp.getString("recovery_answer", "")

                val now = System.currentTimeMillis()
                val failedAttempts = currentPinSp.getInt("sq_failed_attempts", 0)

                if ( answer.trim() == correctA) {
                    // Success
                    currentPinSp.edit { remove("sq_failed_attempts").remove("sq_cooldown_end") }
                    Toast.makeText(context, "Your pin is $currentPin", Toast.LENGTH_SHORT).show()
                } else {
                    val newFails = failedAttempts + 1
                    val cooldown = if (newFails >= 3) newFails * 30 * 1000L else 0L // 30s × attempts

                    currentPinSp.edit(commit = true) {
                        putInt("sq_failed_attempts", newFails)
                        if (cooldown > 0) {
                            putLong("sq_cooldown_end", now + cooldown)
                        }
                    }

                    Toast.makeText(
                        context,
                        if (cooldown > 0) "Too many attempts. Try again in ${cooldown / 1000}s"
                        else "Incorrect answer. Attempt $newFails",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isSecurityQuestionDialogVisible = false

            }
        )
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

            if (attempts >= 5) {

                val seconds = (cooldownRemaining / 1000) % 60
                val minutes = (cooldownRemaining / 1000) / 60
                Text(
                    text = "Try again in %02d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {

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

                if(pinChangeStep != ChangePinStep.INCORRECT) {
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
                }
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
                        text = if (isChangePin) {
                            if(pinChangeStep == ChangePinStep.INCORRECT) "Try Again" else "Set Pin"
                        } else
                            "Unlock",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }


                if(!isChangePin){
                    Spacer(modifier = Modifier.height(32.dp))
                    TextButton(
                        onClick = { isSecurityQuestionDialogVisible = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Forgot Passcode",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Forgot Passcode",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
            }
        }
    }
}
