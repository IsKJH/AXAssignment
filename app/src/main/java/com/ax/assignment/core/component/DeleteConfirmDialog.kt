package com.ax.assignment.core.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.DangerRed
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.OnSurfaceVariant
import com.ax.assignment.core.theme.TextDescription

/**
 * SCR-04 기준 거래 삭제 확인 다이얼로그.
 *
 * - 다이얼로그 radius: 16dp
 * - 버튼 height: 48dp, radius: 8dp
 * - 취소: SurfaceVariant(#EEEEEE) 배경, OnSurface 텍스트
 * - 삭제: DangerRed(#DA2E0B) 배경, 흰 텍스트
 */
@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "이 내역을 삭제할까요?",
    description: String = "삭제된 내역은 복구할 수 없으니\n신중하게 결정해 주세요.",
    confirmText: String = "삭제",
) {
    val haptic = LocalHapticFeedback.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 제목 + 설명
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDescription,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                // 버튼 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFEEEEEE),
                            contentColor = OnSurface,
                        ),
                        border = null,
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // 삭제 버튼
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmDialogPreview() {
    AXAssignmentTheme {
        DeleteConfirmDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
