package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DokanDeepGreen
import com.example.ui.theme.DokanVividGreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Slide to confirm sale interactive slider matching Image 1:
 * - Wide rounded pill track
 * - Vibrant neon green glowing circular thumb
 * - Centered "Slide to confirm sale" text
 * - Chevron ">" on the right
 * - Dragging knob or tapping slider confirms the sale.
 */
@Composable
fun SlideToConfirmSale(
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Slide to confirm sale",
    testTag: String = "slide_to_confirm_sale"
) {
    val coroutineScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(30.dp)
    val thumbSize = 48.dp

    BoxWithConstraints(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = DokanVividGreen.copy(alpha = 0.25f),
                spotColor = DokanVividGreen.copy(alpha = 0.35f)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0x555DD62C),
                        Color(0x33FFFFFF),
                        Color(0x22FFFFFF)
                    )
                ),
                shape = shape
            )
            .clip(shape)
            .background(Color(0xFF1A1A1A))
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (enabled) {
                    onConfirmed()
                }
            }
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val maxDragPx = with(density) { (maxWidth - thumbSize - 10.dp).toPx() }
        val offsetX = remember { Animatable(0f) }

        // Centered label & end chevron
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                color = if (enabled) Color(0xDDFFFFFF) else Color(0x66FFFFFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) Color(0xBBFFFFFF) else Color(0x44FFFFFF),
                modifier = Modifier.size(24.dp)
            )
        }

        // Draggable glowing green knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .drawBehind {
                    if (enabled) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DokanVividGreen.copy(alpha = 0.6f),
                                    DokanVividGreen.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.75f
                            ),
                            radius = size.width * 0.75f,
                            center = center
                        )
                    }
                }
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = DokanVividGreen,
                    spotColor = DokanVividGreen
                )
                .clip(CircleShape)
                .background(
                    if (enabled) DokanVividGreen else Color(0xFF333333)
                )
                .draggable(
                    enabled = enabled,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val target = (offsetX.value + delta).coerceIn(0f, maxDragPx)
                            offsetX.snapTo(target)
                        }
                    },
                    onDragStopped = {
                        if (offsetX.value > maxDragPx * 0.70f) {
                            coroutineScope.launch {
                                offsetX.animateTo(maxDragPx, tween(150))
                                onConfirmed()
                                offsetX.animateTo(0f, tween(250))
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, tween(200))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Optional inner dot/ring
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A1805).copy(alpha = 0.15f))
            )
        }
    }
}
