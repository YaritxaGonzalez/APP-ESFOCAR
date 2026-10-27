package com.example.caso_semestral.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.R
import com.example.caso_semestral.VerdeInstitucional

// Franja verde con el logo arriba (se usa en login y registro).
// El logo aparece con la misma animación de entrada que el formulario.
@Composable
fun EncabezadoLogo(subtitulo: String, titulo: String, color: Color = VerdeInstitucional) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .statusBarsPadding()   // el verde llega hasta arriba y el contenido no queda bajo la barra de estado
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // el Box reserva el espacio para que nada se mueva mientras el logo aparece
        Box(Modifier.height(110.dp), contentAlignment = Alignment.Center) {
            Aparece {
                Image(
                    painter = painterResource(R.drawable.logo_esfocar),
                    contentDescription = "Logo Escuela de Formación de Carabineros",
                    modifier = Modifier.height(110.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(subtitulo, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text(titulo, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
