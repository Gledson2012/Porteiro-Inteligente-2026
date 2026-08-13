# Análise de Segurança — Porteiro Inteligente

## Estado atual

- A autenticação local não salva senha em texto puro: usa PBKDF2-HMAC-SHA256, salt aleatório e comparação em tempo constante.
- O JWT do backend só é emitido quando `SECRET_KEY` está configurada e tem pelo menos 32 caracteres. Login e cadastro têm limitação de tentativas.
- O QR novo usa envelope híbrido: AES-GCM com chave aleatória por QR e RSA-OAEP com uma chave pública no app. A chave privada fica somente em `QR_PRIVATE_KEY` no backend.
- Backups portáteis usam AES-GCM com chave derivada da senha escolhida pelo usuário, salt e IV aleatórios. A restauração valida e substitui moradores/visitas dentro de uma transação Room.
- `allowBackup` do Android está desativado e o `FileProvider` expõe apenas cache e arquivos internos.
- A API valida ownership de moradores/visitas, limita o corpo JSON, restringe CORS, envia headers de segurança e escapa valores antes de renderizar HTML.
- O menu Ajustes oferece exclusão da conta, moradores e visitas locais.

## Compatibilidade legada

QR Codes AES emitidos por versões antigas podem ser lidos pelo backend somente quando `LEGACY_QR_KEY` estiver configurada. Essa chave é de migração e não deve ser usada para novos QR Codes; depois de reemitir os códigos, remova-a do ambiente.

## Pontos operacionais

1. Gere uma chave RSA própria para produção e configure a privada em `QR_PRIVATE_KEY`. Nunca comite essa chave.
2. Configure `SECRET_KEY` com um valor aleatório diferente por ambiente.
3. O SQLite em Vercel usa `/tmp`, que é efêmero. As rotas autenticadas precisam de um banco persistente antes de serem usadas em produção.
4. Mantenha o backend atrás de HTTPS; em `NODE_ENV=production` o Express redireciona requisições HTTP.
5. Após uma troca de chave RSA, reemita os QR Codes. QR Codes antigos continuam funcionando apenas enquanto o respectivo segredo legado estiver configurado.

## Checklist

- [x] Hash de senha local com salt
- [x] bcrypt no backend
- [x] JWT sem segredo hardcoded
- [x] Rate limit básico em login/cadastro
- [x] QR novo sem segredo simétrico no APK
- [x] AES-GCM e IV aleatório no QR novo
- [x] Backup portátil cifrado por senha
- [x] Migração Room não destrutiva
- [x] Ownership nas rotas da API
- [x] Escape de HTML no `/scan`
- [x] CORS e headers de segurança
- [x] Exclusão local de dados
