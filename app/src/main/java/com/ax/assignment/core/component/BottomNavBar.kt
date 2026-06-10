package com.ax.assignment.core.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ax.assignment.R
import com.ax.assignment.core.navigation.Screen
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.NavigationOff
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.Surface as SurfaceColor

private val selectedColor = NavigationOn
private val unselectedColor = NavigationOff

@Composable
fun BottomNavBar(
    currentRoute: String,
    navController: NavController,
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, "홈", R.drawable.ic_figma_home),
        BottomNavItem(Screen.Statistics.route, "통계", R.drawable.ic_figma_bar_chart),
        BottomNavItem(Screen.Settings.route, "설정", R.drawable.ic_figma_settings),
    )

    Surface(color = SurfaceColor, shadowElevation = 0.dp) {
        Column {
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    BottomNavItemView(
                        item = item,
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        inclusive = item.route == Screen.Home.route
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        animationSpec = tween(200),
        label = "iconColor",
    )

    Column(
        modifier = Modifier
            .height(60.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(start = 36.dp, top = 14.dp, end = 36.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
            color = iconColor,
        )
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    @param:DrawableRes val iconRes: Int,
)

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    AXAssignmentTheme {
        BottomNavBar(
            currentRoute = Screen.Home.route,
            navController = rememberNavController(),
        )
    }
}
