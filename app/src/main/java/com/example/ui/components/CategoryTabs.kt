package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOutline
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextSecondary

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    categoryCounts: Map<String, Int>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(GeometricBackground)
            .padding(vertical = 10.dp)
            .testTag("category_tabs"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0

            val containerColor = if (isSelected) GeometricPrimary else GeometricSurface
            val contentColor = if (isSelected) GeometricOnPrimary else GeometricTextSecondary

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(containerColor)
                    .then(
                        if (!isSelected) {
                            Modifier.border(
                                border = BorderStroke(1.dp, GeometricOutline),
                                shape = CircleShape
                            )
                        } else Modifier
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .testTag("category_tab_$index"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )

                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) GeometricOnPrimary.copy(alpha = 0.15f)
                                    else GeometricOutline.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (count > 999) "999+" else count.toString(),
                                fontSize = 11.sp,
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

