const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { getDatabase } = require('./db');
const crypto = require('crypto');

const ENCRYPTION_KEY = 'PorteiroInteligente2026KeySecure'; // 32 characters
const ENCRYPTION_IV = '1234567890123456'; // 16 characters

function decryptPayload(text) {
  try {
    let base64 = text.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) {
      base64 += '=';
    }
    
    const decipher = crypto.createDecipheriv('aes-256-cbc', Buffer.from(ENCRYPTION_KEY), Buffer.from(ENCRYPTION_IV));
    let decrypted = decipher.update(base64, 'base64', 'utf8');
    decrypted += decipher.final('utf8');
    
    const json = JSON.parse(decrypted);
    return {
      phone: json.p,
      name: json.n,
      isOffline: json.o === 1,
      offlineMessage: json.m
    };
  } catch (e) {
    return null;
  }
}


const app = express();
const PORT = process.env.PORT || 3001;
const SECRET_KEY = process.env.SECRET_KEY || 'your-very-secret-key';

app.use(cors());
app.use(express.json());

// =====================
// Middleware de Autenticação
// =====================
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (token == null) {
    return res.status(401).json({ error: 'Token não fornecido' });
  }

  jwt.verify(token, SECRET_KEY, (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Token inválido' });
    }
    req.user = user;
    next();
  });
}

// =====================
// Rotas de Autenticação
// =====================

// POST /api/register - Registra um novo usuário
app.post('/api/register', (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password) {
      return res.status(400).json({ error: 'Usuário e senha são obrigatórios' });
    }

    const db = getDatabase();
    const existingUser = db.prepare('SELECT * FROM users WHERE username = ?').get(username);
    if (existingUser) {
      return res.status(409).json({ error: 'Usuário já existe' });
    }

    const hashedPassword = bcrypt.hashSync(password, 10);
    const stmt = db.prepare('INSERT INTO users (username, password) VALUES (?, ?)');
    const result = stmt.run(username, hashedPassword);

    res.status(201).json({ id: result.lastInsertRowid, username });
  } catch (err) {
    console.error('Erro ao registrar usuário:', err);
    res.status(500).json({ error: 'Erro ao registrar usuário' });
  }
});

// POST /api/login - Efetua login e retorna um token
app.post('/api/login', (req, res) => {
  try {
    const { username, password } = req.body;
    const db = getDatabase();
    const user = db.prepare('SELECT * FROM users WHERE username = ?').get(username);

    if (!user || !bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ error: 'Credenciais inválidas' });
    }

    const token = jwt.sign({ id: user.id, username: user.username }, SECRET_KEY, { expiresIn: '7d' });
    res.json({ token });
  } catch (err) {
    console.error('Erro ao efetuar login:', err);
    res.status(500).json({ error: 'Erro ao efetuar login' });
  }
});


// =====================
// QR Code Scan - Rota Pública
// =====================

app.get('/scan/:id_hash', (req, res) => {
  try {
    const { id_hash } = req.params;
    let name = '';
    let phone = '';
    let isOffline = false;
    let offlineMessage = '';
    
    // 1. Tenta descriptografar o payload offline primeiro (Novo Formato)
    const decrypted = decryptPayload(id_hash);
    if (decrypted) {
      name = decrypted.name;
      phone = decrypted.phone;
      isOffline = decrypted.isOffline;
      offlineMessage = decrypted.offlineMessage;
    } else {
      // 2. Fallback: busca clássica no banco sqlite (Formato Legado)
      const idPart = id_hash.split('_')[0];
      const ownerId = parseInt(idPart, 10);
      
      if (isNaN(ownerId)) {
        return res.status(400).send('QR Code Inválido');
      }
      
      const db = getDatabase();
      const owner = db.prepare('SELECT * FROM owners WHERE id = ?').get(ownerId);
      
      if (!owner) {
        return res.status(404).send('Morador Não Encontrado');
      }
      
      name = owner.nome;
      phone = owner.telefone;
      isOffline = owner.isOffline === 1;
      offlineMessage = owner.offlineMessage;
    }
    
    // Formata o número do WhatsApp
    let cleanPhone = phone.replace(/\D/g, '');
    if (!cleanPhone.startsWith('55')) {
      cleanPhone = '55' + cleanPhone;
    }
    
    const messageText = `Olá ${name.split(' ')[0]}, sou o entregador e estou na portaria.`;
    const waUrl = `https://wa.me/${cleanPhone}?text=${encodeURIComponent(messageText)}`;
    
    // Renderiza a página HTML premium intermediária (para apresentar as opções online/offline)
    const html = `<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Porteiro Inteligente - Contato</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #08090D;
            --card-bg: rgba(255, 255, 255, 0.025);
            --primary: #6366F1;
            --primary-glow: rgba(99, 102, 241, 0.15);
            --success: #10B981;
            --success-glow: rgba(16, 185, 129, 0.15);
            --danger: #EF4444;
            --danger-glow: rgba(239, 68, 68, 0.15);
            --text: #F8FAFC;
            --text-secondary: #94A3B8;
            --gold: #FFD700;
        }
        
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Outfit', sans-serif;
            background-color: var(--bg-color);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            overflow-x: hidden;
            position: relative;
        }

        /* Animated Soft Glow Lights */
        .glow-1 {
            position: absolute;
            top: -20%;
            right: -10%;
            width: 70%;
            height: 70%;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(255, 215, 0, 0.08) 0%, transparent 70%);
            z-index: 1;
            pointer-events: none;
            animation: float1 10s infinite alternate ease-in-out;
        }

        .glow-2 {
            position: absolute;
            bottom: -20%;
            left: -10%;
            width: 70%;
            height: 70%;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
            z-index: 1;
            pointer-events: none;
            animation: float2 12s infinite alternate ease-in-out;
        }

        @keyframes float1 {
            0% { transform: translate(0px, 0px) scale(1); }
            50% { transform: translate(40px, -60px) scale(1.15); }
            100% { transform: translate(0px, 0px) scale(1); }
        }

        @keyframes float2 {
            0% { transform: translate(0px, 0px) scale(1.15); }
            50% { transform: translate(-50px, 40px) scale(0.9); }
            100% { transform: translate(0px, 0px) scale(1.15); }
        }

        .container {
            width: 100%;
            max-width: 440px;
            z-index: 10;
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        /* App Logo Header */
        .logo-container {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 28px;
            animation: fadeInUp 0.8s ease-out;
        }

        .logo-icon {
            width: 44px;
            height: 44px;
            background: linear-gradient(135deg, #FFBF00, #FFD700);
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 8px 24px rgba(255, 215, 0, 0.25);
        }

        .logo-icon svg {
            width: 24px;
            height: 24px;
            fill: #0A0B10;
        }

        .logo-text {
            font-size: 22px;
            font-weight: 900;
            letter-spacing: -0.5px;
            background: linear-gradient(135deg, #FFFFFF, #94A3B8);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        /* Glassmorphic Card */
        .card {
            width: 100%;
            background: var(--card-bg);
            border: 1px solid rgba(255, 255, 255, 0.06);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border-radius: 30px;
            padding: 38px 28px;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), inset 0 1px 1px rgba(255, 255, 255, 0.1);
            animation: cardEntrance 0.8s cubic-bezier(0.16, 1, 0.3, 1);
        }

        @keyframes cardEntrance {
            from {
                opacity: 0;
                transform: translateY(30px) scale(0.96);
            }
            to {
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }

        /* Delivery Welcome Tag */
        .welcome-badge {
            background: rgba(255, 255, 255, 0.04);
            border: 1px solid rgba(255, 255, 255, 0.05);
            padding: 6px 14px;
            border-radius: 100px;
            font-size: 11px;
            font-weight: 700;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 1.5px;
            margin-bottom: 24px;
            display: inline-block;
        }

        /* Status Badge */
        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 6px 16px;
            border-radius: 100px;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 24px;
            letter-spacing: 0.5px;
        }

        .status-badge.online {
            background-color: rgba(16, 185, 129, 0.08);
            color: var(--success);
            border: 1px solid rgba(16, 185, 129, 0.15);
        }

        .status-badge.offline {
            background-color: rgba(239, 68, 68, 0.08);
            color: var(--danger);
            border: 1px solid rgba(239, 68, 68, 0.15);
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
        }

        .status-badge.online .status-dot {
            background-color: var(--success);
            box-shadow: 0 0 10px var(--success);
            animation: pulseDot 2s infinite;
        }

        .status-badge.offline .status-dot {
            background-color: var(--danger);
        }

        @keyframes pulseDot {
            0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5); }
            70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
            100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }

        h1 {
            font-size: 32px;
            font-weight: 800;
            margin-bottom: 8px;
            color: #FFFFFF;
            letter-spacing: -0.5px;
            line-height: 1.2;
        }

        .info-subtitle {
            font-size: 15px;
            color: var(--text-secondary);
            margin-bottom: 32px;
            line-height: 1.55;
            font-weight: 400;
        }

        /* Premium Buttons */
        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            width: 100%;
            padding: 18px 24px;
            border-radius: 20px;
            font-size: 16px;
            font-weight: 700;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            border: none;
            outline: none;
        }

        .btn-whatsapp {
            background: linear-gradient(135deg, #10B981, #059669);
            color: #FFFFFF;
            box-shadow: 0 10px 25px rgba(16, 185, 129, 0.3);
            animation: pulseBtn 3s infinite;
        }

        .btn-whatsapp:hover {
            transform: translateY(-3px);
            box-shadow: 0 15px 30px rgba(16, 185, 129, 0.45);
        }

        .btn-whatsapp:active {
            transform: translateY(-1px);
        }

        @keyframes pulseBtn {
            0% { transform: scale(1); }
            50% { transform: scale(1.02); }
            100% { transform: scale(1); }
        }

        /* Offline Instructions Box */
        .offline-box {
            background-color: rgba(255, 255, 255, 0.015);
            border: 1px dashed rgba(255, 255, 255, 0.08);
            border-radius: 22px;
            padding: 24px;
            margin-bottom: 12px;
            text-align: left;
            box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2);
        }

        .offline-box-header {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;
            font-weight: 700;
            color: var(--text-secondary);
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .offline-box-header svg {
            width: 14px;
            height: 14px;
            fill: var(--text-secondary);
        }

        .offline-box-content {
            font-size: 16px;
            color: #FFFFFF;
            line-height: 1.6;
            font-weight: 500;
            font-style: italic;
        }

        /* Staggered Entrance Animations */
        .animate-item {
            animation: fadeInUp 0.6s both;
        }

        .item-1 { animation-delay: 0.15s; }
        .item-2 { animation-delay: 0.25s; }
        .item-3 { animation-delay: 0.35s; }
        .item-4 { animation-delay: 0.45s; }
        .item-5 { animation-delay: 0.55s; }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(15px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Footer */
        .footer {
            margin-top: 36px;
            font-size: 12px;
            color: rgba(255, 255, 255, 0.2);
            text-align: center;
            letter-spacing: 0.5px;
            line-height: 1.6;
            animation: fadeInUp 0.8s both 0.65s;
        }
    </style>
</head>
<body>
    <div class="glow-1"></div>
    <div class="glow-2"></div>
    <div class="container">
        <div class="logo-container">
            <div class="logo-icon">
                <svg viewBox="0 0 24 24">
                    <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                </svg>
            </div>
            <span class="logo-text">Porteiro Inteligente</span>
        </div>

        <div class="card">
            <div class="welcome-badge animate-item item-1">ENTREGAS & PORTARIA</div>
            
            ${isOffline ? `
                <div class="status-badge offline animate-item item-2">
                    <div class="status-dot"></div>
                    <span>MORADOR INDISPONÍVEL</span>
                </div>
                <h1 class="animate-item item-3">${name}</h1>
                <p class="info-subtitle animate-item item-4">O morador configurou o status para indisponível no momento.</p>
                
                <div class="offline-box animate-item item-5">
                    <div class="offline-box-header">
                        <svg viewBox="0 0 24 24">
                            <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
                        </svg>
                        <span>Instruções Importantes</span>
                    </div>
                    <div class="offline-box-content">"${offlineMessage || 'Não posso atender no momento. Por favor, deixe a encomenda na portaria ou com o zelador.'}"</div>
                </div>
            ` : `
                <div class="status-badge online animate-item item-2">
                    <div class="status-dot"></div>
                    <span>MORADOR DISPONÍVEL</span>
                </div>
                <h1 class="animate-item item-3">${name}</h1>
                <p class="info-subtitle animate-item item-4">Para notificar sua chegada e realizar a entrega com segurança, clique no botão abaixo e fale direto com o morador no WhatsApp.</p>
                
                <a href="${waUrl}" class="btn btn-whatsapp animate-item item-5">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" style="vertical-align: middle; margin-right: 8px;">
                        <path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946C.06 5.348 5.397.01 12.008.01c3.202.001 6.212 1.246 8.477 3.514 2.266 2.268 3.507 5.28 3.505 8.484-.004 6.657-5.34 11.997-11.953 11.997-2.005-.001-3.973-.502-5.724-1.455L0 24zm6.59-4.846c1.62.963 3.41 1.47 5.259 1.471h.005c5.479 0 9.934-4.455 9.938-9.94.002-2.656-1.03-5.153-2.906-7.03C17.068 1.779 14.578.749 11.921.75 6.444.75 1.99 5.201 1.987 10.686c-.001 1.918.504 3.791 1.464 5.422l-1.012 3.7 3.794-.995zm11.516-7.8c-.313-.156-1.85-.913-2.138-1.017-.288-.105-.497-.156-.706.156-.208.312-.806 1.017-.988 1.225-.182.208-.364.234-.677.078-.312-.156-1.32-.486-2.514-1.55-.928-.827-1.554-1.85-1.737-2.162-.182-.313-.02-.482.137-.637.14-.14.312-.364.469-.546.156-.182.208-.312.312-.52.105-.208.052-.39-.026-.546-.078-.156-.706-1.7-.967-2.327-.254-.61-.513-.526-.706-.536-.183-.01-.39-.011-.597-.011-.208 0-.547.078-.833.39-.286.312-1.094 1.067-1.094 2.602 0 1.536 1.12 3.018 1.276 3.226.156.208 2.2 3.36 5.33 4.715.744.322 1.326.515 1.78.659.748.237 1.429.204 1.968.123.6-.09 1.85-.755 2.11-1.485.26-.73.26-1.353.182-1.485-.078-.13-.286-.208-.6-.364z"/>
                    </svg>
                    <span>Iniciar conversa no WhatsApp</span>
                </a>
            `}
        </div>

        <div class="footer">
            Porteiro Inteligente &copy; 2026<br>
            By Família Venâncio
        </div>
    </div>
</body>
</html>`;
    
    res.setHeader('Content-Type', 'text/html; charset=UTF-8');
    res.send(html);

  } catch (err) {
    console.error('Erro ao processar scan:', err);
    res.status(500).send('Erro Interno');
  }
});

// =====================
// Rotas de Moradores (Protegidas)
// =====================

// GET /api/owners - Lista moradores do usuário logado
app.get('/api/owners', authenticateToken, (req, res) => {
  try {
    const db = getDatabase();
    const owners = db.prepare('SELECT * FROM owners WHERE userId = ? ORDER BY nome ASC').all(req.user.id);
    res.json(owners);
  } catch (err) {
    res.status(500).json({ error: 'Erro ao buscar moradores' });
  }
});

// POST /api/owners - Cria um novo morador para o usuário logado
app.post('/api/owners', authenticateToken, (req, res) => {
  try {
    const db = getDatabase();
    const { nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil } = req.body;

    const stmt = db.prepare(`
      INSERT INTO owners (userId, nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const result = stmt.run(
      req.user.id, nome, nomeCondominio, endereco, cep, apartamento, telefone,
      photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil
    );

    res.status(201).json({ id: result.lastInsertRowid, ...req.body });
  } catch (err) {
    res.status(500).json({ error: 'Erro ao criar morador' });
  }
});

// PUT /api/owners/:id - Atualiza um morador do usuário logado
app.put('/api/owners/:id', authenticateToken, (req, res) => {
    try {
      const db = getDatabase();
      const { id } = req.params;
      const { nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil } = req.body;
  
      // Primeiro, verifique se o morador pertence ao usuário logado
      const owner = db.prepare('SELECT id FROM owners WHERE id = ? AND userId = ?').get(id, req.user.id);
      if (!owner) {
        return res.status(404).json({ error: 'Morador não encontrado ou não pertence a este usuário' });
      }
  
      const stmt = db.prepare(`
        UPDATE owners SET nome=?, nomeCondominio=?, endereco=?, cep=?, apartamento=?, telefone=?,
          photoUri=?, qrCodePayload=?, dataCadastro=?, isOffline=?, offlineMessage=?, offlineUntil=?
        WHERE id=? AND userId=?
      `);
  
      const result = stmt.run(
        nome, nomeCondominio, endereco, cep, apartamento, telefone,
        photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil,
        id, req.user.id
      );
  
      res.json({ id: parseInt(id), ...req.body });
    } catch (err) {
      res.status(500).json({ error: 'Erro ao atualizar morador' });
    }
});

// DELETE /api/owners/:id - Exclui um morador do usuário logado
app.delete('/api/owners/:id', authenticateToken, (req, res) => {
    try {
      const db = getDatabase();
      const { id } = req.params;
  
      // Primeiro, verifique se o morador pertence ao usuário logado
      const owner = db.prepare('SELECT id FROM owners WHERE id = ? AND userId = ?').get(id, req.user.id);
      if (!owner) {
        return res.status(404).json({ error: 'Morador não encontrado ou não pertence a este usuário' });
      }
  
      const result = db.prepare('DELETE FROM owners WHERE id = ? AND userId = ?').run(id, req.user.id);
  
      res.json({ message: 'Morador excluído com sucesso', id: parseInt(id) });
    } catch (err) {
      res.status(500).json({ error: 'Erro ao excluir morador' });
    }
});
  

// =====================
// Rotas de Visitas (Protegidas)
// =====================

// GET /api/visits - Lista visitas dos moradores do usuário logado
app.get('/api/visits', authenticateToken, (req, res) => {
  try {
    const db = getDatabase();
    // Este join garante que apenas visitas de moradores do usuário logado sejam retornadas
    const visits = db.prepare(`
        SELECT v.* FROM visits v
        JOIN owners o ON v.ownerId = o.id
        WHERE o.userId = ?
        ORDER BY v.dataEntrada DESC
    `).all(req.user.id);
    res.json(visits);
  } catch (err) {
    res.status(500).json({ error: 'Erro ao buscar visitas' });
  }
});

// POST /api/visits - Cria uma nova visita
app.post('/api/visits', authenticateToken, (req, res) => {
    try {
      const db = getDatabase();
      const { ownerId, nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status } = req.body;
      
      // Verifique se o ownerId pertence ao usuário logado
      const owner = db.prepare('SELECT id FROM owners WHERE id = ? AND userId = ?').get(ownerId, req.user.id);
      if (!owner) {
        return res.status(403).json({ error: 'Você não pode registrar visitas para este morador.' });
      }
  
      const stmt = db.prepare(`
        INSERT INTO visits (ownerId, nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `);
  
      const result = stmt.run(
        ownerId, nome, documento, apartamento, telefone, motivo,
        dataEntrada || Date.now(), dataSaida, status
      );
  
      res.status(201).json({ id: result.lastInsertRowid, ...req.body });
    } catch (err) {
      res.status(500).json({ error: 'Erro ao criar visita' });
    }
});
  

// PUT /api/visits/:id - Atualiza uma visita
app.put('/api/visits/:id', authenticateToken, (req, res) => {
    try {
        const db = getDatabase();
        const { id } = req.params;
        const { ownerId, nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status } = req.body;

        // Verificação de segurança: Garante que a visita a ser atualizada pertence a um morador do usuário logado
        const visit = db.prepare(`
            SELECT v.id FROM visits v
            JOIN owners o ON v.ownerId = o.id
            WHERE v.id = ? AND o.userId = ?
        `).get(id, req.user.id);

        if (!visit) {
            return res.status(404).json({ error: 'Visita não encontrada ou não pertence a este usuário' });
        }

        const stmt = db.prepare(`
            UPDATE visits SET ownerId=?, nome=?, documento=?, apartamento=?, telefone=?, motivo=?, dataEntrada=?, dataSaida=?, status=?
            WHERE id=?
        `);

        stmt.run(ownerId, nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status, id);

        res.json({ id: parseInt(id), ...req.body });
    } catch (err) {
        console.error('Erro ao atualizar visita:', err);
        res.status(500).json({ error: 'Erro ao atualizar visita' });
    }
});


// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`🏠 Porteiro Inteligente API rodando em http://localhost:${PORT}`);
});

module.exports = app;