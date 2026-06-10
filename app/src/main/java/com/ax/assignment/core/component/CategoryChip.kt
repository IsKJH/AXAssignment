package com.ax.assignment.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax.assignment.core.theme.AXAssignmentTheme
import com.ax.assignment.core.theme.CategorySelectedBg
import com.ax.assignment.core.theme.OnSurface
import com.ax.assignment.core.theme.OutlineSoft
import com.ax.assignment.core.theme.Primary
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.domain.model.Category
import com.ax.assignment.domain.model.TransactionType

/**
 * SCR-03/05 기준 카테고리 칩 (선택/미선택 상태).
 *
 * 미선택: Surface 배경, OutlineSoft(#E0E0E5) 테두리, OnSurface 텍스트
 * 선택됨: CategorySelectedBg(#E5E0FF) 배경, Primary(#3D2ED1) 테두리+텍스트
 * radius: 8dp
 */
@Composable
fun CategoryChip(
    category: Category,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) CategorySelectedBg else Surface
    val borderColor = if (isSelected) Primary else OutlineSoft
    val textColor = if (isSelected) Primary else OnSurface
    val fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = fontWeight,
            color = textColor,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, name = "미선택")
@Composable
private fun CategoryChipUnselectedPreview() {
    AXAssignmentTheme {
        CategoryChip(
            category = Category(1, "식비", "", "#5E92F3", TransactionType.EXPENSE),
            onClick = {},
            isSelected = false,
            modifier = androidx.compose.ui.Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, name = "선택됨")
@Composable
private fun CategoryChipSelectedPreview() {
    AXAssignmentTheme {
        CategoryChip(
            category = Category(1, "식비", "", "#5E92F3", TransactionType.EXPENSE),
            onClick = {},
            isSelected = true,
            modifier = androidx.compose.ui.Modifier.padding(8.dp),
        )
    }
}
