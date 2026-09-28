# Análise de Cobertura de Testes — Porteiro Inteligente

## Cobertura existente

A suíte unitária do Android é executada com:

```bash
./gradlew testDebugUnitTest --no-daemon --console=plain
```

Os testes cobrem os ViewModels de início, moradores, gerenciamento (`OwnerManagementViewModel`), visitas, scanner e ajustes; validações de formatação; `OfflineCryptoHelper`; `CryptoUtil`; restauração de backup; e os fluxos de QR/WhatsApp. A suíte também cobre as migrações de compilação e a associação de visitas por `ownerId` por meio dos ViewModels.

O backend possui testes de unidade e integração automatizados (13 testes, 100% de aprovação), executados com:

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

## Lacunas conhecidas

- Ainda faltam testes de migração da senha legada, logout e exclusão da conta do `AuthRepository` em JVM (já existem testes instrumentados em androidTest).
- Ainda faltam testes Compose/instrumentados de UI para navegação e seletor de arquivos.

## Concluído recentemente

- [x] Testes de integração do QR v2 (envelope híbrido RSA-OAEP + AES-GCM) entre o padrão do app e a API Node.js.
- [x] Testes de escape HTML, integridade criptográfica e expiração de modo offline.
- [x] Testes unitários para `OfflineCryptoHelper` e `OwnerManagementViewModel`.
- [x] Geração e pareamento de chaves RSA de ambiente local em `.env.local` e `OfflineCryptoHelper.kt`.
