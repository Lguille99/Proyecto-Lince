package cl.apiavengers.lince.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

val EstiloLince = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

// Estilos de compatibilidad para el Design System
val EstiloTituloPantalla = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
val EstiloSubtituloPantalla = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))

val EstiloNumeroKpi = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
val EstiloUnidadKpi = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
val EstiloSubtituloKpi = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp)
val EstiloMicroHeader = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)

val EstiloBotonPrimario = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
val EstiloBotonSecundario = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
val EstiloChip = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
val EstiloAlerta = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFFC62828))
val EstiloCuerpo = TextStyle(fontSize = 14.sp)
val EstiloCuerpoSecundario = TextStyle(fontSize = 14.sp, color = Color(0xFF64748B))
