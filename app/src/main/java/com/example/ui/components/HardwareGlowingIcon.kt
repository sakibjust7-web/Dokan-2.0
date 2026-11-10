package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanVividGreen
import java.util.Locale

/**
 * Returns the best matching icon for a hardware category or product name.
 */
fun getHardwareCategoryIcon(category: String, productName: String = ""): ImageVector {
    val combined = "$category $productName".lowercase(Locale.ROOT)
    return when {
        combined.contains("lock") || combined.contains("তালা") -> Icons.Default.Lock
        combined.contains("paint") || combined.contains("রং") || combined.contains("রঙ") || combined.contains("bucket") -> Icons.Default.FormatPaint
        combined.contains("drill") || combined.contains("power") || combined.contains("মেশিন") -> Icons.Default.PrecisionManufacturing
        combined.contains("switch") || combined.contains("electric") || combined.contains("সুইচ") || combined.contains("বিদ্যুৎ") || combined.contains("wire") -> Icons.Default.ElectricBolt
        combined.contains("nail") || combined.contains("পেরেক") || combined.contains("bolt") || combined.contains("nut") || combined.contains("স্ক্রু") || combined.contains("screw") -> Icons.Default.Construction
        combined.contains("hammer") || combined.contains("হাতুড়ি") || combined.contains("tools") || combined.contains("টুলস") -> Icons.Default.Build
        combined.contains("plumb") || combined.contains("pipe") || combined.contains("পাইপ") || combined.contains("water") -> Icons.Default.Plumbing
        combined.contains("cement") || combined.contains("সিমেন্ট") || combined.contains("বালু") -> Icons.Default.Home
        else -> Icons.Default.Inventory2
    }
}

/**
 * Large Glowing Category Icon in a circular dark glass container with vibrant neon green outline
 * and radial ambient glow, matching the mockups.
 */
@Composable
fun HardwareGlowingIcon(
    category: String,
    productName: String = "",
    photoKey: String = "",
    modifier: Modifier = Modifier,
    containerSize: Dp = 84.dp,
    iconSize: Dp = 38.dp,
    tint: Color = DokanVividGreen,
    showGlow: Boolean = true
) {
    if (isCustomProductPhoto(photoKey)) {
        AsyncImage(
            model = photoKey,
            contentDescription = category,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(containerSize)
                .clip(CircleShape)
                .border(1.5.dp, DokanVividGreen.copy(alpha = 0.8f), CircleShape)
        )
    } else {
        val icon = getHardwareCategoryIcon(category, productName)
        Box(
            modifier = modifier
                .size(containerSize)
                .drawBehind {
                    if (showGlow) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DokanVividGreen.copy(alpha = 0.25f),
                                    DokanDeepGreen.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.65f
                            ),
                            radius = size.width * 0.65f,
                            center = center
                        )
                    }
                }
                .border(
                    BorderStroke(1.5.dp, DokanVividGreen.copy(alpha = 0.9f)),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color(0xFF131D12)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
