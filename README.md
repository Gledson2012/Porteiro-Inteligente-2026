# Porteiro Inteligente 2026

<p align="center">
  <a href="https://github.com/Gledson2012/Porteiro-Inteligente-2026/releases/tag/v0.4.0"><img src="https://img.shields.io/badge/Release-v0.4.0-FFB000?style=for-the-badge&logo=android&logoColor=black" alt="Release v0.4.0"></a>
  <a href="https://porteiro-inteligente-2026.vercel.app/"><img src="https://img.shields.io/badge/Demo%20Online-Vercel-black?style=for-the-badge&logo=vercel" alt="Vercel Demo"></a>
  <img src="https://img.shields.io/badge/Kotlin-2.0-purple?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-BOM%202024.09-brightgreen?style=for-the-badge&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/Target%20SDK-35-green?style=for-the-badge&logo=android" alt="Target SDK 35">
  <img src="https://img.shields.io/badge/LGPD-100%25%20Offline-blue?style=for-the-badge&logo=shield" alt="LGPD">
</p>

Aplicativo Android nativo para gestão de portaria em condomínios e controle de acessos 100% offline. Moradores cadastram seu perfil, geram QR Codes criptografados com contato direto no WhatsApp, e a portaria registra acessos instantaneamente.

**Experimente online**: [porteiro-inteligente-2026.vercel.app](https://porteiro-inteligente-2026.vercel.app/)  
**Download do APK**: [PorteiroInteligente.apk (v0.4.0)](https://github.com/Gledson2012/Porteiro-Inteligente-2026/releases/tag/v0.4.0)

---

## 📱 Navegação Principal

| Aba | Descrição |
|-----|-----------|
| **Início** | Visão geral com status em tempo real do morador (Disponível/Ausente), QR Code dinâmico, estatísticas consolidadas e check-out rápido de visitas recentes. |
| **Histórico** | Livro de visitas digital com busca textual instantânea, filtros por categoria, status (Ativos/Finalizados), veículos com placa Mercosul e exportação em CSV. |
| **QR Code** | Geração e pré-visualização ao vivo do QR Code de acesso, modal de zoom em alta definição e download de PNG para impressão de placas. |
| **Ajustes** | Tema (Claro/Escuro/Sistema), Biometria/Face ID, Modo Ausência com atalhos de tempo (1h, 4h, até amanhã), Limpeza Segura de histórico e Backup JSON cifrado. |

### 🚀 Recursos em Destaque

- **Criptografia Local de Ponta a Ponta**: AES-256 no Keystore e payload de QR Code híbrido RSA-2048 + AES-GCM.
- **Autenticação Biométrica**: Acesso protegido por impressão digital ou reconhecimento facial (`BiometricPrompt`).
- **Placas de Veículos (Mercosul)**: Suporte completo a veículos com validação de formato e badges com padrão visual oficial.
- **Modo Ausente Inteligente**: Permite configurar duração rápida e mensagem customizada para entregadores sem expor o WhatsApp diretamente na placa física.
- **Exportação de Relatórios em CSV**: Histórico de portaria compartilhável via WhatsApp, e-mail ou nuvem.
- **Proteção Visual contra Captura (`FLAG_SECURE`)**: Prevenção de prints e gravações de tela para máxima privacidade dos condôminos.
- **Feedback Sensorial Háptico e Sonoro**: Vibração e bip sonoro ao reconhecer QR Codes com sucesso.
- **PWA & Simulador Web Interativo**: Simulador 1:1 na web com Service Worker offline e PWA instalável.

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
| **Banco local** | Room + AES-GCM por campo (chave no Android Keystore) |
| **Câmera** | CameraX (Preview + ImageAnalysis) |
| **QR Code** | ZXing (geração) + CameraX Analyzer (leitura) |
| **Imagens** | Coil (AsyncImage) |
| **Tema persistente** | DataStore Preferences |
| **Backup** | Gson + AES-GCM/PBKDF2 |
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
│   │   │   ├── AppDatabase.kt              # Room Database (v9, suporte a placas Mercosul)
│   │   │   ├── LocalDataStore.kt           # Transações de backup/exclusão
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
│   │   ├── CryptoUtil.kt                   # Compatibilidade com payload legado local
│   │   ├── OfflineCryptoHelper.kt          # QR híbrido RSA/AES-GCM
│   │   ├── KeyDerivation.kt                # PBKDF2 para backup e senha local
│   │   ├── LocalDataCrypto.kt              # AES-GCM dos campos pessoais locais
│   │   ├── FeedbackHelper.kt               # Vibração tátil e áudio para scanner
│   │   ├── BiometricHelper.kt              # Autenticação biométrica / Face ID
│   │   ├── VisitReportExporter.kt          # Exportador de relatórios em CSV
│   │   ├── PhotoSaver.kt                   # Salvar QR na galeria
│   │   └── BackupManager.kt                # Backup .pib cifrado
│   └── ui/
│       ├── theme/                          # Color, Theme, Shape, Type
│       ├── navigation/NavGraph.kt          # Bottom nav + rotas
│       ├── components/
│       │   ├── VisitItem.kt                # Card de visita modular com placa
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
git clone https://github.com/Gledson2012/Porteiro-Inteligente-2026.git
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
App salva o morador e gera QR Code v2 (RSA-OAEP + AES-GCM)
       ↓
Entregador escaneia com a câmera do app ou navegador
       ↓
O app usa o cadastro local; a página web usa QR_PRIVATE_KEY no backend
       ↓
WhatsApp é aberto com mensagem padrão
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
| `ownerId` | `Long?` | Morador associado; visitas legadas ambíguas permanecem sem vínculo |

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

## Backend e publicação

O Express em `backend/` é usado pela página pública `/scan` e pelas rotas autenticadas opcionais.
Copie `.env.example`, configure `SECRET_KEY` e a chave privada RSA correspondente à chave pública
embutida em `OfflineCryptoHelper.kt` como `QR_PRIVATE_KEY`. Nunca versione a chave privada.

O APK continua deliberadamente offline e não sincroniza automaticamente com essas rotas REST. O
backend é independente e só deve ser habilitado quando houver necessidade de publicação do QR ou de
uma futura integração de sincronização.

O backend não usa mais o ID do QR como fallback público: somente payloads criptograficamente válidos
são aceitos. QR v2 não precisa consultar o banco quando `QR_PRIVATE_KEY` está configurada.

O SQLite local da Vercel é efêmero. Por segurança, as rotas de cadastro/login, moradores e visitas
respondem `503` quando executadas na Vercel sem `DATABASE_PATH` persistente explicitamente configurado.
Use um armazenamento persistente compatível antes de habilitar esse fluxo; `ALLOW_EPHEMERAL_DATABASE`
deve ficar restrito a desenvolvimento/testes.

---

##   Contribuição

1. `git checkout -b feature/nova-feature`
2. Faça commits descritivos
3. Abra um Pull Request

---

##   Licença

Uso privado — todos os direitos reservados.
