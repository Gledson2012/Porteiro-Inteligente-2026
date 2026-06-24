const Database = require('better-sqlite3');
const path = require('path');

const DB_PATH = path.join(__dirname, 'porteiro_inteligente.db');

let db;

function getDatabase() {
  if (!db) {
    db = new Database(DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');
    initializeTables();
  }
  return db;
}

function initializeTables() {
  db.exec(`
    CREATE TABLE IF NOT EXISTS owners (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      nome TEXT NOT NULL,
      nomeCondominio TEXT DEFAULT '',
      endereco TEXT NOT NULL,
      cep TEXT DEFAULT '',
      apartamento TEXT NOT NULL,
      telefone TEXT NOT NULL,
      photoUri TEXT,
      qrCodePayload TEXT NOT NULL,
      dataCadastro INTEGER NOT NULL,
      isOffline INTEGER DEFAULT 0,
      offlineMessage TEXT DEFAULT '',
      offlineUntil INTEGER
    );

    CREATE TABLE IF NOT EXISTS visits (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      nome TEXT NOT NULL,
      documento TEXT DEFAULT '',
      apartamento TEXT NOT NULL,
      telefone TEXT DEFAULT '',
      motivo TEXT DEFAULT '',
      dataEntrada INTEGER NOT NULL,
      dataSaida INTEGER,
      status TEXT DEFAULT 'ENTRADA_REGISTRADA'
    );
  `);
}

module.exports = { getDatabase };
