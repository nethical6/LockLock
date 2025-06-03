package nethical.locklock.data

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Represents information about an app.
 *
 * @property name The name of the app.
 * @property packageName The package name of the app.
 */
data class AppInfo(
    val icon: ImageBitmap,
    val name: String,
    val packageName: String
)