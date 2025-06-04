package nethical.locklock.utils

import android.R.attr.name
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nethical.locklock.data.AppInfo

suspend fun reloadApps(
    packageManager: PackageManager,
): Result<List<AppInfo>> {
    return withContext(Dispatchers.IO) {
        try {
            // Fetch the latest app list from the PackageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val apps = packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
                .mapNotNull { resolveInfo ->
                    resolveInfo.activityInfo?.applicationInfo?.let { appInfo ->
                        AppInfo(
                            name = appInfo.loadLabel(packageManager).toString(),
                            packageName = appInfo.packageName,
                            icon = packageManager.getApplicationIcon(appInfo.packageName).toBitmap(64, 64).asImageBitmap(),
                        )
                    }
                }

            Result.success(apps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun loadAppInfo(context: Context, packageName: String): AppInfo {
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
    return AppInfo(
        icon = packageManager.getApplicationIcon(applicationInfo).toBitmap(64, 64).asImageBitmap(),
        name = packageManager.getApplicationLabel(applicationInfo).toString(),
        packageName = packageName
    )
}