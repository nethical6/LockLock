@file:OptIn(ExperimentalMaterial3Api::class)
package nethical.locklock.screens.onboard
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import nethical.locklock.DeviceAdmin
import nethical.locklock.screens.components.PermissionCard
import nethical.locklock.services.AppLockerService
import nethical.locklock.utils.isAccessibilityServiceEnabled
import nethical.locklock.utils.isDeviceAdminEnabled
import nethical.locklock.utils.openAccessibilityServiceScreen
import nethical.locklock.utils.openDeviceAdmin
import kotlin.jvm.java

@Composable
fun PermissionRequestScreen(
    isNextEnabled: MutableState<Boolean>
) {
    val context = LocalContext.current
    var accessibilityGranted by remember { mutableStateOf(false) }
    var deviceAdminGranted by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        isNextEnabled.value = false
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityGranted = isAccessibilityServiceEnabled(
                    context,
                    AppLockerService::class.java
                )
                deviceAdminGranted = isDeviceAdminEnabled(
                    context,
                    DeviceAdmin::class.java
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(accessibilityGranted&&deviceAdminGranted) {
        isNextEnabled.value = (accessibilityGranted&&deviceAdminGranted)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "To provide the best experience, this app needs the following permissions:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Accessibility Permission Card
        PermissionCard(
            icon = Icons.Default.Build,
            title = "Accessibility Service",
            description = "Allows the app to interact with other apps and system UI to provide enhanced automation and assistance features. This helps us deliver seamless user interactions and improve app functionality.",
            isGranted = accessibilityGranted,
            onClick = {
                openAccessibilityServiceScreen(context, AppLockerService::class.java)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device Admin Permission Card
        PermissionCard(
            icon = Icons.Default.Person,
            title = "Device Administrator",
            description = "Enables advanced device management capabilities including screen lock, device wipe for security, and policy enforcement. This ensures your device remains secure and compliant with organizational requirements.",
            isGranted = deviceAdminGranted,
            onClick = {
                openDeviceAdmin(context)
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
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your privacy is important to us. These permissions are used solely for app functionality and your data remains secure on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
