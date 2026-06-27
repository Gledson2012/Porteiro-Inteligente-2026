#   Análise de Cobertura de Testes — Porteiro Inteligente

##   Testes Existentes (11 arquivos)

| Arquivo | Tipo | O que testa | Status |
|---------|------|-------------|--------|
| `HomeViewModelTest.kt` | Unitário | Carregamento de dados, seleção de morador | ✅ 2 testes |
| `VisitRegistrationViewModelTest.kt` | Unitário | Validação de campos obrigatórios, registro de visita | ✅ 3 testes |
| `SettingsViewModelTest.kt` | Unitário | Tema, cor dinâmica, backup, modo offline | ✅ 6 testes |
| `OwnerDetailsViewModelTest.kt` | Unitário | Estado Success/Empty, exclusão, seleção | ✅ 4 testes |
| `OwnerRegistrationViewModelTest.kt` | Unitário | CRUD de morador, validações | ✅ 7 testes |
| `StringUtilsTest.kt` | Unitário | Formatação de telefone e CEP | ✅ 7 testes |
| `CryptoUtilTest.kt` | Unitário | Comportamento sem Keystore (null safety) | ✅ 3 testes |
| `BackupManagerTest.kt` | Unitário | Restore com JSON válido/inválido | ✅ 3 testes |
| `VisitHistoryViewModelTest.kt` | Unitário | Filtros, registro de saída | ✅ 6 testes |
| `ScannerViewModelTest.kt` | Unitário | Processamento de QR Code (LGPD, wa.me, etc) | ✅ 4 testes |
| `SplashScreenTest.kt` | Instrumentado | UI da Splash (nome, subtítulo, tagline) | ✅ 3 testes |

**Total: 48 testes** (45 unitários + 3 instrumentados)

---

## ⚠️ Lacunas Identificadas

### ❌ NENHUM teste para as camadas de rede/API

| Classe | Risco | Prioridade |
|--------|-------|------------|
| `ApiService` (interface Retrofit) | **Alto** — endpoints podem estar desalinhados com o backend | ⭐⭐⭐ |
| `AuthRepository` | **Alto** — lógica de autenticação crítica sem cobertura | ⭐⭐⭐ |
| `OwnerRepository` | **Alto** — lógica de sync local+rede sem teste | ⭐⭐⭐ |
| `VisitRepository` | **Alto** — mesmo caso do OwnerRepository | ⭐⭐⭐ |
| `AuthInterceptor` | **Médio** — injeção de token JWT sem verificação | ⭐⭐ |
| `TokenManager` | **Médio** — persistência de token em DataStore | ⭐⭐ |

### ❌ ViewModels sem testes

| ViewModel | Funcionalidades | Prioridade |
|-----------|----------------|------------|
| `AuthViewModel` | Estado de autenticação root | ⭐⭐ |
| `LoginViewModel` | Login com credenciais | ⭐⭐ |
| `RegistrationViewModel` | Registro de usuário | ⭐⭐ |
| `OnboardingViewModel` | Fluxo de onboarding | ⭐ |

### ❌ Utilitários sem teste

| Utilitário | Prioridade |
|------------|------------|
| `QrCodeGenerator` | ⭐⭐ |
| `QrCodeAnalyzer` | ⭐⭐ (precisa de emulador) |
| `PhotoSaver` | ⭐ |
| `ViaCepClient` | ⭐⭐ |
| `OwnerSelectionManager` | ⭐⭐ |
| `ThemeManager` | ⭐ |

### ❌ Telas sem teste instrumental

| Tela | Prioridade |
|------|------------|
| `HomeScreen` | ⭐⭐⭐ |
| `ScannerScreen` | ⭐⭐ |
| `CadastroScreen` | ⭐⭐ |
| `VisitHistoryScreen` | ⭐⭐ |

---

##   Plano de Ação Recomendado

### Fase 1 — Imediata (Rede + Auth) ⭐⭐⭐
1. `AuthInterceptorTest` — Testar injeção de token e ausência de token
2. `TokenManagerTest` — Testar save, get, delete com DataStore
3. `AuthRepositoryTest` — Testar login sucesso, login falha, register, logout
4. `OwnerRepositoryTest` — Testar sync local+rede, fallback offline
5. `VisitRepositoryTest` — Testar sync local+rede, fallback offline

### Fase 2 — Curto Prazo (ViewModels críticas) ⭐⭐
6. `ScannerViewModelTest` — Testar detecção de QR Code (válido, inválido, offline) [Concluído] ✅
7. `VisitHistoryViewModelTest` — Testar filtros e registro de saída [Concluído] ✅
8. `LoginViewModelTest` — Testar fluxo de login
9. `RegistrationViewModelTest` — Testar fluxo de registro
10. `QrCodeGeneratorTest` — Testar geração de bitmap

### Fase 3 — Médio Prazo (UI instrumentada) ⭐
11. Testes Compose para `HomeScreen`, `CadastroScreen`, `ScannerScreen`, etc.
12. Testes de integração entre ViewModel e Repository com Hilt

---

##   Como executar os testes atuais

```bash
# Testes unitários
./gradlew test

# Testes instrumentados (requer emulador/dispositivo)
./gradlew connectedAndroidTest

# Cobertura (com Kover ou Jacoco)
./gradlew koverReport
```
