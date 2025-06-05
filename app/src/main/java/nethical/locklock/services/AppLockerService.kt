package nethical.locklock.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.ContextCompat.startActivity
import nethical.locklock.AppLockActivity
import java.util.Locale
import kotlin.jvm.java

const val INTENT_ACTION_APP_UNLOCKED = "nethical.locklock.UNLOCKED"
val ANTI_UNINSTALL_KEYWORDS = hashSetOf<String>("uninstall","forcestop","security","privacy","shortcut","locklocktscreen","admin")
object AppLockerInfo {
    var lockedApps = hashSetOf<String>()
    var isAntiUninstallOn = true
}


class AppLockerService : AccessibilityService() {
    private var lastPackage = ""
    private var temporarilyUnlocked = ""
    private var keywordsFound = hashSetOf<String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val openedApp = event?.packageName?.toString() ?: return

        // Clear unlock if user has left the previously unlocked app
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && lastPackage != packageName){
            if ((lastPackage != openedApp ) && temporarilyUnlocked.isNotEmpty()) {
                temporarilyUnlocked = ""
            }
        }
        if(event.packageName=="com.android.settings" && AppLockerInfo.isAntiUninstallOn){
            traverseNodesForKeywords(rootInActiveWindow)
        }

        if (lastPackage == openedApp) return

        lastPackage = openedApp

        Log.d("AppBlockerService", "Switched to app $openedApp")
        if (AppLockerInfo.lockedApps.contains(openedApp) && temporarilyUnlocked==(openedApp)) {
            return
        }
        if (AppLockerInfo.lockedApps.contains(openedApp)) {
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
        val additionalInfoSp = getSharedPreferences("additional_info",Context.MODE_PRIVATE)

        AppLockerInfo.lockedApps = selectedAppsSp.getStringSet("selected_apps",emptySet<String>())?.toHashSet() ?: hashSetOf()
        AppLockerInfo.isAntiUninstallOn = additionalInfoSp.getBoolean("is_anti_uninstall",false)
        
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

    fun traverseNodesForKeywords(
        node: AccessibilityNodeInfo?
    ) {
        if (node == null) {
            return
        }
        if (node.className != null && node.className == "android.widget.TextView") {
            val nodeText = node.text
            if (nodeText != null) {
                val editTextContent = nodeText.toString().lowercase(Locale.getDefault())
                ANTI_UNINSTALL_KEYWORDS.forEachIndexed { i, word ->
                    if (editTextContent.replace(" ","").contains(word)) {
                        keywordsFound.add(word)
                        Log.d("AntiUninstall","Found a Blocked Word:  $word")
                    }
                    if(keywordsFound.isNotEmpty() && editTextContent.contains("locklock")){
                        performGlobalAction(GLOBAL_ACTION_HOME)
                        keywordsFound.clear()
                        return
                    }
                    if(i==ANTI_UNINSTALL_KEYWORDS.size-1) keywordsFound.clear()
                }
            }
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            traverseNodesForKeywords(childNode)
        }
    }

}

