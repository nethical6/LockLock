@file:OptIn(ExperimentalMaterial3Api::class)
package nethical.locklock

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Log.i
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nethical.locklock.data.AppInfo
import nethical.locklock.ui.theme.LockLockTheme
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockLockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        AppSelectionScreen()
                }
            }
        }
    }
}

@Composable
fun AppSelectionScreen() {
    val context = LocalContext.current
    var appList by remember { mutableStateOf(emptyList<AppInfo>()) }
    val selectedApps = remember { mutableStateListOf<String>() }

    val selectedAppsSp = context.getSharedPreferences("selected_apps", Context.MODE_PRIVATE)

    // just couldn't get launched effect to work with mutablestatelist
    var isSaved by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val selectedAppsSet = selectedAppsSp.getStringSet("selected_apps",emptySet<String>())
        Log.d("AppSelection", "loaded $selectedAppsSet")
        selectedAppsSet?.forEach { selectedApps.add(it) }
        val result = reloadApps(context.packageManager)
        if(result.isSuccess){
            appList = result.getOrDefault(emptyList())
        }

    }

    LaunchedEffect(isSaved) {
        if (isSaved == false) {
            selectedAppsSp.edit(commit = true) {
                putStringSet(
                    "selected_apps",
                    selectedApps.toSet()
                )
            }
            Log.d("AppSelection", "Saved ${selectedApps.toSet()}")
            isSaved = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                )
            )

            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Select Apps",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Apps List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(appList) { app ->
                AppSelectionItem(
                    app = app,
                    onToggle = { packageName ->
                        if (selectedApps.contains(packageName)) {
                            selectedApps.remove(packageName)
                        } else {
                            selectedApps.add(packageName)
                        }
                        isSaved = false

                    },
                    selectedApps = selectedApps
                )
            }
        }
    }
}

    @Composable
    fun AppSelectionItem(
        app: AppInfo,
        selectedApps: SnapshotStateList<String>,
        onToggle: (String) -> Unit
    ) {
        val colorScheme = MaterialTheme.colorScheme

        val isSelected = selectedApps.contains(app.packageName)
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.02f else 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
            label = "scale"
        )

        val containerColor by animateColorAsState(
            targetValue = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.2f),
            animationSpec = tween(300),
            label = "containerColor"
        )

        val contentColor by animateColorAsState(
            targetValue = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface.copy(alpha = 0.87f),
            animationSpec = tween(300),
            label = "contentColor"
        )

        Card(
            onClick = { onToggle(app.packageName) },
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val iconBackgroundColor by animateColorAsState(
                        targetValue = if (isSelected) colorScheme.primary.copy(alpha = 0.2f)
                        else colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        animationSpec = tween(300),
                        label = "iconBackground"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = iconBackgroundColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = app.name,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                }

                AnimatedSwitch(
                    checked = isSelected,
                    onCheckedChange = { onToggle(app.packageName) }
                )
            }
        }
    }


    @Composable
    fun AnimatedSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val colorScheme = MaterialTheme.colorScheme

        val trackColor by animateColorAsState(
            targetValue = if (checked) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f),
            animationSpec = tween(300),
            label = "trackColor"
        )

        val thumbOffset by animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 900f),
            label = "thumbOffset"
        )

        Box(
            modifier = Modifier
                .width(52.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(trackColor)
                .clickable { onCheckedChange(!checked) }
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = 20.dp * thumbOffset)
                    .background(
                        color = colorScheme.background,
                        shape = CircleShape
                    )
            )
        }
    }

@Preview(showBackground = true)
@Composable
fun AppSelectionScreenPreview() {
    MaterialTheme {
        AppSelectionScreen()
    }
}
}