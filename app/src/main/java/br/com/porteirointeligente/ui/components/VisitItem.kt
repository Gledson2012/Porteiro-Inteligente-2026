package br.com.porteirointeligente.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.porteirointeligente.domain.model.Visit
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VisitItem(visit: Visit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateSdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    // Define a cor de destaque com base no status da visita
    val statusColor = when (visit.status) {
        br.com.porteirointeligente.domain.model.VisitStatus.ENTRADA_REGISTRADA -> MaterialTheme.colorScheme.primary // Laranja/Destaque
        br.com.porteirointeligente.domain.model.VisitStatus.SAIDA_REGISTRADA -> MaterialTheme.colorScheme.secondary // Slate ou Teal se definido
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // Cantos mais suaves e modernos (16.dp)
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra lateral indicadora de status
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(statusColor)
                    .height(80.dp) // Altura fixa sutil para consistência
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar circular com iniciais
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            statusColor.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = visit.nome.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // Dados textuais do visitante
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${visit.motivo} • Ap ${visit.apartamento}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Data e Hora de entrada
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = sdf.format(Date(visit.dataEntrada)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateSdf.format(Date(visit.dataEntrada)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
