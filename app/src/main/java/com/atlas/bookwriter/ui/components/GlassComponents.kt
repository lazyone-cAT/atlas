package com.atlas.bookwriter.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.window.Dialog
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.bookwriter.ui.theme.UiSans
import com.atlas.bookwriter.ui.theme.DisplaySerif
import dev.chrisbanes.haze.HazeState

// ═══════════════════════════════════════════════════════
// DESIGN TOKENS — Glassmorphism
// ═══════════════════════════════════════════════════════

object GlassTokens {
    // Base
    val DarkBase = Color(0xFF0E1018)

    // Glass frost — pure white, no color tint
    val FrostPrimary = Color(0x1AFFFFFF)
    val FrostSecondary = Color(0x12FFFFFF)
    val FrostTopBar = Color(0x24FFFFFF)

    // Gradient highlight
    val TopHighlightStart = Color(0x18FFFFFF)
    val TopHighlightEnd = Color.Transparent

    // Border
    val BorderColor = Color(0x18FFFFFF)
    val BorderWidth = 0.5.dp

    // Inner highlight
    val InnerHighlight = Color(0x0DFFFFFF)

    // Outer shadow
    val ShadowColor = Color(0x40000000)
    val ShadowOffsetY = 8.dp
    val ShadowBlur = 24.dp

    // Corner radii
    val CornerLarge = 14.dp
    val CornerMedium = 12.dp
    val CornerSmall = 10.dp

    // Hand-drawn pencil stroke (mid-gray reads on dark AND light bases)
    val SketchStroke = Color(0xFF9E958A)
    val TapeColor = Color(0x59F0E2C0)
}

// Global toggle for the hand-drawn sketch chrome (driven from Settings)
val LocalSketchEnabled = staticCompositionLocalOf { true }

// ═══════════════════════════════════════════════════════
// SKETCH BORDER — wobbly pencil outline for rounded rects
// ═══════════════════════════════════════════════════════

private fun buildWobblyRect(
    width: Float,
    height: Float,
    radiusPx: Float,
    seed: Long,
    jitterPx: Float
): Path {
    val out = Path()
    if (width <= 2f || height <= 2f) return out
    val r = radiusPx.coerceIn(0f, minOf(width, height) / 2f)
    val rng = java.util.Random(seed)
    val pts = ArrayList<androidx.compose.ui.geometry.Offset>(96)

    fun jitter(x: Float, y: Float): androidx.compose.ui.geometry.Offset =
        androidx.compose.ui.geometry.Offset(
            x + (rng.nextFloat() - 0.5f) * 2f * jitterPx,
            y + (rng.nextFloat() - 0.5f) * 2f * jitterPx
        )

    fun edge(x0: Float, y0: Float, x1: Float, y1: Float) {
        val dx = x1 - x0
        val dy = y1 - y0
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        val n = (len / 14f).toInt().coerceAtLeast(2)
        for (i in 0 until n) {
            val t = i.toFloat() / n
            pts.add(jitter(x0 + dx * t, y0 + dy * t))
        }
    }

    fun corner(cx: Float, cy: Float, startDeg: Double) {
        val n = (r * Math.PI / 2.0 / 14.0).toInt().coerceAtLeast(3)
        for (i in 0 until n) {
            val a = Math.toRadians(startDeg) + (Math.PI / 2.0) * i / n
            pts.add(jitter(cx + (r * kotlin.math.cos(a)).toFloat(), cy + (r * kotlin.math.sin(a)).toFloat()))
        }
    }

    // Walk the rounded rect clockwise: top edge → TR corner → right edge → BR → bottom → BL → left → TL
    edge(r, 0f, width - r, 0f)
    corner(width - r, r, -90.0)
    edge(width, r, width, height - r)
    corner(width - r, height - r, 0.0)
    edge(width - r, height, r, height)
    corner(r, height - r, 90.0)
    edge(0f, height - r, 0f, r)
    corner(r, r, 180.0)

    if (pts.isEmpty()) return out
    pts.add(pts[0])
    out.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size - 1) {
        val midX = (pts[i].x + pts[i + 1].x) / 2f
        val midY = (pts[i].y + pts[i + 1].y) / 2f
        out.quadraticBezierTo(pts[i].x, pts[i].y, midX, midY)
    }
    out.close()
    return out
}

// Draws a wobbly pencil outline of the current draw bounds.
// Call INSIDE a draw lambda after drawContent() so it lands on top of children.
private val sketchPathCache = mutableMapOf<String, Path>()

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSketchStroke(
    cornerRadius: Dp,
    seed: Long = 0L
) {
    val r = cornerRadius.toPx()
    val w = 1.6.dp.toPx()
    val j = 1.8.dp.toPx()
    if (size.width <= 2f || size.height <= 2f) return
    val key = "${size.width.toInt()}_${size.height.toInt()}_${r.toInt()}_${j.toInt()}_${seed}"
    val outer = sketchPathCache.getOrPut(key) {
        buildWobblyRect(size.width, size.height, r, 42L + seed + r.toLong(), j)
    }
    val inner = sketchPathCache.getOrPut("$key#i") {
        buildWobblyRect(size.width, size.height, r, 97L + seed + r.toLong(), j * 0.7f)
    }
    drawPath(outer, color = GlassTokens.SketchStroke.copy(alpha = 0.85f), style = Stroke(width = w, cap = StrokeCap.Round))
    drawPath(inner, color = GlassTokens.SketchStroke.copy(alpha = 0.35f), style = Stroke(width = w * 0.65f, cap = StrokeCap.Round))
}

// A wobbly hand-drawn horizontal rule (used under top bars)
private val rulePathCache = mutableMapOf<String, Path>()

private fun buildWobblyLine(width: Float, yBase: Float, seed: Long): Path {
    val path = Path()
    if (width < 8f) return path
    val rng = java.util.Random(seed)
    val n = (width / 16f).toInt().coerceIn(4, 200)
    val pts = ArrayList<androidx.compose.ui.geometry.Offset>(n + 1)
    for (i in 0..n) {
        pts.add(
            androidx.compose.ui.geometry.Offset(
                width * i / n,
                yBase + (rng.nextFloat() - 0.5f) * 3f
            )
        )
    }
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size - 1) {
        val midX = (pts[i].x + pts[i + 1].x) / 2f
        val midY = (pts[i].y + pts[i + 1].y) / 2f
        path.quadraticBezierTo(pts[i].x, pts[i].y, midX, midY)
    }
    return path
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSketchRule(seed: Long = 11L) {
    val w = size.width
    if (w < 8f) return
    val y = size.height - 1.5.dp.toPx()
    val key = "${w.toInt()}_${size.height.toInt()}_${seed}"
    val path = rulePathCache.getOrPut(key) { buildWobblyLine(w, y, seed) }
    drawPath(path, color = GlassTokens.SketchStroke.copy(alpha = 0.7f), style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
}

// ═══════════════════════════════════════════════════════
// BACKGROUND — Dark base + colorful radial glows
// ═══════════════════════════════════════════════════════

@Composable
fun GlassBackground(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    base: Color = GlassTokens.DarkBase,
    glowA: Color = Color(0xFFD4A843),
    glowB: Color = Color(0xFF3A9E8F),
    glowC: Color = Color(0xFFA0522D),
    glowD: Color = Color(0xFF9B8EC0)
) {
    val infinite = rememberInfiniteTransition(label = "glow")
    val drift1 = infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse), label = "d1")
    val drift2 = infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Reverse), label = "d2")
    val drift3 = infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), label = "d3")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
    ) {
        // Watercolor washes — radial gradients fading to transparent.
        // No blur / no haze: one cheap shader per wash, and the drift
        // runs inside graphicsLayer (draw phase only, no recomposition).
        // Glow A — top-left
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.TopStart)
                .fillMaxWidth(0.55f)
                .aspectRatio(1f)
                .graphicsLayer {
                    translationX = (kotlin.math.sin(Math.toRadians(drift1.value.toDouble())) * 44.0).toFloat().dp.toPx()
                    translationY = (kotlin.math.cos(Math.toRadians(drift3.value.toDouble())) * 38.0).toFloat().dp.toPx()
                }
                .background(washBrush(glowA, 0.28f))
        )
        // Glow B — center-right
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .graphicsLayer {
                    translationX = (kotlin.math.sin(Math.toRadians(drift2.value.toDouble())) * -42.0).toFloat().dp.toPx()
                }
                .background(washBrush(glowB, 0.24f))
        )
        // Glow C — bottom-left
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.55f)
                .aspectRatio(1f)
                .graphicsLayer {
                    translationX = (kotlin.math.sin(Math.toRadians(drift1.value.toDouble() + 120.0)) * 46.0).toFloat().dp.toPx()
                    translationY = (-80).dp.toPx()
                }
                .background(washBrush(glowC, 0.26f))
        )
        // Glow D — top-right
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.45f)
                .aspectRatio(1f)
                .graphicsLayer {
                    translationX = (kotlin.math.sin(Math.toRadians(drift3.value.toDouble())) * -40.0).toFloat().dp.toPx()
                    translationY = 60.dp.toPx()
                }
                .background(washBrush(glowD, 0.30f))
        )
    }
}

// Soft watercolor wash: dense at the center, fully faded at the edge
private fun washBrush(color: Color, peakAlpha: Float): Brush =
    Brush.radialGradient(
        0f to color.copy(alpha = peakAlpha),
        0.55f to color.copy(alpha = peakAlpha * 0.45f),
        1f to color.copy(alpha = 0f)
    )

// ═══════════════════════════════════════════════════════
// GLASS SURFACE — pure frosted glass with grain + blur
// ═══════════════════════════════════════════════════════

private val grainBitmapCache = mutableMapOf<Int, android.graphics.Bitmap>()
private val grainShaderCache = mutableMapOf<Int, android.graphics.Shader>()

// Shared paint: reused across every card/dialog so drawing grain allocates nothing per frame
private val grainPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
    xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
}

private fun getGrainBitmap(size: Int): android.graphics.Bitmap {
    return grainBitmapCache.getOrPut(size) {
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val rng = java.util.Random()
        // One bulk pixel fill instead of size*size drawPoint calls
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            // Cluster around mid-gray so OVERLAY blend gives even grain
            val shade = 96 + rng.nextInt(65)
            pixels[i] = (0xFF shl 24) or (shade shl 16) or (shade shl 8) or shade
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        val canvas = android.graphics.Canvas(bmp)
        // Sparse paper fibers — faint horizontal wisps over the grain
        val fiberPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.5f
            alpha = 22
            color = android.graphics.Color.rgb(165, 165, 165)
            isAntiAlias = true
        }
        repeat(9) {
            val y0 = rng.nextInt(size).toFloat()
            val fiber = android.graphics.Path().apply {
                moveTo(0f, y0)
                var prevX = 0f
                var prevY = y0
                val seg = size / 4f
                repeat(4) {
                    val nextX = prevX + seg
                    val nextY = y0 + (rng.nextFloat() - 0.5f) * 6f
                    quadTo(prevX + seg / 2f, prevY + (rng.nextFloat() - 0.5f) * 5f, nextX, nextY)
                    prevX = nextX
                    prevY = nextY
                }
            }
            canvas.drawPath(fiber, fiberPaint)
        }
        bmp
    }
}

// Single drawRect through a cached BitmapShader — tiles the grain with one GPU op,
// reuses one Paint, allocates nothing per frame.
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrainOverlay(alpha: Int) {
    val shader = grainShaderCache.getOrPut(256) {
        android.graphics.BitmapShader(
            getGrainBitmap(256),
            android.graphics.Shader.TileMode.REPEAT,
            android.graphics.Shader.TileMode.REPEAT
        )
    }
    grainPaint.shader = shader
    grainPaint.alpha = alpha
    drawContext.canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, grainPaint)
}

@Composable
fun GlassSurface(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerLarge,
    tint: Color = GlassTokens.FrostPrimary,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val sketchEnabled = LocalSketchEnabled.current

    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = GlassTokens.ShadowColor,
                spotColor = GlassTokens.ShadowColor
            )
            .clip(shape)
            // Flat paint wash instead of frosted blur glass
            .background(tint)
            .drawWithContent {
                drawContent()
                // Paper grain — one shader drawRect, zero allocations
                drawGrainOverlay(64)
                // Hand-drawn pencil outline on top of everything
                if (sketchEnabled) drawSketchStroke(cornerRadius)
            }
            .then(
                if (sketchEnabled) Modifier
                else Modifier.border(GlassTokens.BorderWidth, GlassTokens.BorderColor, shape)
            )
    ) {
        // Content
        content()
    }
}

// ═══════════════════════════════════════════════════════
// TOP BAR — glass gradient strip
// ═══════════════════════════════════════════════════════

@Composable
fun GlassTopBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val sketchEnabled = LocalSketchEnabled.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Flat paper header strip (no frost blur)
            .background(GlassTokens.FrostTopBar.copy(alpha = 0.35f))
            .drawWithContent {
                drawContent()
                drawGrainOverlay(48)
                // Hand-ruled line across the bottom of the bar
                if (sketchEnabled) drawSketchRule(seed = 11L)
            },
        content = content
    )
}

// ═══════════════════════════════════════════════════════
// GLASS TAB
// ═══════════════════════════════════════════════════════

@Composable
fun GlassTab(
    selected: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val bgColor = if (selected) {
        Color(0x22FFFFFF)
    } else {
        Color.Transparent
    }
    val borderColor = if (selected) {
        Color(0x22FFFFFF)
    } else {
        Color.Transparent
    }
    val sketchEnabled = LocalSketchEnabled.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GlassTokens.CornerSmall))
            .background(bgColor)
            .then(
                if (selected && !sketchEnabled) Modifier.border(
                    GlassTokens.BorderWidth,
                    borderColor,
                    RoundedCornerShape(GlassTokens.CornerSmall)
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .drawWithContent {
                drawContent()
                if (sketchEnabled && selected) drawSketchStroke(GlassTokens.CornerSmall, seed = 3L)
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════
// GLASS CHIP
// ═══════════════════════════════════════════════════════

@Composable
fun GlassChip(
    selected: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bgColor = if (selected) {
        Color(0x22FFFFFF)
    } else {
        Color(0x0DFFFFFF)
    }
    val borderColor = if (selected) {
        Color(0x22FFFFFF)
    } else {
        GlassTokens.BorderColor
    }
    val sketchEnabled = LocalSketchEnabled.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(GlassTokens.CornerSmall))
            .background(bgColor)
            .then(
                if (sketchEnabled) Modifier
                else Modifier.border(GlassTokens.BorderWidth, borderColor, RoundedCornerShape(GlassTokens.CornerSmall))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .drawWithContent {
                drawContent()
                if (sketchEnabled) drawSketchStroke(GlassTokens.CornerSmall, seed = 5L)
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════
// DIALOG CONTAINER
// ═══════════════════════════════════════════════════════

@Composable
fun GlassDialogContainer(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        hazeState = hazeState,
        modifier = modifier,
        cornerRadius = GlassTokens.CornerLarge,
        tint = GlassTokens.FrostPrimary,
        content = content
    )
}

// ═══════════════════════════════════════════════════════
// SKETCH DIALOG — hand-drawn paper dialog (replaces Material AlertDialog)
// ═══════════════════════════════════════════════════════

@Composable
fun SketchAlertDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null
) {
    val sketch = LocalSketchEnabled.current
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 512.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(GlassTokens.DarkBase)
                .drawWithContent {
                    drawContent()
                    if (sketch) {
                        drawGrainOverlay(64)
                        drawSketchStroke(24.dp, seed = 77L)
                    }
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        fontFamily = DisplaySerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        color = Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (text != null) {
                    text()
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (confirmButton != null || dismissButton != null) {
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton?.invoke()
                        confirmButton?.invoke()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// SCREEN BACKGROUND — shared across all screens
// Shows custom editor bg if set, otherwise GlassBackground
// ═══════════════════════════════════════════════════════

@Composable
fun ScreenBackground(
    hazeState: HazeState,
    editorBackgroundPath: String = "",
    storage: com.atlas.bookwriter.data.Storage? = null,
    base: Color = GlassTokens.DarkBase,
    glowA: Color = Color(0xFFD4A843),
    glowB: Color = Color(0xFF3A9E8F),
    glowC: Color = Color(0xFFA0522D),
    glowD: Color = Color(0xFF9B8EC0)
) {
    val bgFile = if (storage != null && editorBackgroundPath.isNotEmpty()) {
        remember(editorBackgroundPath) { storage.loadImage(editorBackgroundPath) }
    } else null

    if (bgFile != null) {
        coil.compose.AsyncImage(
            model = bgFile,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.5f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(base.copy(alpha = 0.5f))
        )
    } else {
        GlassBackground(
            hazeState = hazeState,
            base = base,
            glowA = glowA,
            glowB = glowB,
            glowC = glowC,
            glowD = glowD
        )
    }
}

// ═══════════════════════════════════════════════════════
// BLUR OVERLAY
// ═══════════════════════════════════════════════════════

@Composable
fun BlurBackground(
    visible: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    )
}

// ═══════════════════════════════════════════════════════
// LEGACY COMPAT — kept so existing screens compile
// All now delegate to the new glass system
// ═══════════════════════════════════════════════════════

@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    // Legacy — now handled per-screen via GlassBackground
    Box(modifier = modifier.fillMaxSize().background(GlassTokens.DarkBase))
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerLarge,
    alpha: Float = 0.40f,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = remember { HazeState() }
    GlassSurface(
        hazeState = hazeState,
        modifier = modifier,
        cornerRadius = cornerRadius,
        tint = GlassTokens.FrostPrimary.copy(alpha = alpha),
        content = content
    )
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerMedium,
    alpha: Float = 0.34f,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = remember { HazeState() }
    GlassSurface(
        hazeState = hazeState,
        modifier = modifier,
        cornerRadius = cornerRadius,
        tint = GlassTokens.FrostSecondary.copy(alpha = alpha),
        content = content
    )
}

@Composable
fun LiquidGlassTopBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = remember { HazeState() }
    GlassTopBar(hazeState = hazeState, modifier = modifier, content = content)
}

@Composable
fun LiquidGlassTab(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val hazeState = remember { HazeState() }
    GlassTab(selected, hazeState, modifier, onClick, content)
}

@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hazeState = remember { HazeState() }
    GlassChip(selected, hazeState, onClick, modifier, content)
}

// ── Legacy aliases ──

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerLarge,
    glassAlpha: Float = 0.40f,
    borderAlpha: Float = 0.13f,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        hazeState = remember { HazeState() },
        modifier = modifier,
        cornerRadius = cornerRadius,
        tint = GlassTokens.FrostPrimary.copy(alpha = glassAlpha),
        content = content
    )
}

@Composable
fun GlassSurface_Legacy(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerMedium,
    glassAlpha: Float = 0.34f,
    borderAlpha: Float = 0.13f,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        hazeState = remember { HazeState() },
        modifier = modifier,
        cornerRadius = cornerRadius,
        tint = GlassTokens.FrostSecondary.copy(alpha = glassAlpha),
        content = content
    )
}

@Composable
fun GlassTopBar_Legacy(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassTopBar(hazeState = remember { HazeState() }, modifier = modifier, content = content)
}

@Composable
fun GlassChip_Legacy(
    selected: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassChip(selected, remember { HazeState() }, onClick, modifier, content)
}

@Composable
fun GlassTextFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassSurface(
        hazeState = remember { HazeState() },
        modifier = modifier,
        cornerRadius = GlassTokens.CornerSmall,
        tint = GlassTokens.FrostSecondary
    ) {
        content()
    }
}

@Composable
fun GradientBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(GlassTokens.DarkBase))
}

// ── Book system components ──

@Composable
fun BookBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(0.5.dp, GlassTokens.BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFE0E0E0),
            fontFamily = UiSans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun LeatherDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassTokens.BorderColor)
            .padding(vertical = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(GlassTokens.BorderColor.copy(alpha = 0.5f)).padding(vertical = 0.5.dp))
    }
}

@Composable
fun PaperReaderColumn(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        content()
    }
}

@Composable
fun BookSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        color = Color(0xFFE0E0E0)
    )
}

// ═══════════════════════════════════════════════════════
// DOODLES — hand-drawn accents
// ═══════════════════════════════════════════════════════

@Composable
fun SquiggleUnderline(
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 8.dp,
    color: Color = Color(0xFFC9A77C),
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(width, height)) {
        val path = Path()
        val w = size.width
        val h = size.height
        val waves = 4
        val seg = w / waves
        path.moveTo(0f, h * 0.55f)
        var x = 0f
        var up = true
        repeat(waves) {
            val nx = x + seg
            val cy = if (up) h * 0.1f else h * 0.95f
            path.quadraticBezierTo(x + seg / 2f, cy, nx, h * 0.55f)
            x = nx
            up = !up
        }
        drawPath(path, color, style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun SketchRing(
    modifier: Modifier = Modifier,
    color: Color = GlassTokens.SketchStroke,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier) {
        val w = strokeWidth.toPx()
        val inset = w * 1.6f
        val sz = Size(
            (size.width - inset * 2f).coerceAtLeast(1f),
            (size.height - inset * 2f).coerceAtLeast(1f)
        )
        val tl = Offset(inset, inset)
        drawArc(
            color = color.copy(alpha = 0.9f),
            startAngle = -78f,
            sweepAngle = 374f,
            useCenter = false,
            topLeft = tl,
            size = sz,
            style = Stroke(w, cap = StrokeCap.Round)
        )
        drawArc(
            color = color.copy(alpha = 0.4f),
            startAngle = -64f,
            sweepAngle = 352f,
            useCenter = false,
            topLeft = Offset(tl.x + 1.5f, tl.y + 1f),
            size = sz,
            style = Stroke(w * 0.7f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SketchStar(
    modifier: Modifier = Modifier,
    color: Color = GlassTokens.SketchStroke,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val w = strokeWidth.toPx()
        val r = minOf(size.width, size.height) / 2f - w
        drawLine(color, Offset(cx - r * 0.86f, cy - r * 0.5f), Offset(cx + r * 0.86f, cy + r * 0.5f), w, cap = StrokeCap.Round)
        drawLine(color, Offset(cx + r * 0.86f, cy - r * 0.5f), Offset(cx - r * 0.86f, cy + r * 0.5f), w, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, cy - r), Offset(cx, cy + r), w, cap = StrokeCap.Round)
    }
}
