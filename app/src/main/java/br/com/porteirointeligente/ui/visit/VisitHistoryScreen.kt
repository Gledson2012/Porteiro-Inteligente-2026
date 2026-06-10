package br.com.porteirointeligente.ui.visit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Histórico de Visitas") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FilterChips(onFilterSelected = { viewModel.setFilter(it) })
            
            if (visits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma visita encontrada.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
fun FilterChips(onFilterSelected: (VisitHistoryViewModel.Filter) -> Unit) {
    var selectedFilter = VisitHistoryViewModel.Filter.ALL // Idealmente viria do ViewModel
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = true, // Simplificado para o exemplo
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ALL) },
            label = { Text("Todas") }
        )
        FilterChip(
            selected = false,
            onClick = { onFilterSelected(VisitHistoryViewModel.Filter.ACTIVE) },
            label = { Text("Ativas") }
        )
    }
}

@Composable
fun HistoryVisitItem(visit: Visit, onRegistrarSaida: (Visit) -> Unit) {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Entrada: ${sdf.format(Date(visit.dataEntrada))}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (visit.dataSaida != null) {
                    Text(
                        text = "Saída: ${sdf.format(Date(visit.dataSaida))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            if (visit.status == VisitStatus.ENTRADA_REGISTRADA) {
                IconButton(onClick = { onRegistrarSaida(visit) }) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Registrar Saída",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
