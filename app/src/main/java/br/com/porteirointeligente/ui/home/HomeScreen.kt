package br.com.porteirointeligente.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.ui.components.AppSignature
import br.com.porteirointeligente.ui.components.ShimmerCard
import br.com.porteirointeligente.ui.components.VisitItem
import br.com.porteirointeligente.ui.theme.GradientGold
import br.com.porteirointeligente.ui.theme.GradientPrimary
import br.com.porteirointeligente.ui.theme.GradientTeal
import br.com.porteirointeligente.ui.theme.GradientNeon
import br.com.porteirointeligente.ui.theme.Slate400

// ... imports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToRegisterVisit: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToOwners: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (val state = uiState) {
        is HomeUIState.Loading -> {
            LoadingContent(modifier = Modifier.fillMaxSize())
        }
        is HomeUIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Erro: ${state.message}")
            }
        }
        is HomeUIState.Success -> {
            val ownerName = state.selectedOwner?.nome ?: ""
            val greeting = getGreeting()

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = greeting,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = if (ownerName.isNotBlank()) ownerName else "Porteiro Inteligente",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        navigationIcon = {
                            Box(modifier = Modifier.padding(start = 12.dp)) {
                                OwnerAvatar(
                                    photoUri = state.selectedOwner?.photoUri,
                                    name = state.selectedOwner?.nome ?: ""
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onNavigateToRegisterVisit,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nova visita")
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
                    if (state.selectedOwner?.isCurrentlyOffline() == true) {
                        item {
                            OfflineAlertBanner(
                                onSetOnline = { viewModel.setOnline() }
                            )
                        }
                    }

                    if (state.allOwners.size > 1) {
                        item {
                            OwnerSwitcher(
                                selectedOwner = state.selectedOwner,
                                owners = state.allOwners,
                                onOwnerSelected = viewModel::selecionarMorador
                            )
                        }
                    }

                    if (state.allOwners.isEmpty()) {
                        item {
                            EmptyOwnersCard(onManageOwners = onNavigateToOwners)
                        }
                    }

                    item {
                        WelcomeCard(
                            ownerName = ownerName,
                            onRegisterVisit = onNavigateToRegisterVisit,
                            onScan = onNavigateToScanner
                        )
                    }

                    item {
                        StatsSection(
                            totalVisitsToday = state.totalVisitsToday,
                            activeVisitsCount = state.activeVisitsCount,
                            totalOwners = state.allOwners.size
                        )
                    }

                    item {
                        QuickActionsSection(
                            onScan = onNavigateToScanner,
                            onRegisterVisit = onNavigateToRegisterVisit,
                            onViewHistory = onNavigateToHistory,
                            onManageOwners = onNavigateToOwners
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Visitas Recentes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalButton(onClick = onNavigateToHistory) {
                                Text("Ver todas")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (state.recentVisits.isEmpty()) {
                        item {
                            EmptyVisitsCard(onRegister = onNavigateToRegisterVisit)
                        }
                    } else {
                        items(items = state.recentVisits, key = { it.id }) { visit ->
                            VisitItem(visit = visit)
                        }
                    }

                    item { AppSignature() }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun OwnerSwitcher(
    selectedOwner: br.com.porteirointeligente.domain.model.Owner?,
    owners: List<br.com.porteirointeligente.domain.model.Owner>,
    onOwnerSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentName = selectedOwner?.nome ?: "Selecione um morador"

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OwnerAvatar(
                    photoUri = selectedOwner?.photoUri,
                    name = currentName,
                    modifier = Modifier.size(40.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Morador ativo", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currentName, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, maxLines = 1)
                    selectedOwner?.apartamento?.takeIf { it.isNotBlank() }?.let {
                        Text("Apartamento $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Trocar morador",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            owners.forEach { owner ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(owner.nome, fontWeight = FontWeight.Medium)
                            Text("Apartamento ${owner.apartamento}", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    leadingIcon = {
                        if (owner.id == selectedOwner?.id) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        onOwnerSelected(owner.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyOwnersCard(onManageOwners: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Configure seu primeiro morador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Cadastre um morador para gerar o QR Code e começar a controlar as visitas.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onManageOwners, modifier = Modifier.padding(top = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cadastrar morador")
            }
        }
    }
}


@Composable
private fun StatsSection(totalVisitsToday: Int, activeVisitsCount: Int, totalOwners: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(icon = Icons.Default.CalendarMonth, value = "$totalVisitsToday", label = "Hoje", gradient = GradientGold, modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.Visibility, value = "$activeVisitsCount", label = "No local", gradient = GradientTeal, modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.People, value = "$totalOwners", label = "Moradores", gradient = GradientPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WelcomeCard(
    ownerName: String,
    onRegisterVisit: () -> Unit,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(GradientPrimary))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tudo sob controle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (ownerName.isBlank()) "Gerencie o acesso com praticidade" else "Acompanhe o acesso de $ownerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRegisterVisit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nova visita", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onScan,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ler QR")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, gradient: List<Color>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(108.dp), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(colors = gradient, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(1000f, 1000f))
            ).padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onScan: () -> Unit,
    onRegisterVisit: () -> Unit,
    onViewHistory: () -> Unit,
    onManageOwners: () -> Unit
) {
    Column {
        Text(text = "Ações Rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(icon = Icons.Default.QrCodeScanner, label = "Escanear QR", gradient = GradientNeon, onClick = onScan, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Default.Person, label = "Nova Visita", gradient = GradientTeal, onClick = onRegisterVisit, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(icon = Icons.Default.Groups, label = "Moradores", gradient = GradientPrimary, onClick = onManageOwners, modifier = Modifier.weight(1f))
                QuickActionCard(icon = Icons.Default.History, label = "Histórico", gradient = GradientGold, onClick = onViewHistory, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OfflineAlertBanner(onSetOnline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Você está ausente (Offline)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Leituras do QR Code exibirão sua mensagem offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSetOnline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "ONLINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, label: String, gradient: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(72.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = gradient[0], modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), textAlign = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

@Composable
private fun EmptyVisitsCard(onRegister: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(48.dp), tint = Slate400)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Nenhuma visita hoje", style = MaterialTheme.typography.titleMedium, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Registre a primeira visita do dia", style = MaterialTheme.typography.bodySmall, color = Slate400.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRegister, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Visita")
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(3) { ShimmerCard(modifier = Modifier.weight(1f)) } } }
        item { ShimmerCard(modifier = Modifier.fillMaxWidth()) }
        repeat(4) { item { ShimmerCard(modifier = Modifier.fillMaxWidth()) } }
    }
}

private fun getGreeting(): String {
    val cal = java.util.Calendar.getInstance()
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..5 -> "Boa madrugada"
        in 6..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
}

@Composable
private fun OwnerAvatar(
    photoUri: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(GradientGold)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Avatar do morador",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = if (name.isNotBlank()) {
                name.trim().split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")
            } else {
                "P"
            }
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
