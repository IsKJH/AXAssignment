package com.ax.assignment.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ax.assignment.R
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.NavigationOn
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(navController) {
        delay(SPLASH_DURATION_MS)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
    }
    SplashContent(showLoadingDots = true)
}

// Figma 368:1519 — white bg, brand logo box + app name centered, copyright at bottom
@Composable
fun SplashContent(showLoadingDots: Boolean = false) {
    // Logo entrance: fade in while scaling up slightly
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    // Loading dots above the logo, lighting up one by one
    var activeDot by remember { mutableStateOf(0) }
    LaunchedEffect(showLoadingDots) {
        while (showLoadingDots) {
            delay(DOT_STEP_MS)
            activeDot = (activeDot + 1) % DOT_COUNT
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        if (showLoadingDots) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * DOT_TOP_FRACTION)
                    .alpha(appear.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(DOT_COUNT) { index ->
                    val dotAlpha by animateFloatAsState(
                        targetValue = if (index == activeDot) 1f else 0.25f,
                        animationSpec = tween(250),
                        label = "splashDot$index",
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .background(NavigationOn, CircleShape),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(appear.value)
                .scale(0.85f + 0.15f * appear.value),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(NavigationOn, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_splash_logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "AX 가계부",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = Color(0xFF020202),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "COPYRIGHT 2026.AX 가계부 ALL RIGHTS RESERVED.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp,
            color = Color(0xFF020202),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 50.dp),
        )
    }
}

private const val SPLASH_DURATION_MS = 2_000L
private const val DOT_COUNT = 3
private const val DOT_STEP_MS = 400L
private const val DOT_TOP_FRACTION = 0.25f

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashContentPreview() {
    AXAssignmentTheme {
        SplashContent()
    }
}
