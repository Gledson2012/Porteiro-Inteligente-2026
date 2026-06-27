package br.com.porteirointeligente.ui.visit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.ui.components.*
import br.com.porteirointeligente.ui.theme.Amber
import br.com.porteirointeligente.ui.theme.Emerald
import br.com.porteirointeligente.ui.theme.Rose
import br.com.porteirointeligente.ui.theme.Slate400
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: VisitHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentFilter by remember { mutableStateOf(VisitHistoryViewModel.Filter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Histórico de Visitas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    val hasVisits = (uiState as? VisitHistoryUIState.Success)?.visits?.isNotEmpty() ?: false
                    if (hasVisits) {
                        var showClearAllConfirm by remember { mutableStateOf(false) }
                        IconButton(onClick = { showClearAllConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar Histórico")
                        }
                        if (showClearAllConfirm) {
                            AlertDialog(
                                onDismissRequest = { showClearAllConfirm = false },
                                title = { Text("Limpar Histórico") },
                                text = { Text("Deseja apagar TODO o histórico de visitas? Esta ação é irreversível.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.clearAllVisits()
                                            showClearAllConfirm = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Limpar Tudo")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showClearAllConfirm = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRegister,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Visita")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Stats bar
            when (val state = uiState) {
                is VisitHistoryUIState.Success -> {
                    val activeCount = state.visits.count { it.status == VisitStatus.ENTRADA_REGISTRADA }
                    if (state.visits.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniStatCard(
                                value = "${state.visits.size}",
                                label = "Total",
                                color = MaterialTheme.colorScheme.primary
                            )
                            MiniStatCard(
                                value = "$activeCount",
                                label = "No local",
                                color = Emerald
                            )
                            MiniStatCard(
                                value = "${state.visits.count { it.status == VisitStatus.SAIDA_REGISTRADA }}",
                                label = "Concluídas",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                else -> {}
            }
            
            // Search Bar
            val hasVisits = (uiState as? VisitHistoryUIState.Success)?.visits?.isNotEmpty() ?: false
            if (hasVisits) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nome, apto, motivo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Filter chips
            FilterChips(
                selectedFilter = currentFilter,
                onFilterSelected = { 
                    currentFilter = it
                    viewModel.setFilter(it) 
                }
            )
            
            when (val state = uiState) {
                is VisitHistoryUIState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) {
                            ShimmerCard()
                        }
                    }
                }
                is VisitHistoryUIState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is VisitHistoryUIState.Success -> {
                    val visits = state.visits
                    val filteredVisits = visits.filter {
                        it.nome.contains(searchQuery, ignoreCase = true) ||
                        it.apartamento.contains(searchQuery, ignoreCase = true) ||
                        it.motivo.contains(searchQuery, ignoreCase = true) ||
                        it.documento.contains(searchQuery, ignoreCase = true) ||
                        it.telefone.contains(searchQuery, ignoreCase = true)
                    }

                    if (visits.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Nenhuma visita encontrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Registre uma visita para começar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    } else if (filteredVisits.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum resultado encontrado",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tente buscar por outro termo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredVisits) { visit ->
                                HistoryVisitItem(
                                    visit = visit,
                                    onRegistrarSaida = { viewModel.registrarSaida(it) },
                                    onDeleteVisit = { viewModel.deleteVisit(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MiniStatCard(
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: VisitHistoryViewModel.Filter,
    onFilterSelected: (VisitHistoryViewModel.Filter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == VisitHistoryViewModel.Filter.ALL,
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ALL) },
            label = { Text("Todas") },
            shape = RoundedCornerShape(10.dp)
        )
        FilterChip(
            selected = selectedFilter == VisitHistoryViewModel.Filter.ACTIVE,
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ACTIVE) },
            label = { Text("Ativas") },
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun StatusBadge(status: VisitStatus) {
    val (text, containerColor, contentColor) = when (status) {
        VisitStatus.ENTRADA_REGISTRADA -> Triple(
            "No Prédio",
            Emerald.copy(alpha = 0.12f),
            Emerald
        )
        VisitStatus.SAIDA_REGISTRADA -> Triple(
            "Concluída",
            Amber.copy(alpha = 0.12f),
            Amber
        )
        VisitStatus.CANCELADA -> Triple(
            "Cancelada",
            Rose.copy(alpha = 0.12f),
            Rose
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HistoryVisitItem(
    visit: Visit,
    onRegistrarSaida: (Visit) -> Unit,
    onDeleteVisit: (Visit) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    
    val statusColor = when (visit.status) {
        VisitStatus.ENTRADA_REGISTRADA -> Emerald
        VisitStatus.SAIDA_REGISTRADA -> Amber
        VisitStatus.CANCELADA -> Rose
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            // Color accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(statusColor.copy(alpha = 0.7f))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = visit.nome,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Apto: ${visit.apartamento}${if (visit.motivo.isNotBlank()) " • ${visit.motivo}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusBadge(visit.status)
                        
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Visita",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Excluir Visita") },
                                text = { Text("Tem certeza que deseja excluir esta visita do histórico?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteVisit(visit)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Excluir")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Entrada: ${sdf.format(Date(visit.dataEntrada))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (visit.dataSaida != null) {
                            Text(
                                text = "Saída: ${sdf.format(Date(visit.dataSaida))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (visit.status == VisitStatus.ENTRADA_REGISTRADA) {
                        FilledTonalButton(
                            onClick = { onRegistrarSaida(visit) },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = statusColor.copy(alpha = 0.12f),
                                contentColor = statusColor
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "SAÍDA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
