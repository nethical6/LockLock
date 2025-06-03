package nethical.locklock.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
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
        val openedApp = event?.packageName?.toString() ?: return

        // Clear unlock if user has left the previously unlocked app
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && lastPackage != packageName){
            if ((lastPackage != openedApp ) && temporarilyUnlocked.isNotEmpty()) {
                temporarilyUnlocked = ""
            }
        }
        if (lastPackage == openedApp) return

        lastPackage = openedApp

        Log.d("AppBlockerService", "Switched to app $openedApp")
        if (lockedApps.contains(openedApp) && temporarilyUnlocked==(openedApp)) {
            return
        }
        if (lockedApps.contains(openedApp)) {
            val intent = Intent(this, AppLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                putExtra("locked_package", openedApp)
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
            lastPackage = packageName
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
