let Database;
try {
  Database = require('better-sqlite3');
} catch (e) {
  console.warn('better-sqlite3 não está disponível; rotas que exigem banco ficarão indisponíveis.');
}

const path = require('path');
const runningOnVercel = Boolean(process.env.VERCEL);
const configuredDatabasePath = process.env.DATABASE_PATH || null;
const DB_PATH = configuredDatabasePath || (
  runningOnVercel
    ? path.join('/tmp', 'porteiro_inteligente.db')
    : path.join(__dirname, 'porteiro_inteligente.db')
);

function isEphemeralPath(databasePath) {
  if (databasePath === ':memory:') return true;
  const normalized = path.resolve(databasePath);
  return normalized === '/tmp' || normalized.startsWith('/tmp' + path.sep);
}

function databaseStatus() {
  const productionEnvironment = process.env.NODE_ENV === 'production' || process.env.VERCEL_ENV === 'production';
  const explicitlyAllowsEphemeral =
    process.env.ALLOW_EPHEMERAL_DATABASE === 'true' && !productionEnvironment;
  const persistent = !runningOnVercel || (
    explicitlyAllowsEphemeral ||
    Boolean(configuredDatabasePath && !isEphemeralPath(configuredDatabasePath))
  );

  return {
    available: persistent,
    reason: persistent ? null : 'Configure DATABASE_PATH persistente antes de habilitar o SQLite na Vercel.'
  };
}

let db = null;

function getDatabase() {
  const status = databaseStatus();
  if (!status.available) {
    const error = new Error(status.reason);
    error.code = 'PERSISTENT_DATABASE_REQUIRED';
    throw error;
  }

  if (!Database) {
    throw new Error('SQLite indisponível. Instale better-sqlite3 ou configure um banco persistente.');
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

  // Atualiza bancos locais criados por versões anteriores sem apagar dados.
  ensureColumn('owners', 'userId', 'INTEGER');
  ensureColumn('owners', 'nomeCondominio', "TEXT DEFAULT ''");
  ensureColumn('owners', 'endereco', "TEXT NOT NULL DEFAULT ''");
  ensureColumn('owners', 'cep', "TEXT DEFAULT ''");
  ensureColumn('owners', 'apartamento', "TEXT NOT NULL DEFAULT ''");
  ensureColumn('owners', 'telefone', "TEXT NOT NULL DEFAULT ''");
  ensureColumn('owners', 'photoUri', 'TEXT');
  ensureColumn('owners', 'qrCodePayload', "TEXT NOT NULL DEFAULT ''");
  ensureColumn('owners', 'dataCadastro', 'INTEGER NOT NULL DEFAULT 0');
  ensureColumn('owners', 'isOffline', 'INTEGER DEFAULT 0');
  ensureColumn('owners', 'offlineMessage', "TEXT DEFAULT ''");
  ensureColumn('owners', 'offlineUntil', 'INTEGER');
  ensureColumn('visits', 'ownerId', 'INTEGER');
  ensureColumn('visits', 'documento', "TEXT DEFAULT ''");
  ensureColumn('visits', 'telefone', "TEXT DEFAULT ''");
  ensureColumn('visits', 'motivo', "TEXT DEFAULT ''");
  ensureColumn('visits', 'dataSaida', 'INTEGER');
  ensureColumn('visits', 'status', "TEXT DEFAULT 'ENTRADA_REGISTRADA'");
  db.exec('CREATE INDEX IF NOT EXISTS index_owners_userId ON owners(userId)');
  db.exec('CREATE INDEX IF NOT EXISTS index_visits_ownerId ON visits(ownerId)');
}

function ensureColumn(table, column, definition) {
  const columns = db.pragma(`table_info(${table})`);
  if (!columns.some(current => current.name === column)) {
    db.exec(`ALTER TABLE ${table} ADD COLUMN ${column} ${definition}`);
  }
}

module.exports = { getDatabase, databaseStatus };
