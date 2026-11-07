# Análise de Cobertura de Testes — Porteiro Inteligente

## Cobertura existente

A suíte unitária do Android é executada com:

```bash
./gradlew testDebugUnitTest --no-daemon --console=plain
```

Ela cobre os ViewModels de início, moradores, gerenciamento (`OwnerManagementViewModel`), visitas,
scanner e ajustes; validações de formatação; `OfflineCryptoHelper`; `CryptoUtil`; restauração de
backup; neutralização de fórmulas no CSV; e os fluxos de QR/WhatsApp.

O backend possui testes de unidade e integração automatizados, executados com:

```bash
npm --prefix backend test
```

Eles verificam:
- Health check e detecção de disponibilidade do banco de dados SQLite.
- Exigência de token Bearer em rotas protegidas da API.
- Rejeição de enumeração de moradores por ID legado.
- Validação e decodificação do envelope híbrido QR v2 (RSA-OAEP SHA-256 + AES-256-GCM).
- Renderização de link do WhatsApp e mensagem de ausência offline (incluindo expiração via `offlineUntil`).
- Sanitização contra ataques XSS (escape de HTML em nome e mensagens).
- Rejeição de payloads adulterados (divergência entre ID da URL e payload criptografado, corrupção de bytes/tag).
- Comportamento de persistência segura e validação de caminhos efêmeros na Vercel.
- Resposta uniforme de login para usuário inexistente e senha incorreta.

## Lacunas conhecidas

- **Migrações do Room não são testadas.** O banco está na versão 9 com migrações 6→7, 7→8 e 8→9,
  mas não há `MigrationTestHelper` nem todos os schemas históricos versionados.
- **O payload real gerado pelo app não é testado contra a chave privada do backend.** Os testes de
  integração do backend geram o envelope com Node.js; o `OfflineCryptoHelper` do Android é testado
  separadamente com uma chave pública de teste.
- **`OfflineCryptoHelper.decryptOwnerData` retorna `null` em produção.** Os testes do scanner cobrem
  o caminho real (encaminhar o payload ao backend ou usar o cadastro local cujo `qrCodePayload`
  coincide), não uma decifragem local que o app não implementa.
- **`BackupManagerTest` usa criptografia identidade (mock).** Ele valida o fluxo de importação, não o
  formato criptográfico completo em um dispositivo real.
- **Sem testes de ownership/CRUD completos no backend** (criação, atualização e exclusão de moradores
  e visitas por usuário).
- Ainda faltam testes de migração da senha legada, logout e exclusão da conta do `AuthRepository` em
  JVM (já existem testes instrumentados em androidTest).
- Ainda faltam testes Compose/instrumentados de UI para navegação e seletor de arquivos.

## Concluído recentemente

- [x] Testes de integração do QR v2 (envelope híbrido RSA-OAEP + AES-GCM) entre o padrão do app e a API Node.js.
- [x] Testes de escape HTML, integridade criptográfica e expiração de modo offline.
- [x] Testes unitários para `OfflineCryptoHelper` e `OwnerManagementViewModel`.
- [x] Geração e pareamento de chaves RSA de ambiente local em `.env.local` e `OfflineCryptoHelper.kt`.
- [x] Teste de neutralização de fórmulas no relatório CSV.
- [x] Teste de resposta uniforme de login no backend.
