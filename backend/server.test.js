const test = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const http = require('node:http');

process.env.SECRET_KEY = 'test-secret-key-with-at-least-32-characters';
delete process.env.QR_PRIVATE_KEY;
delete process.env.QR_PRIVATE_KEY_FILE;
delete process.env.LEGACY_QR_KEY;
delete process.env.VERCEL;

const app = require('./server.js');

function request(server, method, path, body) {
  return new Promise((resolve, reject) => {
    const payload = body === undefined ? '' : JSON.stringify(body);
    const request = http.request({
      host: '127.0.0.1',
      port: server.address().port,
      path,
      method,
      headers: body === undefined ? {} : {
        'content-type': 'application/json',
        'content-length': Buffer.byteLength(payload)
      }
    }, response => {
      let responseBody = '';
      response.setEncoding('utf8');
      response.on('data', chunk => { responseBody += chunk; });
      response.on('end', () => resolve({ status: response.statusCode, body: responseBody }));
    });
    request.on('error', reject);
    if (payload) request.write(payload);
    request.end();
  });
}

async function withServer(callback) {
  const server = app.listen(0, '127.0.0.1');
  try {
    await new Promise(resolve => server.once('listening', resolve));
    return await callback(server);
  } finally {
    await new Promise(resolve => server.close(resolve));
  }
}

test('health reports a healthy local configuration', async () => {
  await withServer(async server => {
    const response = await request(server, 'GET', '/api/health');
    assert.equal(response.status, 200);
    assert.equal(JSON.parse(response.body).status, 'ok');
  });
});

test('legacy scan never falls back to an enumerable owner ID', async () => {
  await withServer(async server => {
    const response = await request(server, 'GET', '/scan/1_any-value');
    assert.equal(response.status, 400);
    assert.match(response.body, /não suportado|inválido/i);
  });
});

test('protected API routes require a bearer token', async () => {
  await withServer(async server => {
    const response = await request(server, 'GET', '/api/owners');
    assert.equal(response.status, 401);
  });
});

test('vercel configuration rejects an ephemeral SQLite path', () => {
  const result = spawnSync(process.execPath, ['-e',
    "process.stdout.write(JSON.stringify(require('./db').databaseStatus()))"
  ], {
    cwd: __dirname,
    env: {
      ...process.env,
      VERCEL: '1',
      DATABASE_PATH: '',
      ALLOW_EPHEMERAL_DATABASE: 'false'
    },
    encoding: 'utf8'
  });

  assert.equal(result.status, 0);
  assert.deepEqual(JSON.parse(result.stdout), {
    available: false,
    reason: 'Configure DATABASE_PATH persistente antes de habilitar o SQLite na Vercel.'
  });
});

test('in-memory SQLite is not treated as persistent on Vercel', () => {
  const result = spawnSync(process.execPath, ['-e',
    "process.stdout.write(JSON.stringify(require('./db').databaseStatus()))"
  ], {
    cwd: __dirname,
    env: {
      ...process.env,
      VERCEL: '1',
      DATABASE_PATH: ':memory:',
      ALLOW_EPHEMERAL_DATABASE: 'false'
    },
    encoding: 'utf8'
  });

  assert.equal(result.status, 0);
  assert.equal(JSON.parse(result.stdout).available, false);
});
