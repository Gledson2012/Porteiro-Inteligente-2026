# Porteiro Inteligente

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-BOM%202024.09-brightgreen?logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/Min%20SDK-23-orange?logo=android" alt="Min SDK 23">
  <img src="https://img.shields.io/badge/Target%20SDK-35-green?logo=android" alt="Target SDK 35">
  <img src="https://img.shields.io/badge/Architecture-MVVM-blue" alt="MVVM">
  <img src="https://img.shields.io/badge/DI-Hilt-cyan" alt="Hilt">
</p>

Aplicativo Android nativo para gestão de portaria em condomínios. Moradores cadastram seu perfil, geram QR Code de acesso, e entregadores escaneiam para contato via WhatsApp.

---

##   Funcionalidades

###   Navegação principal
| Aba | Descrição |
|-----|-----------|
| **Início** | Visão geral com boas-vindas, QR Code do morador, estatísticas do dia e visitas recentes |
| **Histórico** | Registro completo de visitas com filtro (Todas / Ativas) e ação de registrar saída |
| **Perfil/QR** | Cadastro e edição do morador, exibição do QR Code de acesso |
| **Ajustes** | Tema (Claro/Escuro/Sistema), Modo Offline com mensagem personalizada, Backup e Restauração de dados |

###   Recursos implementados

- **Cadastro de morador** com foto, nome, condomínio, endereço, CEP, apartamento e WhatsApp
- **QR Code dinâmico** — gerado automaticamente com payload criptografado para redirecionamento ao WhatsApp
- **Scanner de QR Code** com CameraX, lanterna e detecção em tempo real
- **Modo Offline** — permite configurar mensagem de ausência exibida ao escanear o QR Code
- **Registro de visitas** com nome, documento, apartamento, telefone e motivo
- **Histórico com filtros** — visitas ativas (no prédio) e concluídas
- **Backup e restauração** completo em JSON (perfil + visitas)
- **Tema dinâmico** Material You (Android 12+) opcional
- **Modo escuro** completo
- **Skeleton loading** animado no carregamento inicial
- **Snapshot de QR Code** para salvar na galeria

---

##   Capturas de Tela

| Início | Histórico | Perfil | Ajustes | Scanner |
|--------|-----------|--------|---------|---------|
| *(screenshot)* | *(screenshot)* | *(screenshot)* | *(screenshot)* | *(screenshot)* |

---

##   Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 (Material You) |
| **Navegação** | Navigation Compose (Single Activity) |
| **Arquitetura** | MVVM com ViewModel + StateFlow |
| **Injeção** | Dagger Hilt + KSP |
| **Banco local** | Room |
| **Câmera** | CameraX (Preview + ImageAnalysis) |
| **QR Code** | ZXing (geração) + CameraX Analyzer (leitura) |
| **Imagens** | Coil (AsyncImage) |
| **Tema persistente** | DataStore Preferences |
| **Backup** | Gson (JSON export/import) |
| **SDK mínimo** | 23 (Android 6.0) |
| **SDK alvo** | 35 (Android 15) |

---

##   Projeto

```
app/
├── src/main/java/br/com/porteirointeligente/
│   ├── PorteiroInteligenteApp.kt          # @HiltAndroidApp
│   ├── MainActivity.kt                     # Single Activity
│   ├── AppViewModel.kt                     # Estado global do tema
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt              # Room Database (v6)
│   │   │   ├── dao/OwnerDao.kt             # CRUD morador
│   │   │   ├── dao/VisitDao.kt             # CRUD visitas
│   │   │   ├── entity/OwnerEntity.kt
│   │   │   └── entity/VisitEntity.kt
│   │   └── repository/
│   │       ├── OwnerRepository.kt
│   │       └── VisitRepository.kt
│   ├── di/AppModule.kt                     # Hilt module
│   ├── domain/model/
│   │   ├── Owner.kt
│   │   └── Visit.kt + VisitStatus
│   ├── util/
│   │   ├── ThemeManager.kt                 # DataStore tema
│   │   ├── QrCodeGenerator.kt
│   │   ├── QrCodeAnalyzer.kt               # CameraX analyzer
│   │   ├── CryptoUtil.kt                   # Criptografia payload
│   │   ├── PhotoSaver.kt                   # Salvar QR na galeria
│   │   └── BackupManager.kt               # JSON backup
│   └── ui/
│       ├── theme/                          # Color, Theme, Shape, Type
│       ├── navigation/NavGraph.kt          # Bottom nav + rotas
│       ├── components/
│       │   ├── VisitItem.kt                # Card de visita
│       │   └── ShimmerEffect.kt            # Skeleton loading
│       ├── home/HomeScreen.kt + ViewModel
│       ├── visit/VisitHistoryScreen.kt + ViewModel
│       ├── visit/VisitRegistrationScreen.kt + ViewModel
│       ├── owner/ProfileScreen.kt + ViewModels
│       ├── scanner/ScannerScreen.kt + ViewModel
│       └── settings/SettingsScreen.kt + ViewModel
└── src/main/res/
    ├── values/colors.xml, strings.xml, themes.xml
    └── values-night/themes.xml
```

---

## ⚙️ Configuração

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17+
- Android SDK 35
- Gradle 8.7 (wrapper incluso)

### Passos

```bash
git clone https://github.com/seu-usuario/Porteiro-Inteligente-2026.git
cd Porteiro-Inteligente-2026
./gradlew assembleDebug
```

Ou abra a pasta no Android Studio e clique em **Run**.

---

##   Arquitetura

O app segue o padrão **MVVM** com camadas bem definidas:

```
UI (Compose) → ViewModel → Repository → Room / DataStore
```

- **UI**: Telas em Compose observam `StateFlow` dos ViewModels
- **ViewModel**: Gerencia estado e lógica de apresentação
- **Repository**: Abstrai fonte de dados (Room)
- **Room**: Banco SQLite local com DAOs

### Fluxo do QR Code

```
Morador cadastra perfil
       ↓
App gera QR Code com payload criptografado (whatsapp://send?phone=...)
       ↓
Entregador escaneia com câmera do app
       ↓
App decodifica e abre WhatsApp com mensagem padrão
       ↓
Visita registrada no histórico
```

---

##   Modelos

### Visit

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Identificador único |
| `nome` | `String` | Nome do visitante |
| `documento` | `String` | Documento (RG/CPF) |
| `apartamento` | `String` | Unidade de destino |
| `telefone` | `String` | Contato |
| `motivo` | `String` | Motivo da visita |
| `dataEntrada` | `Long` | Epoch de entrada |
| `dataSaida` | `Long?` | Epoch de saída |
| `status` | `VisitStatus` | `ENTRADA_REGISTRADA`, `SAIDA_REGISTRADA` ou `CANCELADA` |

### Owner

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Identificador único |
| `nome` | `String` | Nome completo |
| `nomeCondominio` | `String` | Condomínio |
| `apartamento` | `String` | Unidade |
| `telefone` | `String` | WhatsApp |
| `endereco` | `String` | Endereço |
| `cep` | `String` | CEP |
| `photoUri` | `String?` | URI da foto de perfil |
| `qrCodePayload` | `String` | Payload do QR Code |
| `isOffline` | `Boolean` | Modo offline ativo |
| `offlineMessage` | `String` | Mensagem de ausência |
| `offlineUntil` | `Long?` | Data limite do modo offline |
| `dataCadastro` | `Long` | Epoch de cadastro |

---

##   Melhorias futuras

- [ ] Notificações push (Firebase Cloud Messaging)
- [ ] Sincronização com backend REST
- [ ] Múltiplos moradores por unidade
- [ ] Tour guiado na primeira execução
- [ ] Suporte a tablets com layout adaptativo
- [ ] Testes instrumentados (Compose Test)
- [ ] CI/CD com GitHub Actions
- [ ] Tradução para outros idiomas

---

##   Contribuição

1. `git checkout -b feature/nova-feature`
2. Faça commits descritivos
3. Abra um Pull Request

---

##   Licença

Uso privado — todos os direitos reservados.
