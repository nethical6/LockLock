package nethical.locklock.utils

import android.R.attr.name
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
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
suspend fun getBackgroundSystemApps(context: Context): List<ApplicationInfo> {
    return withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchableApps = pm.queryIntentActivities(launcherIntent, 0)
            .map { it.activityInfo.applicationInfo.packageName }
            .toSet()

        allApps.filter { app ->
            (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 && // system app
                    app.packageName !in launchableApps                  // not launchable
        }
    }
}

fun getKeyboards(context: Context): List<String> {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val enabledMethods = imm.enabledInputMethodList
    return enabledMethods.map { it.packageName }
}