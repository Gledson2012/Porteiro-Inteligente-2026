let Database;
try {
  Database = require('better-sqlite3');
} catch (e) {
  console.warn('better-sqlite3 não está disponível. O backend rodará em modo sem banco de dados local.');
}

const path = require('path');
const DB_PATH = path.join(__dirname, 'porteiro_inteligente.db');

let db = null;

function getDatabase() {
  if (!Database) {
    // Retorna um banco de dados mockado para evitar erros de compilação ou execução em ambientes sem SQLite (como Vercel)
    return {
      prepare: () => ({
        get: () => null,
        all: () => [],
        run: () => ({ lastInsertRowid: 0 })
      }),
      exec: () => {}
    };
  }

  if (!db) {
    db = new Database(DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');
    initializeTables();
  }
  return db;
}

function initializeTables() {
  if (!db) return;
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS owners (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      userId INTEGER,
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
      offlineUntil INTEGER,
      FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS visits (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      ownerId INTEGER,
      nome TEXT NOT NULL,
      documento TEXT DEFAULT '',
      apartamento TEXT NOT NULL,
      telefone TEXT DEFAULT '',
      motivo TEXT DEFAULT '',
      dataEntrada INTEGER NOT NULL,
      dataSaida INTEGER,
      status TEXT DEFAULT 'ENTRADA_REGISTRADA',
      FOREIGN KEY (ownerId) REFERENCES owners(id) ON DELETE CASCADE
    );
  `);
}

module.exports = { getDatabase };
