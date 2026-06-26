const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { getDatabase } = require('./db');

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
    
    if (owner.isOffline) {
      return res.status(200).send(`<h1>Morador Indisponível</h1><p>${owner.offlineMessage || 'O morador está temporariamente indisponível.'}</p>`);
    }
    
    let phone = owner.telefone.replace(/\D/g, '');
    if (!phone.startsWith('55')) {
      phone = '55' + phone;
    }
    
    const message = encodeURIComponent(`Olá ${owner.nome.split(' ')[0]}, sou o entregador e estou na portaria.`);
    const waUrl = `https://wa.me/${phone}?text=${message}`;
    
    res.redirect(waUrl);

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