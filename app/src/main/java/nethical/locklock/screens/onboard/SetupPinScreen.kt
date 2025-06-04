package nethical.locklock.screens.onboard

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import nethical.locklock.AppLockActivity
import nethical.locklock.DeviceAdmin
import nethical.locklock.screens.components.PermissionCard
import nethical.locklock.services.AppLockerService
import nethical.locklock.utils.isAccessibilityServiceEnabled
import nethical.locklock.utils.isDeviceAdminEnabled
import androidx.core.content.edit


@Composable
fun PinSetupScreen(
    isNextEnabled: MutableState<Boolean>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPinSetup by remember { mutableStateOf(false) }
    var isRecoveryQuestionDone by remember { mutableStateOf(false) }

    var showSecurityQuestionDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPinSp = context.getSharedPreferences("pin", MODE_PRIVATE)
                isPinSetup = currentPinSp.contains("pin")
                isRecoveryQuestionDone = currentPinSp.contains("recovery_question")
                isNextEnabled.value = isPinSetup && isRecoveryQuestionDone
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(isPinSetup,isRecoveryQuestionDone) {
        isNextEnabled.value = isPinSetup && isRecoveryQuestionDone
    }
    if(showSecurityQuestionDialog){
        SecurityQuestionDialog(
            onDismiss = { showSecurityQuestionDialog = false },
            onConfirm = { q,a ->
                val currentPinSp = context.getSharedPreferences("pin", MODE_PRIVATE)
                currentPinSp.edit(commit = true) {
                    putString("recovery_question", q)
                    putString("recovery_answer", a)
                }
                isRecoveryQuestionDone = true
                showSecurityQuestionDialog = false
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header Icon
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Press the below button setup a passcode",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionCard(
            icon = Icons.Default.Build,
            title = "Setup Passcode",
            description = "Make sure not to share it with anyone!",
            isGranted = isPinSetup,
            onClick = {
                val intent = Intent(context, AppLockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    putExtra("is_change_pin", true)
                }
                context.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PermissionCard(
            icon = Icons.Default.Build,
            title = "Setup Recovery Question",
            description = "A question that you answer in cases wherein you forget your password",
            isGranted = isRecoveryQuestionDone,
            onClick = {
                showSecurityQuestionDialog = true
            }
        )


        Spacer(modifier = Modifier.height(32.dp))

        // Privacy Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Notice",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please note down the answer to your recovery question somewhere safe. You won't be allowed to change the passcode with out it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
