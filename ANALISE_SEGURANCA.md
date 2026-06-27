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
-   ✅ QR Code não contém dados pessoais visíveis — apenas URL com payload criptografado
-   ✅ Botão "Compartilhar" usa `FileProvider` com URI temporária — sem expor arquivos
-   ✅ Indicador visual "Protegido pela LGPD" na tela do QR Code
-   ✅ **Backup JSON criptografado**: Implementado via `CryptoUtil.encrypt()` usando AES/GCM/NoPadding com chaves geradas no Keystore local do dispositivo.
-   ✅ **Página Premium Intermediária**: A rota pública `/scan` do backend apresenta uma interface visual segura para o entregador iniciar a conversa no WhatsApp clicando no botão de ação, ocultando o telefone em texto plano na página.

### Riscos ⚠️
O QR Code offline usa criptografia AES com chave estática para permitir a decodificação sem banco de dados na Vercel. Embora os dados não estejam legíveis em texto puro, um atacante em posse da chave estática hardcoded do repositório pode extrair o nome e telefone de um QR Code impresso.

| Risco | Gravidade | Recomendação |
|-------|-----------|--------------|
| QR Code com chave simétrica estática | **Média** | Ofuscar chaves em builds de release (ProGuard/DexGuard) e orientar moradores sobre a guarda do QR Code físico |
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
- [x] Autenticação JWT com bcrypt (Backend)
- [x] Verificação de ownership (usuário só acessa seus dados no Backend)
- [x] LGPD: QR Code sem dados pessoais visíveis no payload bruto
- [x] FileProvider com URIs grant temporárias (Compartilhamento seguro)
- [x] CORS configurado no backend
- [x] Foreign Keys com ON DELETE CASCADE no SQLite
- [x] Backup JSON criptografado com chave Keystore local

### ❌ Pendente / Ações Futuras
- [ ] Ofuscação de chaves estáticas offline em builds de release (ProGuard/R8)
- [ ] Opção "Excluir meus dados" (LGPD/GDPR) no menu de Ajustes
- [ ] HTTPS forçado em produção no backend (Vercel)
- [ ] Rate limiting no login (Caso implemente sincronização online futuramente)
- [ ] Refresh token JWT (Caso implemente sincronização online futuramente)
- [ ] Logging OkHttp adaptativo (Caso adicione chamadas de rede futuramente)
