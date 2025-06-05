package nethical.locklock.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import nethical.locklock.AppLockActivity
import nethical.locklock.screens.components.AnimatedSwitch
import androidx.core.content.edit
import nethical.locklock.services.AppLockerInfo

@Composable
fun SettingsDialog(
    isVisible: MutableState<Boolean>,
) {
    val context = LocalContext.current
    fun onDismiss() {
        isVisible.value = false
    }
    var enableAntiUninstall by remember { mutableStateOf(false) }
    val additionalInfoSp = context.getSharedPreferences("additional_info",Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        val additionalInfoSp = context.getSharedPreferences("additional_info",Context.MODE_PRIVATE)
        enableAntiUninstall = additionalInfoSp.getBoolean("is_anti_uninstall",false)
    }

    LaunchedEffect(enableAntiUninstall) {
        additionalInfoSp.edit(commit = true) {
            putBoolean(
                "is_anti_uninstall",
                enableAntiUninstall
            )
        }
        AppLockerInfo.isAntiUninstallOn = false
    }

    if (isVisible.value) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Settings Content
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Security Section
                        item {
                            SettingSectionHeader(title = "Security")
                        }

                        item {
                            SettingCheckBox(
                                title = "Anti Uninstall",
                                subtitle = "Get notified about app locks and breaks",
                                icon = Icons.Default.Lock,
                                checked = enableAntiUninstall,
                                onCheckedChange = { enableAntiUninstall = it }
                            )
                        }
                        item {
                            AnimatedActionButton(
                                title = "Change Passcode",
                                icon = Icons.Default.Lock,
                                onClick = {
                                    val intent = Intent(context, AppLockActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                                        putExtra("is_change_pin", true)
                                    }
                                    context.startActivity(intent)
                                },
                                enabled = true
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Save settings logic here
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun SettingCheckBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (checked) colorScheme.primaryContainer.copy(alpha = 0.3f)
        else colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (checked) colorScheme.onPrimaryContainer
        else colorScheme.onSurface.copy(alpha = 0.87f),
        animationSpec = tween(300),
        label = "contentColor"
    )

    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val iconBackgroundColor by animateColorAsState(
                    targetValue = if (checked) colorScheme.primary.copy(alpha = 0.2f)
                    else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    animationSpec = tween(300),
                    label = "iconBackground"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconBackgroundColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp),
                        tint = if (checked) colorScheme.primary else contentColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Using your custom animated switch
            AnimatedSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}


@Composable
fun AnimatedActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 1000f),
        label = "scale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
            isPressed -> colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> colorScheme.primaryContainer.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurface.copy(alpha = 0.4f)
            isPressed -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurface.copy(alpha = 0.87f)
        },
        animationSpec = tween(200),
        label = "contentColor"
    )

    Card(
        onClick = {
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (enabled) {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                                onClick()

                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val iconBackgroundColor by animateColorAsState(
                    targetValue = when {
                        !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        isPressed -> colorScheme.primary.copy(alpha = 0.3f)
                        else -> colorScheme.primary.copy(alpha = 0.2f)
                    },
                    animationSpec = tween(200),
                    label = "iconBackground"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconBackgroundColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            !enabled -> contentColor.copy(alpha = 0.5f)
                            else -> colorScheme.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }
    }
}
