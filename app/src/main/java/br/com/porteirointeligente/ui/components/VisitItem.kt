@file:Suppress("SpellCheckingInspection")

package br.com.porteirointeligente.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.ui.theme.Emerald
import br.com.porteirointeligente.ui.theme.PorteiroInteligenteTheme
import br.com.porteirointeligente.ui.theme.Rose
import br.com.porteirointeligente.ui.theme.Slate400
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern and interactive card for displaying an individual visit.
 *
 * Supports smooth expand/collapse animations to show additional details
 * such as documents, phone number, vehicle license plate, and exit time.
 *
 * @param visit Visit entity data.
 * @param modifier Optional layout modifier (first optional parameter per Compose guidelines).
 * @param onClick Optional callback triggered when the card is clicked.
 * @param initiallyExpanded Whether the card starts in an expanded state.
 */
@Composable
fun VisitItem(
    visit: Visit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val statusColor = visit.status.statusColor()
    val statusLabel = visit.status.displayName()

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "expand_rotation"
    )

    val relativeTime = remember(visit.dataEntrada) {
        formatRelativeTime(visit.dataEntrada)
    }

    val initials = remember(visit.nome) {
        extractInitials(visit.nome)
    }

    val accessibilityLabel = remember(visit, expanded, statusLabel) {
        buildString {
            append("Visita de ${visit.nome}, Apartamento ${visit.apartamento}. ")
            append("Status: $statusLabel. ")
            if (visit.motivo.isNotBlank()) append("Motivo: ${visit.motivo}. ")
            append(if (expanded) "Toque para recolher detalhes." else "Toque para expandir detalhes.")
        }
    }

    Card(
        onClick = {
            expanded = !expanded
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Status vertical accent bar on the left edge
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Top row: Avatar, visitor info, status chip & chevron
                VisitItemHeader(
                    visit = visit,
                    initials = initials,
                    statusColor = statusColor,
                    expanded = expanded,
                    rotationAngle = rotationAngle
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Time info row with relative elapsed time
                VisitItemTimeRow(
                    entryTimestamp = visit.dataEntrada,
                    relativeTime = relativeTime
                )

                // Expandable details container
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 3 }
                    ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    VisitItemExpandedDetails(visit = visit)
                }
            }
        }
    }
}

/**
 * Top header containing avatar, visitor name, apartment, status pill, and chevron.
 */
@Composable
private fun VisitItemHeader(
    visit: Visit,
    initials: String,
    statusColor: Color,
    expanded: Boolean,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular avatar with initials and status dot indicator
            VisitAvatar(
                initials = initials,
                statusColor = statusColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Visitor name and apartment info
            Column {
                Text(
                    text = visit.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Slate400
                    )
                    Text(
                        text = "Apto ${visit.apartamento}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    if (visit.motivo.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                        Text(
                            text = visit.motivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status pill and animated chevron
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VisitStatusPill(
                status = visit.status
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Recolher detalhes" else "Expandir detalhes",
                tint = Slate400,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(rotationZ = rotationAngle)
            )
        }
    }
}

/**
 * Circular avatar displaying initials and an indicator ring for visit status.
 */
@Composable
fun VisitAvatar(
    initials: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Box(
            modifier = Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(statusColor)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .align(Alignment.BottomEnd)
        )
    }
}

/**
 * Compact status pill with colored indicator and semi-transparent container background.
 */
@Composable
fun VisitStatusPill(
    status: VisitStatus,
    modifier: Modifier = Modifier
) {
    val color = status.statusColor()
    Surface(
        color = status.statusContainerColor(),
        contentColor = color,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Timestamp row displaying formatted entry time and relative elapsed time.
 */
@Composable
private fun VisitItemTimeRow(
    entryTimestamp: Long,
    relativeTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = Slate400
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = formatDateTime(entryTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }

        Text(
            text = relativeTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Expanded details section shown when the card is expanded.
 */
@Composable
private fun VisitItemExpandedDetails(
    visit: Visit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 12.dp)) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (visit.motivo.isNotBlank()) {
            VisitDetailRow(
                icon = Icons.Default.Description,
                label = "Motivo da Visita",
                value = visit.motivo
            )
        }

        if (visit.documento.isNotBlank()) {
            VisitDetailRow(
                icon = Icons.Default.Badge,
                label = "Documento (RG/CPF)",
                value = visit.documento
            )
        }

        if (visit.telefone.isNotBlank()) {
            VisitDetailRow(
                icon = Icons.Default.Phone,
                label = "Telefone / WhatsApp",
                value = visit.telefone
            )
        }

        if (!visit.placa.isNullOrBlank()) {
            VisitDetailRow(
                icon = Icons.Default.DirectionsCar,
                label = "Placa do Veículo",
                value = visit.placa.uppercase()
            ) {
                MercosulPlateBadge(plate = visit.placa.uppercase())
            }
        }

        if (visit.dataSaida != null) {
            Spacer(modifier = Modifier.height(4.dp))
            VisitDetailRow(
                icon = Icons.Default.CheckCircle,
                label = "Saída Registrada",
                value = formatDateTime(visit.dataSaida)
            )
        }
    }
}

/**
 * Reusable detail row displaying an icon, label, and formatted value or custom composable.
 */
@Composable
fun VisitDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    customContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
            if (customContent != null) {
                Spacer(modifier = Modifier.height(2.dp))
                customContent()
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Styled badge for vehicle license plates following Brazilian Mercosul standards.
 */
@Composable
fun MercosulPlateBadge(
    plate: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = plate,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UTILITY FUNCTIONS AND EXTENSIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns user-friendly display name for the visit status in Portuguese.
 */
fun VisitStatus.displayName(): String = when (this) {
    VisitStatus.ENTRADA_REGISTRADA -> "No local"
    VisitStatus.SAIDA_REGISTRADA -> "Concluída"
    VisitStatus.CANCELADA -> "Cancelada"
}

/**
 * Returns primary theme color corresponding to the visit status.
 */
@Composable
fun VisitStatus.statusColor(): Color = when (this) {
    VisitStatus.ENTRADA_REGISTRADA -> Emerald
    VisitStatus.SAIDA_REGISTRADA -> MaterialTheme.colorScheme.primary
    VisitStatus.CANCELADA -> Rose
}

/**
 * Returns tinted container color for the status pill background.
 */
@Composable
fun VisitStatus.statusContainerColor(): Color = statusColor().copy(alpha = 0.12f)

/**
 * Extracts up to 2 uppercase initials from a person's full name.
 */
fun extractInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "V"
    return trimmed.split("\\s+".toRegex())
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
}

/**
 * Formats a timestamp into human-readable relative time in Brazilian Portuguese.
 *
 * Kept public for backwards compatibility with screens like VisitHistoryScreen.
 */
fun formatRelativeTime(
    timestamp: Long,
    now: Long = System.currentTimeMillis()
): String {
    val diffMillis = now - timestamp
    if (diffMillis < 0) return "Agora"
    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "Agora há pouco"
        diffMinutes < 60 -> "Há $diffMinutes min"
        diffHours < 24 -> "Há ${diffHours}h"
        diffDays == 1L -> "Ontem"
        diffDays < 7 -> "Há $diffDays dias"
        else -> {
            val sdf = SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR"))
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * Formats a timestamp into standard Brazilian date and time format.
 */
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Active Visit - Light Mode", showBackground = true)
@Composable
private fun VisitItemActivePreview() {
    PorteiroInteligenteTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VisitItem(
                visit = Visit(
                    id = 1L,
                    nome = "Carlos Eduardo Santos",
                    documento = "12.345.678-9",
                    apartamento = "104",
                    telefone = "(11) 98765-4321",
                    motivo = "Entrega de encomenda",
                    dataEntrada = System.currentTimeMillis() - 15 * 60 * 1000,
                    status = VisitStatus.ENTRADA_REGISTRADA,
                    placa = "BRA2E19"
                ),
                initiallyExpanded = true
            )
        }
    }
}

@Preview(name = "Concluded Visit - Dark Mode", showBackground = true)
@Composable
private fun VisitItemConcludedPreview() {
    PorteiroInteligenteTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            VisitItem(
                visit = Visit(
                    id = 2L,
                    nome = "Mariana Alcantara",
                    documento = "98.765.432-1",
                    apartamento = "302",
                    telefone = "(11) 91234-5678",
                    motivo = "Visita familiar",
                    dataEntrada = System.currentTimeMillis() - 3 * 3600 * 1000,
                    dataSaida = System.currentTimeMillis() - 30 * 60 * 1000,
                    status = VisitStatus.SAIDA_REGISTRADA,
                    placa = null
                )
            )
        }
    }
}
