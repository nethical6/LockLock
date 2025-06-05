package nethical.locklock.utils

import android.accessibilityservice.AccessibilityService
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import nethical.locklock.DeviceAdmin

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val expectedComponentName = ComponentName(context, serviceClass)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.split(':').any {
        ComponentName.unflattenFromString(it)?.equals(expectedComponentName) == true
    }
}
fun isDeviceAdminEnabled(context: Context, adminReceiver: Class<out DeviceAdminReceiver>): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val component = ComponentName(context, adminReceiver)
    return dpm.isAdminActive(component)
}

fun openAccessibilityServiceScreen(context: Context,cls: Class<*>) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val componentName = ComponentName(context, cls)
        intent.putExtra(":settings:fragment_args_key", componentName.flattenToString())
        val bundle = Bundle()
        bundle.putString(":settings:fragment_args_key", componentName.flattenToString())
        intent.putExtra(":settings:show_fragment_args", bundle)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to general Accessibility Settings
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}


fun openDeviceAdmin(context: Context){
    val componentName = ComponentName(context, DeviceAdmin::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required to enable anti uninstall.")
    }
    context.startActivity(intent)
}