package br.com.porteirointeligente.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.R
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.ui.components.*
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToVisitRegistration: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val condominio by viewModel.condominio.collectAsState()
    val apartamento by viewModel.apartamento.collectAsState()
    val visitas by viewModel.visitasRecentes.collectAsState()
    val morador by viewModel.moradorCadastrado.collectAsState()
    val todosMoradores by viewModel.todosMoradores.collectAsState()
    val qrCode by viewModel.qrCodeMorador.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statsTotalHoje by viewModel.statsTotalHoje.collectAsState()
    val statsEntradasHoje by viewModel.statsEntradasHoje.collectAsState()
    val statsVisitantesUnicos by viewModel.statsVisitantesUnicos.collectAsState()

    Scaffold(
        /*⚠️ WARNING: the HomeScreen currently uses `viewModel.moradorCadastrado` to access the selected resident.
          This remains consistent as `moradorCadastrado` is now an alias for `moradorSelecionado`.
          The HomeHeader composable also receives the selected resident.*/

        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToVisitRegistration) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Visita")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                item { ShimmerHeader() }
                item { ShimmerBox(modifier = Modifier.fillMaxWidth().height(140.dp), shape = MaterialTheme.shapes.large) }
                item { ShimmerStatsCard() }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShimmerBox(modifier = Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium)
                        ShimmerBox(modifier = Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium)
                    }
                }
                item { ShimmerTextLine(width = 140.dp, height = 20.dp) }
                repeat(3) { item { ShimmerVisitItem() } }
            } else {
                item {
                    HomeHeader(
                        condominio = condominio,
                        apartamento = apartamento,
                        morador = morador,
                        todosMoradores = todosMoradores,
                        onOwnerSelected = { viewModel.selecionarMorador(it.id) }
                    )
                }

                item {
                    ResidentSkin(morador, qrCode)
                }

                item {
                    StatsCard(
                        totalHoje = statsTotalHoje,
                        entradasAtivas = statsEntradasHoje,
                        visitantesUnicos = statsVisitantesUnicos
                    )
                }

                item {
                    ActionButtons(
                        hasMorador = morador != null,
                        onScannerClick = onNavigateToScanner,
                        onProfileClick = onNavigateToProfile
                    )
                }

                item {
                    Text(
                        text = "Visitas Recentes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (visitas.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma visita recente registrada.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(visitas) { visit ->
                        VisitItem(visit)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.credits_created_by),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    condominio: String,
    apartamento: String,
    morador: Owner?,
    todosMoradores: List<Owner> = emptyList(),
    onOwnerSelected: (Owner) -> Unit = {}
) {
    var showOwnerMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (morador != null) "Olá, ${morador.nome.split(" ").first()}" else "Bem-vindo,",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = condominio,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = apartamento,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Seletor de morador (dropdown)
                if (todosMoradores.size > 1) {
                    Box {
                        IconButton(onClick = { showOwnerMenu = true }) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Trocar morador",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showOwnerMenu,
                            onDismissRequest = { showOwnerMenu = false }
                        ) {
                            todosMoradores.forEach { owner ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            owner.nome.split(" ").first(),
                                            fontWeight = if (owner.id == morador?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onOwnerSelected(owner)
                                        showOwnerMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (owner.id == morador?.id) Icons.Default.CheckCircle else Icons.Default.Circle,
                                            contentDescription = null,
                                            tint = if (owner.id == morador?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (morador != null) {
                    AsyncImage(
                        model = morador.photoUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ResidentSkin(morador: Owner?, qrCode: Bitmap?) {
    if (morador != null && qrCode != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = qrCode.asImageBitmap(),
                        contentDescription = "Meu QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Seu QR Code de Acesso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Apresente este código para identificação rápida no condomínio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    totalHoje: Int,
    entradasAtivas: Int,
    visitantesUnicos: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Movimentação Hoje",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$totalHoje Visitas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Mini cards de estatísticas detalhadas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "No Prédio",
                value = entradasAtivas.toString(),
                icon = Icons.Default.Person,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Visitante(s) Único(s)",
                value = visitantesUnicos.toString(),
                icon = Icons.Default.Person,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    hasMorador: Boolean,
    onScannerClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onScannerClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sou Entregador")
        }
        
        OutlinedButton(
            onClick = onProfileClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp)
        ) {
            Text(if (hasMorador) "Ver meu QR" else "Cadastrar-me")
        }
    }
}

