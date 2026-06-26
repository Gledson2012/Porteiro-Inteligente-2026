package br.com.porteirointeligente.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// ============================================================
// Rotas Type-Safe (Navigation Compose 2.8+)
// ============================================================

@Serializable
object Splash

@Serializable
object Home

@Serializable
object History

@Serializable
object Settings

@Serializable
object Scanner

@Serializable
object VisitRegistration

@Serializable
data class Cadastro(val ownerId: Long = 0L)

@Serializable
object QrCodeDisplay

@Serializable
object OwnerManagement

// Auth routes
@Serializable
object Login

@Serializable
object Register

// ============================================================
// Configuração da Bottom Navigation Bar
// ============================================================

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Histórico", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("QR Code", Icons.Filled.QrCode, Icons.Outlined.QrCode),
    BottomNavItem("Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
)

/** Mapeia um BottomNavItem para sua rota type-safe correspondente */
fun BottomNavItem.toRoute(): Any = when (this) {
    bottomNavItems[0] -> Home
    bottomNavItems[1] -> History
    bottomNavItems[2] -> QrCodeDisplay
    bottomNavItems[3] -> Settings
    else -> Home
}
