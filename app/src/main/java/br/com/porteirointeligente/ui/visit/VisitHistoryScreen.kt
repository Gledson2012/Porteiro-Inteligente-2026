package br.com.porteirointeligente.ui.visit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    viewModel: VisitHistoryViewModel = hiltViewModel()
) {
    val visits by viewModel.visits.collectAsState()
    var currentFilter by remember { mutableStateOf(VisitHistoryViewModel.Filter.ALL) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Visitas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FilterChips(
                selectedFilter = currentFilter,
                onFilterSelected = { 
                    currentFilter = it
                    viewModel.setFilter(it) 
                }
            )
            
            if (visits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhuma visita encontrada.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visits) { visit ->
                        HistoryVisitItem(
                            visit = visit,
                            onRegistrarSaida = { viewModel.registrarSaida(it) }
                        )
                    }
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == VisitHistoryViewModel.Filter.ALL,
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ALL) },
            label = { Text("Todas") }
        )
        FilterChip(
            selected = selectedFilter == VisitHistoryViewModel.Filter.ACTIVE,
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ACTIVE) },
            label = { Text("Ativas") }
        )
    }
}

@Composable
fun HistoryVisitItem(visit: Visit, onRegistrarSaida: (Visit) -> Unit) {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apto: ${visit.apartamento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Entrada: ${sdf.format(Date(visit.dataEntrada))}",
                    style = MaterialTheme.typography.labelSmall
                )
                if (visit.dataSaida != null) {
                    Text(
                        text = "Saída: ${sdf.format(Date(visit.dataSaida))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (visit.status == VisitStatus.ENTRADA_REGISTRADA) {
                Button(
                    onClick = { onRegistrarSaida(visit) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("SAÍDA", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Concluída", style = MaterialTheme.typography.labelSmall) },
                    enabled = false
                )
            }
        }
    }
}
