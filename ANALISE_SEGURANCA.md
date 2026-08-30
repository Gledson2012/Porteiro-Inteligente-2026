# Análise de Segurança — Porteiro Inteligente

## Estado atual

- A autenticação local usa PBKDF2-HMAC-SHA256, salt aleatório, comparação em tempo constante e cifra o verificador no Android Keystore.
- Dados pessoais de moradores e visitas são cifrados por campo com AES-GCM e chave no Android Keystore; registros antigos em texto puro são migrados ao serem alterados.
- O JWT do backend só é emitido quando `SECRET_KEY` está configurada e tem pelo menos 32 caracteres. Login, cadastro e leitura pública de QR têm limitação de tentativas por processo.
- O QR novo usa envelope híbrido: AES-GCM com chave aleatória por QR e RSA-OAEP com uma chave pública no app. A chave privada fica somente em `QR_PRIVATE_KEY` no backend.
- Backups portáteis usam AES-GCM com chave derivada da senha escolhida pelo usuário, salt e IV aleatórios. A restauração valida e substitui moradores/visitas dentro de uma transação Room.
- `allowBackup` do Android está desativado e o `FileProvider` expõe apenas cache e arquivos internos.
- A API valida ownership de moradores/visitas, limita o corpo JSON, restringe CORS, envia headers de segurança e escapa valores antes de renderizar HTML.
- QR Codes legados sem payload criptográfico válido não são mais resolvidos por ID enumerável.
- O menu Ajustes oferece exclusão da conta, moradores e visitas locais.

## Compatibilidade legada

QR Codes AES emitidos por versões antigas podem ser lidos pelo backend somente quando `LEGACY_QR_KEY` estiver configurada. Essa chave é de migração e não deve ser usada para novos QR Codes; depois de reemitir os códigos, remova-a do ambiente.

## Pontos operacionais

1. Gere uma chave RSA própria para produção e configure a privada em `QR_PRIVATE_KEY`. Nunca comite essa chave.
2. Configure `SECRET_KEY` com um valor aleatório diferente por ambiente.
3. O SQLite em Vercel é recusado por padrão quando não há `DATABASE_PATH` persistente. Configure armazenamento persistente antes de habilitar as rotas autenticadas.
4. Mantenha o backend atrás de HTTPS; em `NODE_ENV=production` o Express redireciona requisições HTTP.
5. Após uma troca de chave RSA, reemita os QR Codes. QR Codes antigos continuam funcionando apenas enquanto o respectivo segredo legado estiver configurado.
6. O limite embutido é por processo; em produção distribuída, complemente-o com rate limit no gateway/WAF ou Redis.

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
- [x] Fallback público por ID removido
- [x] Cifra local de dados pessoais
- [x] Escape de HTML no `/scan`
- [x] CORS e headers de segurança
- [x] Exclusão local de dados
