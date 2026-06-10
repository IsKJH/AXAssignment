package com.ax.assignment.core.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 주기(월) 전환용 좌우 스와이프 제스처.
 * - 오른쪽 스와이프 → 이전 주기, 왼쪽 스와이프 → 다음 주기 (ViewPager 관례)
 * - 가로 드래그만 소비하므로 세로 스크롤과 충돌하지 않는다
 */
fun Modifier.periodSwipe(
    onPrev: () -> Unit,
    onNext: () -> Unit,
): Modifier = pointerInput(Unit) {
    val threshold = 80.dp.toPx()
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            when {
                totalDrag > threshold -> onPrev()
                totalDrag < -threshold -> onNext()
            }
        },
    ) { _, dragAmount -> totalDrag += dragAmount }
}
