package nethical.locklock.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat.startActivity
import nethical.locklock.AppLockActivity
import nethical.locklock.services.LockedAppInfo.lockedApps
import kotlin.jvm.java

const val INTENT_ACTION_APP_UNLOCKED = "nethical.locklock.UNLOCKED"
object LockedAppInfo {
    var lockedApps = hashSetOf<String>()
}


class AppLockerService : AccessibilityService() {
    private var lastPackage = ""
    private var temporarilyUnlocked = ""
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || lastPackage == packageName) return

        val packageName = event.packageName?.toString() ?: return

        // Clear unlock if user has left the previously unlocked app
        if (lastPackage != packageName && temporarilyUnlocked.isNotEmpty()) {
            temporarilyUnlocked = ""
        }
        lastPackage = packageName

        Log.d("AppBlockerService", "Switched to app $packageName")
        if (lockedApps.contains(packageName) && !temporarilyUnlocked.contains(packageName)) {
            return
        }
        if (lockedApps.contains(packageName)) {
            val intent = Intent(this, AppLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("locked_package", packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // No-op
    }
    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val unlockedPackage = intent.getStringExtra("packageName") ?: return
            temporarilyUnlocked = unlockedPackage
            Log.d("AppBlockerService", "Unlocked $unlockedPackage")
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onServiceConnected() {
        super.onServiceConnected()
        val selectedAppsSp = getSharedPreferences("selected_apps", MODE_PRIVATE)

        lockedApps = selectedAppsSp.getStringSet("selected_apps",emptySet<String>())?.toHashSet() ?: hashSetOf()

        val filter = IntentFilter().apply {
            addAction(INTENT_ACTION_APP_UNLOCKED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(unlockReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(unlockReceiver, filter)
        }

    }
    override fun onDestroy() {
        unregisterReceiver(unlockReceiver)
        super.onDestroy()
    }

}
