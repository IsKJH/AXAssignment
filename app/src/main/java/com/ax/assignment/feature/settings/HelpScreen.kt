package com.ax.assignment.feature.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.ax.assignment.R
import com.ax.assignment.core.component.AppTopBar
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.BrandLight
import com.ax.assignment.core.theme.CardPrimary
import com.ax.assignment.core.theme.ConfirmButtonBg
import com.ax.assignment.core.theme.HomeExpenseAmount
import com.ax.assignment.core.theme.NavigationOn
import com.ax.assignment.core.theme.SettingsBackground
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.core.theme.TextDefault
import com.ax.assignment.core.theme.TextDescription

private data class GuideStep(
    val caption: String,
    val visual: @Composable () -> Unit,
)

private data class HelpItem(
    val title: String,
    val description: String,
    val steps: List<GuideStep>,
)

@Composable
fun HelpScreen(navController: NavController) {
    HelpContent(onNavigateBack = { navController.popBackStack() })
}

@Composable
fun HelpContent(onNavigateBack: () -> Unit) {
    val helpItems = rememberHelpItems()
    var guideItem by remember { mutableStateOf<HelpItem?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "사용 방법",
                onBack = onNavigateBack,
            )
        },
        containerColor = SettingsBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Cap content width so cards stay readable on tablets/landscape
            Column(modifier = Modifier.widthIn(max = 600.dp)) {
                Text(
                    text = "항목을 누르면 단계별 가이드를 볼 수 있어요",
                    color = TextDescription,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(8.dp)),
                ) {
                    helpItems.forEachIndexed { index, item ->
                        HelpRow(
                            number = "${index + 1}",
                            item = item,
                            onClick = { guideItem = item },
                        )
                        if (index != helpItems.lastIndex) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFEEEEEE)),
                            )
                        }
                    }
                }
            }
        }
    }

    guideItem?.let { item ->
        GuideTourDialog(item = item, onDismiss = { guideItem = null })
    }
}

@Composable
private fun HelpRow(
    number: String,
    item: HelpItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BrandLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = CardPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                color = TextDefault,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
            )
            Text(
                text = item.description,
                color = TextDescription,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_figma_chevron_right),
            contentDescription = "가이드 보기",
            tint = TextDescription,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Guide tour dialog — coach-mark style step viewer with pulsing spotlight
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GuideTourDialog(item: HelpItem, onDismiss: () -> Unit) {
    var stepIndex by remember(item) { mutableIntStateOf(0) }
    val isLast = stepIndex == item.steps.lastIndex

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .background(Surface, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { /* consume click */ }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.title,
                    color = TextDefault,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = stepIndex,
                    transitionSpec = {
                        (slideInHorizontally { it / 3 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut(tween(150)))
                    },
                    label = "guideStep",
                ) { index ->
                    val step = item.steps[index]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Visual stage — mini mockup of the real UI element
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(BrandLight, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            step.visual()
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = step.caption,
                            color = TextDefault,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.height(46.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.steps.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == stepIndex) 8.dp else 6.dp)
                                .background(
                                    if (i == stepIndex) CardPrimary else Color(0xFFE0E0E0),
                                    CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { if (isLast) onDismiss() else stepIndex++ },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLast) ConfirmButtonBg else NavigationOn,
                        contentColor = Surface,
                    ),
                ) {
                    Text(
                        text = if (isLast) "완료" else "다음",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/** Coach-mark pulse — an expanding, fading ring matching the element's shape. */
@Composable
private fun PulseSpotlight(
    modifier: Modifier = Modifier,
    rounded: Boolean = false,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseProgress",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(1f + progress * 0.35f),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val color = NavigationOn.copy(alpha = (1f - progress) * 0.5f)
                val stroke = StrokeStyle(width = 3.dp.toPx())
                if (rounded) {
                    drawRoundRect(
                        color = color,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                        style = stroke,
                    )
                } else {
                    drawCircle(color = color, style = stroke)
                }
            }
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini mockups of real UI elements (same tokens as the actual screens)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MockFab() {
    PulseSpotlight(Modifier.size(56.dp)) {
        Box(
            modifier = Modifier.size(56.dp).background(NavigationOn, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_add),
                contentDescription = null,
                tint = Surface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun MockAmountField() {
    Column(Modifier.width(220.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("12,000", color = TextDefault, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("원", color = TextDefault, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(TextDefault))
        Spacer(Modifier.height(14.dp))
        Text("점심값", color = TextDescription, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD2D2D2)))
    }
}

@Composable
private fun MockConfirmButton() {
    PulseSpotlight(Modifier.size(196.dp, 56.dp), rounded = true) {
        Box(
            modifier = Modifier
                .size(180.dp, 44.dp)
                .background(ConfirmButtonBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("확인", color = Surface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockRecurringCheck() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PulseSpotlight(Modifier.size(34.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_checkbox_on),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Text("정기 지출로 등록", color = TextDefault, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MockAutoRegister() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("6월 15일", "7월 15일", "8월 15일").forEachIndexed { i, label ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(8.dp).background(NavigationOn.copy(alpha = 1f - i * 0.3f), CircleShape))
                Text(
                    text = "$label 자동 등록",
                    color = if (i == 0) TextDefault else TextDescription,
                    fontSize = 14.sp,
                    fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun MockTransactionRow() {
    PulseSpotlight(Modifier.size(254.dp, 68.dp), rounded = true) {
        Row(
            modifier = Modifier
                .size(240.dp, 56.dp)
                .background(Surface, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).background(Color(0xFFFFAC11), CircleShape))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("식비", color = TextDefault, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("점심값", color = TextDescription, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("-12,000", color = HomeExpenseAmount, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockEditAction() {
    Row(
        modifier = Modifier
            .width(240.dp)
            .background(Surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("상세내역", color = TextDefault, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        PulseSpotlight(Modifier.size(56.dp, 38.dp), rounded = true) {
            Text("편집", color = NavigationOn, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MockScopeSheet() {
    Column(
        modifier = Modifier
            .width(220.dp)
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("이 달만 수정", color = TextDefault, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("이후 내역 모두 수정", color = TextDescription, fontSize = 13.sp)
        Text("전체 수정", color = TextDescription, fontSize = 13.sp)
    }
}

@Composable
private fun MockSettingsRow() {
    PulseSpotlight(Modifier.size(254.dp, 62.dp), rounded = true) {
        Row(
            modifier = Modifier
                .size(240.dp, 50.dp)
                .background(Surface, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("시작일 설정", color = TextDefault, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("매달 1일", color = NavigationOn, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MockDayPicker() {
    Column(
        modifier = Modifier.width(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("14일", color = TextDescription, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("15일", color = NavigationOn, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Text("16일", color = TextDescription, fontSize = 14.sp)
    }
}

@Composable
private fun MockDonut() {
    PulseSpotlight(Modifier.size(110.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(92.dp)) {
                val stroke = StrokeStyle(width = 14.dp.toPx())
                drawArc(Color(0xFFFFAC11), -90f, 220f, false, style = stroke)
                drawArc(Color(0xFF81C784), 130f, 80f, false, style = stroke)
                drawArc(Color(0xFF1872FA), 210f, 60f, false, style = stroke)
            }
            Text("61%", color = TextDefault, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockSixMonthButton() {
    PulseSpotlight(Modifier.size(116.dp, 44.dp), rounded = true) {
        Box(
            modifier = Modifier
                .background(NavigationOn, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("6개월 내역", color = Surface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockSwipe() {
    val transition = rememberInfiniteTransition(label = "swipe")
    val offset by transition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "swipeOffset",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("‹", color = TextDefault, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("6월1일 ~ 6월30일", color = TextDefault, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("›", color = TextDefault, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.width(160.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "👈",
                fontSize = 26.sp,
                modifier = Modifier.padding(start = (offset + 30).dp),
            )
        }
    }
}

@Composable
private fun rememberHelpItems(): List<HelpItem> = remember {
    listOf(
        HelpItem(
            title = "거래 추가",
            description = "+ 버튼으로 지출·수입을 입력해요",
            steps = listOf(
                GuideStep("홈 오른쪽 아래의 + 버튼을 눌러요") { MockFab() },
                GuideStep("금액과 내역을 입력하고\n카테고리·일시를 선택해요") { MockAmountField() },
                GuideStep("확인을 누르면 홈에 바로 반영돼요") { MockConfirmButton() },
            ),
        ),
        HelpItem(
            title = "정기 거래 등록",
            description = "구독료·월급은 매달 자동으로 기록돼요",
            steps = listOf(
                GuideStep("거래 추가에서 \"정기 지출로 등록\"을 체크해요") { MockRecurringCheck() },
                GuideStep("선택한 일자에 매달 자동으로 등록돼요") { MockAutoRegister() },
            ),
        ),
        HelpItem(
            title = "내역 확인과 수정",
            description = "내역을 눌러 편집하거나 삭제해요",
            steps = listOf(
                GuideStep("홈에서 거래 내역을 눌러요") { MockTransactionRow() },
                GuideStep("상세 화면의 편집으로 수정·삭제해요") { MockEditAction() },
                GuideStep("정기 거래는 적용 범위를 고를 수 있어요") { MockScopeSheet() },
            ),
        ),
        HelpItem(
            title = "정산 주기 설정",
            description = "시작일을 바꾸면 주기가 다시 계산돼요",
            steps = listOf(
                GuideStep("설정에서 시작일 설정으로 들어가요") { MockSettingsRow() },
                GuideStep("1~28일 중 고르면 홈·통계에 바로 반영돼요") { MockDayPicker() },
            ),
        ),
        HelpItem(
            title = "통계 보기",
            description = "카테고리별 비중과 소비 흐름을 확인해요",
            steps = listOf(
                GuideStep("도넛을 누른 채 문지르면\n카테고리별 비중이 보여요") { MockDonut() },
                GuideStep("6개월 내역에서 소비 흐름을 확인해요") { MockSixMonthButton() },
            ),
        ),
        HelpItem(
            title = "주기 이동",
            description = "좌우 스와이프로 주기를 이동해요",
            steps = listOf(
                GuideStep("화면을 좌우로 쓸어넘기거나\n화살표를 눌러 주기를 이동해요") { MockSwipe() },
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun HelpContentPreview() {
    AXAssignmentTheme {
        HelpContent(onNavigateBack = {})
    }
}
