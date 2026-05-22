package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun DoodleDumbbell(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.05f
        val drawStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Center handle
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.5f)
            
            // Left weight inner
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.25f, h * 0.65f)
            lineTo(w * 0.20f, h * 0.65f)
            lineTo(w * 0.20f, h * 0.35f)
            close()

            // Left weight outer
            moveTo(w * 0.20f, h * 0.25f)
            lineTo(w * 0.20f, h * 0.75f)
            lineTo(w * 0.12f, h * 0.75f)
            lineTo(w * 0.12f, h * 0.25f)
            close()

            // Right weight inner
            moveTo(w * 0.75f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.65f)
            lineTo(w * 0.80f, h * 0.65f)
            lineTo(w * 0.80f, h * 0.35f)
            close()

            // Right weight outer
            moveTo(w * 0.80f, h * 0.25f)
            lineTo(w * 0.80f, h * 0.75f)
            lineTo(w * 0.88f, h * 0.75f)
            lineTo(w * 0.88f, h * 0.25f)
            close()
        }
        drawPath(path, color = color, style = drawStroke)
    }
}

@Composable
fun DoodleHeartRate(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.04f
        val drawStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        val path = Path().apply {
            moveTo(w * 0.05f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.4f, h * 0.2f)
            lineTo(w * 0.5f, h * 0.8f)
            lineTo(w * 0.6f, h * 0.35f)
            lineTo(w * 0.7f, h * 0.55f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.95f, h * 0.5f)
        }
        drawPath(path, color = color, style = drawStroke)
    }
}

@Composable
fun DoodleStar(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.05f
        val drawStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // 5-point star path drawn as a single continuous line for that hand-drawn look
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.62f, h * 0.4f)
            lineTo(w * 0.95f, h * 0.4f)
            lineTo(w * 0.68f, h * 0.6f)
            lineTo(w * 0.78f, h * 0.9f)
            lineTo(w * 0.5f, h * 0.72f)
            lineTo(w * 0.22f, h * 0.9f)
            lineTo(w * 0.32f, h * 0.6f)
            lineTo(w * 0.05f, h * 0.4f)
            lineTo(w * 0.38f, h * 0.4f)
            close()
        }
        drawPath(path, color = color, style = drawStroke)
    }
}

@Composable
fun DoodleApple(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.04f
        val drawStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Apple outline and stem
        val path = Path().apply {
            // Left half curve
            moveTo(w * 0.5f, h * 0.3f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.15f, h * 0.4f, w * 0.2f, h * 0.65f)
            cubicTo(w * 0.25f, h * 0.85f, w * 0.45f, h * 0.9f, w * 0.5f, h * 0.85f)
            
            // Right half curve
            cubicTo(w * 0.55f, h * 0.9f, w * 0.75f, h * 0.85f, w * 0.8f, h * 0.65f)
            cubicTo(w * 0.85f, h * 0.4f, w * 0.7f, h * 0.2f, w * 0.5f, h * 0.3f)
            
            // Stem
            moveTo(w * 0.5f, h * 0.3f)
            quadraticTo(w * 0.55f, h * 0.15f, w * 0.65f, h * 0.12f)
        }
        drawPath(path, color = color, style = drawStroke)
    }
}
