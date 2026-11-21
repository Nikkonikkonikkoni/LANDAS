package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.wvsu.landas.ui.theme.Gold
import ph.edu.wvsu.landas.ui.theme.Navy
import ph.edu.wvsu.landas.ui.theme.SafetyOrange
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary

@Composable
fun SplashScreen(bootError: String? = null) {
    val transition = rememberInfiniteTransition(label = "splash")
    val progress by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), repeatMode = RepeatMode.Restart),
        label = "progress"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealDark, TealPrimary, Color(0xFF114C52))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val route = listOf(
                Offset(size.width * 0.05f, size.height * 0.75f),
                Offset(size.width * 0.28f, size.height * 0.62f),
                Offset(size.width * 0.57f, size.height * 0.70f),
                Offset(size.width * 0.82f, size.height * 0.52f),
                Offset(size.width * 0.96f, size.height * 0.58f)
            )
            for (i in 0 until route.lastIndex) {
                drawLine(
                    color = Color.White.copy(alpha = 0.10f),
                    start = route[i],
                    end = route[i + 1],
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                )
            }
            route.forEachIndexed { index, point ->
                drawCircle(
                    color = if (index == route.lastIndex) Gold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.22f),
                    radius = if (index == route.lastIndex) 8.dp.toPx() else 4.dp.toPx(),
                    center = point
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.95f, size.height * 0.10f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.3f,
                center = Offset(size.width * 0.02f, size.height * 0.9f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.8f))
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .background(Color.White, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(TealPrimary, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AltRoute,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "LANDAS",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Location-Aware Navigation and\nDisaster Assistance System",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(26.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                    Text(
                        "WVSU – Pototan Campus",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (bootError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.85f)
                ) {
                    Text(
                        text = bootError,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.16f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(Gold, SafetyOrange)),
                                    CircleShape
                                )
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Preparing offline campus data…",
                        color = Color.White.copy(alpha = 0.76f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(13.dp))
                Text(
                    "Works offline when you need it most",
                    color = Color.White.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
