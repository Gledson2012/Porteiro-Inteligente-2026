const express = require('express');
const cors = require('cors');
const { getDatabase } = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// =====================
// Rotas de Moradores
// =====================

// GET /api/owners - Lista todos os moradores
app.get('/api/owners', (req, res) => {
  try {
    const db = getDatabase();
    const owners = db.prepare('SELECT * FROM owners ORDER BY nome ASC').all();
    res.json(owners);
  } catch (err) {
    console.error('Erro ao buscar moradores:', err);
    res.status(500).json({ error: 'Erro ao buscar moradores' });
  }
});

// GET /api/owners/:id - Busca um morador por ID
app.get('/api/owners/:id', (req, res) => {
  try {
    const db = getDatabase();
    const owner = db.prepare('SELECT * FROM owners WHERE id = ?').get(req.params.id);
    if (!owner) {
      return res.status(404).json({ error: 'Morador não encontrado' });
    }
    res.json(owner);
  } catch (err) {
    console.error('Erro ao buscar morador:', err);
    res.status(500).json({ error: 'Erro ao buscar morador' });
  }
});

// POST /api/owners - Cria um novo morador
app.post('/api/owners', (req, res) => {
  try {
    const db = getDatabase();
    const { nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil } = req.body;

    if (!nome || !endereco || !apartamento || !telefone || !qrCodePayload) {
      return res.status(400).json({ error: 'Campos obrigatórios: nome, endereco, apartamento, telefone, qrCodePayload' });
    }

    const stmt = db.prepare(`
      INSERT INTO owners (nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const result = stmt.run(
      nome, nomeCondominio || '', endereco, cep || '', apartamento, telefone,
      photoUri || null, qrCodePayload, dataCadastro || Date.now(),
      isOffline ? 1 : 0, offlineMessage || '', offlineUntil || null
    );

    res.status(201).json({ id: result.lastInsertRowid, ...req.body });
  } catch (err) {
    console.error('Erro ao criar morador:', err);
    res.status(500).json({ error: 'Erro ao criar morador' });
  }
});

// PUT /api/owners/:id - Atualiza um morador
app.put('/api/owners/:id', (req, res) => {
  try {
    const db = getDatabase();
    const { id } = req.params;
    const { nome, nomeCondominio, endereco, cep, apartamento, telefone, photoUri, qrCodePayload, dataCadastro, isOffline, offlineMessage, offlineUntil } = req.body;

    const stmt = db.prepare(`
      UPDATE owners SET nome=?, nomeCondominio=?, endereco=?, cep=?, apartamento=?, telefone=?,
        photoUri=?, qrCodePayload=?, dataCadastro=?, isOffline=?, offlineMessage=?, offlineUntil=?
      WHERE id=?
    `);

    const result = stmt.run(
      nome, nomeCondominio || '', endereco, cep || '', apartamento, telefone,
      photoUri || null, qrCodePayload, dataCadastro || Date.now(),
      isOffline ? 1 : 0, offlineMessage || '', offlineUntil || null, id
    );

    if (result.changes === 0) {
      return res.status(404).json({ error: 'Morador não encontrado' });
    }

    res.json({ id: parseInt(id), ...req.body });
  } catch (err) {
    console.error('Erro ao atualizar morador:', err);
    res.status(500).json({ error: 'Erro ao atualizar morador' });
  }
});

// DELETE /api/owners/:id - Exclui um morador
app.delete('/api/owners/:id', (req, res) => {
  try {
    const db = getDatabase();
    const result = db.prepare('DELETE FROM owners WHERE id = ?').run(req.params.id);
    
    if (result.changes === 0) {
      return res.status(404).json({ error: 'Morador não encontrado' });
    }

    res.json({ message: 'Morador excluído com sucesso', id: parseInt(req.params.id) });
  } catch (err) {
    console.error('Erro ao excluir morador:', err);
    res.status(500).json({ error: 'Erro ao excluir morador' });
  }
});

// =====================
// Rotas de Visitas
// =====================

// GET /api/visits - Lista todas as visitas
app.get('/api/visits', (req, res) => {
  try {
    const db = getDatabase();
    const visits = db.prepare('SELECT * FROM visits ORDER BY dataEntrada DESC').all();
    res.json(visits);
  } catch (err) {
    console.error('Erro ao buscar visitas:', err);
    res.status(500).json({ error: 'Erro ao buscar visitas' });
  }
});

// POST /api/visits - Cria uma nova visita
app.post('/api/visits', (req, res) => {
  try {
    const db = getDatabase();
    const { nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status } = req.body;

    if (!nome || !apartamento) {
      return res.status(400).json({ error: 'Campos obrigatórios: nome, apartamento' });
    }

    const stmt = db.prepare(`
      INSERT INTO visits (nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const result = stmt.run(
      nome, documento || '', apartamento, telefone || '', motivo || '',
      dataEntrada || Date.now(), dataSaida || null, status || 'ENTRADA_REGISTRADA'
    );

    res.status(201).json({ id: result.lastInsertRowid, ...req.body });
  } catch (err) {
    console.error('Erro ao criar visita:', err);
    res.status(500).json({ error: 'Erro ao criar visita' });
  }
});

// PUT /api/visits/:id - Atualiza uma visita (ex: registrar saída)
app.put('/api/visits/:id', (req, res) => {
  try {
    const db = getDatabase();
    const { id } = req.params;
    const { nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status } = req.body;

    const stmt = db.prepare(`
      UPDATE visits SET nome=?, documento=?, apartamento=?, telefone=?, motivo=?, dataEntrada=?, dataSaida=?, status=?
      WHERE id=?
    `);

    const result = stmt.run(
      nome, documento, apartamento, telefone, motivo, dataEntrada, dataSaida, status, id
    );

    if (result.changes === 0) {
      return res.status(404).json({ error: 'Visita não encontrada' });
    }

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
  console.log(`   Endpoints:`);
  console.log(`   - GET    /api/owners`);
  console.log(`   - GET    /api/owners/:id`);
  console.log(`   - POST   /api/owners`);
  console.log(`   - PUT    /api/owners/:id`);
  console.log(`   - DELETE /api/owners/:id`);
  console.log(`   - GET    /api/visits`);
  console.log(`   - POST   /api/visits`);
  console.log(`   - PUT    /api/visits/:id`);
  console.log(`   - GET    /api/health`);
});
