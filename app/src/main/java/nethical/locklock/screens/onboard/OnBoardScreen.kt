package nethical.locklock.screens.onboard

import android.content.Context.MODE_PRIVATE
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.core.content.edit

// Sealed class to represent different types of onboarding pages
sealed class OnboardingContent {
    // Custom composable content
    data class CustomPage(
        val content: @Composable (MutableState<Boolean>) -> Unit
    ) : OnboardingContent()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    pages: List<OnboardingContent>
) {
    // Remember the pager state
    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Coroutine scope for button actions
    val scope = rememberCoroutineScope()

    val isLastPage = pagerState.currentPage == pages.size - 1
    val isNextEnabled = remember  {mutableStateOf(false)}

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Horizontal Pager for swipeable pages
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = isNextEnabled.value,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { position ->
            when (val page = pages[position]) {
                is OnboardingContent.CustomPage -> {
                    page.content(isNextEnabled)
                }
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }

        // Back and Next buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(64.dp))
            AnimatedVisibility(
                visible = isNextEnabled.value,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Button(
                    onClick = {
//                        VibrationHelper.vibrate(50)
                        if (isLastPage) {
                            onFinishOnboarding()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    enabled = isNextEnabled.value
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    }
}
@Composable
fun OnBoardScreen(isOnBoardDone: MutableState<Boolean>) {

    val context = LocalContext.current
    val onboardingPages = listOf(
        OnboardingContent.CustomPage {
            PinSetupScreen(isNextEnabled = it)
        },
        OnboardingContent.CustomPage {
            PermissionRequestScreen(isNextEnabled = it)
        },

    )

    OnboardingScreen(
        onFinishOnboarding = {
            isOnBoardDone.value = true

            val setupSp = context.getSharedPreferences("isSetupDone", MODE_PRIVATE)
            setupSp.edit(commit = true) { putBoolean("isSetupDone", true) }
        },
        pages = onboardingPages
    )
}
