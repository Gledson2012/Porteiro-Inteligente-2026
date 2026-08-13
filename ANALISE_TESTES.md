# Análise de Cobertura de Testes — Porteiro Inteligente

## Cobertura existente

Há 53 testes unitários JVM e 3 testes instrumentados. A suíte unitária é executada com:

```bash
./gradlew testDebugUnitTest --no-daemon --console=plain
```

Os testes cobrem os ViewModels de início, moradores, visitas, scanner e ajustes; validações de
formatação; `CryptoUtil`; restauração de backup; e os fluxos de QR/WhatsApp. A suíte também cobre
as migrações de compilação e a nova associação de visitas por `ownerId` por meio dos ViewModels.

## Lacunas conhecidas

- `AuthRepository` ainda precisa de testes com DataStore real ou fake para login, migração da senha
  legada, logout e exclusão da conta.
- `OfflineCryptoHelper` precisa de um teste de interoperabilidade formal com a chave privada RSA
  do backend; a cadeia foi validada manualmente com Node durante a revisão.
- As rotas Express precisam de testes HTTP para ownership, rate limit, escape de HTML e validação
  de payloads.
- Ainda faltam testes Compose/instrumentados para navegação, exclusão de dados e restauração por
  seletor de arquivos.

## Próximas prioridades

1. Criar testes de integração do QR v2 entre Kotlin e Node.
2. Adicionar testes de API com banco temporário e segredo de teste.
3. Cobrir `AuthRepository` e `OwnerSelectionManager` com fakes de DataStore.
4. Adicionar testes instrumentados para backup, exclusão de dados e navegação após logout.
