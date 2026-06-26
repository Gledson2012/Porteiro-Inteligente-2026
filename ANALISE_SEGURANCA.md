#   Análise de Segurança — Porteiro Inteligente

##   1. Autenticação JWT

### Pontos Fortes ✅
-   Senhas armazenadas com **bcrypt** (salt rounds = 10) no backend
-   Token JWT com **expiração de 7 dias** — reduz risco de token eterno
-   Token armazenado em **DataStore** (não SharedPreferences) — isolado por sandbox Android
-   Interceptor OkHttp injeta token automaticamente em todas as requisições

### Riscos ⚠️
| Risco | Gravidade | Recomendação |
|-------|-----------|--------------|
| `SECRET_KEY` hardcoded como fallback no `server.js` | **Alta** | Use variável de ambiente obrigatória, sem fallback |
| Token JWT não é refreshado | **Média** | Implemente refresh token com rota `/api/refresh` |
| Sem rate limiting no login | **Média** | Adicione `express-rate-limit` para evitar brute force |
| Logging interceptor OkHttp em `Level.BODY` em produção | **Alta** | Use `Level.HEADERS` ou `Level.NONE` em release |

### Recomendação para server.js:
```javascript
// Em vez de:
const SECRET_KEY = process.env.SECRET_KEY || 'your-very-secret-key';

// Faça:
const SECRET_KEY = process.env.SECRET_KEY;
if (!SECRET_KEY) {
    console.error('❌ SECRET_KEY environment variable is required');
    process.exit(1);
}
```

---

##   2. Criptografia do QR Code (AES/GCM + Android Keystore)

### Pontos Fortes ✅
-   ✅ Chave AES-256 armazenada no **Android Keystore** (hardware-backed em dispositivos compatíveis)
-   ✅ Algoritmo **AES/GCM/NoPadding** — autenticado (tag de 128 bits), resistente a tampering
-   ✅ **IV único** a cada operação de encrypt — mesmo texto cifra diferente
-   ✅ Base64 **URL Safe** — compatível com QR Codes e parâmetros de URL
-   ✅ Chave **nunca sai do dispositivo** — não é exportável

### Riscos ⚠️
| Risco | Gravidade | Recomendação |
|-------|-----------|--------------|
| `resetKey()` apaga chave sem notificar usuário | **Média** | Não expor no frontend; usar apenas como último recurso |
| Sem fallback para dispositivos sem Keystore | **Baixa** | Android 6.0+ tem Keystore; ok para minSdk 23 |
| QR Code pode conter URL de redirecionamento visível | **Baixa** | A URL é mascarada na UI; mas o payload criptografado protege o conteúdo |

---

##   3. Proteção LGPD

### Pontos Fortes ✅
-   ✅ QR Code não contém dados pessoais visíveis — apenas URL mascarada com ID
-   ✅ Botão "Compartilhar" usa `FileProvider` com URI temporária — sem expor arquivos
-   ✅ Indicador visual "Protegido pela LGPD" na tela do QR Code
-   ✅ `CryptoUtil.encrypt()` usado para ofuscar payload

### Riscos ⚠️
**Backup JSON exporta todos os dados sem criptografia** — dados pessoais (nome, telefone, endereço, documento) são exportados em texto puro no arquivo de backup.

| Risco | Gravidade | Recomendação |
|-------|-----------|--------------|
| Backup JSON sem criptografia | **Alta** | Criptografar o arquivo de backup com a chave do Keystore antes de exportar |
| Sem exclusão de dados do usuário | **Média** | Implementar funcionalidade "Excluir meus dados" (GDPR Art. 17) |

---

##   4. API Backend

### Riscos ⚠️
| Rota | Risco | Recomendação |
|------|-------|--------------|
| `GET /scan/:id_hash` (pública) | **Baixo** | Apenas consulta e redireciona; sem side effects |
| `POST /api/register` | **Médio** | Sem validação de força de senha |
| PUT/DELETE owners e visits | **Médio** | Já tem verificação de ownership (`WHERE userId = ?`) ✅ |
| Sem HTTPS forçado | **Alta** | Configurar redirecionamento HTTP→HTTPS no servidor |

### Recomendação para o servidor:
```javascript
// Forçar HTTPS em produção
if (process.env.NODE_ENV === 'production') {
    app.use((req, res, next) => {
        if (!req.secure) return res.redirect('https://' + req.headers.host + req.url);
        next();
    });
}
```

---

##   5. Checklist de Segurança

### ✅ Implementado
- [x] Criptografia AES-256 com chave no Keystore
- [x] Autenticação JWT com bcrypt
- [x] Verificação de ownership (usuário só acessa seus dados)
- [x] LGPD: QR Code sem dados pessoais visíveis
- [x] FileProvider com URIs grant temporárias
- [x] CORS configurado no backend
- [x] Foreign Keys com ON DELETE CASCADE no SQLite

### ❌ Pendente
- [ ] Rate limiting no login
- [ ] Refresh token JWT
- [ ] SECRET_KEY sem fallback
- [ ] Backup JSON criptografado
- [ ] Logging OkHttp adaptativo (BODY só em debug)
- [ ] HTTPS forçado em produção
- [ ] Opção "Excluir meus dados" (LGPD/GDPR)
