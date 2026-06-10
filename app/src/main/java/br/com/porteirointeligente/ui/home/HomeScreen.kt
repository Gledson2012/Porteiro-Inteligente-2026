package br.com.porteirointeligente.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.ui.owner.OwnerDetailsActivity
import br.com.porteirointeligente.ui.owner.OwnerRegistrationActivity
import br.com.porteirointeligente.ui.scanner.ScannerActivity
import br.com.porteirointeligente.ui.visit.VisitRegistrationActivity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val condominio by viewModel.condominio.collectAsState()
    val apartamento by viewModel.apartamento.collectAsState()
    val visitas by viewModel.visitasRecentes.collectAsState()
    val morador by viewModel.moradorCadastrado.collectAsState()

    // Configuração inicial se estiver vazio (apenas para exemplo)
    if (condominio.isEmpty()) {
        viewModel.configurarIdentificacao("Condomínio das Palmeiras", "Bloco A - 102")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                context.startActivity(Intent(context, VisitRegistrationActivity::class.java))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Visita")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(condominio, apartamento)
            }

            item {
                StatsCard(visitas.size)
            }

            item {
                ActionButtons(
                    hasMorador = morador != null,
                    onScannerClick = {
                        context.startActivity(Intent(context, ScannerActivity::class.java))
                    },
                    onProfileClick = {
                        if (morador != null) {
                            context.startActivity(Intent(context, OwnerDetailsActivity::class.java))
                        } else {
                            context.startActivity(Intent(context, OwnerRegistrationActivity::class.java))
                        }
                    }
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
        }
    }
}

@Composable
fun HomeHeader(condominio: String, apartamento: String) {
    Column {
        Text(
            text = "Bem-vindo,",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = condominio,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = apartamento,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StatsCard(count: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Visitas no Período",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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

@Composable
fun VisitItem(visit: Visit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = visit.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = visit.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Motivo: ${visit.motivo}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Entrada: ${sdf.format(Date(visit.dataEntrada))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
