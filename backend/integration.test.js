const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('node:http');
const crypto = require('node:crypto');

// Gera par de chaves RSA exclusivo para os testes de integração
const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'der' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' }
});

process.env.QR_PRIVATE_KEY = privateKey;
process.env.SECRET_KEY = 'super-test-secret-with-more-than-32-chars-long';
process.env.ALLOW_EPHEMERAL_DATABASE = 'true';
process.env.DATABASE_PATH = ':memory:';

const app = require('./server.js');

function base64UrlEncode(buffer) {
  return buffer.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function createV2Payload(ownerId, { phone, name, isOffline, offlineMessage, offlineUntil }, key = publicKey) {
  const json = JSON.stringify({
    i: ownerId,
    p: phone,
    n: name || '',
    o: isOffline ? 1 : 0,
    m: offlineMessage || '',
    u: offlineUntil || null
  });

  const aesKey = crypto.randomBytes(32);
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', aesKey, iv);
  const ciphertext = Buffer.concat([cipher.update(json, 'utf8'), cipher.final()]);
  const tag = cipher.getAuthTag();
  const ciphertextWithTag = Buffer.concat([ciphertext, tag]);

  const encryptedKey = crypto.publicEncrypt({
    key: crypto.createPublicKey({ key, format: 'der', type: 'spki' }),
    padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
    oaepHash: 'sha256'
  }, aesKey);

  return [
    'v2',
    ownerId.toString(),
    base64UrlEncode(encryptedKey),
    base64UrlEncode(iv),
    base64UrlEncode(ciphertextWithTag)
  ].join('.');
}

function request(server, method, path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const payload = body === undefined ? '' : JSON.stringify(body);
    const reqHeaders = { ...headers };
    if (body !== undefined) {
      reqHeaders['content-type'] = 'application/json';
      reqHeaders['content-length'] = Buffer.byteLength(payload);
    }
    const req = http.request({
      host: '127.0.0.1',
      port: server.address().port,
      path,
      method,
      headers: reqHeaders
    }, res => {
      let responseBody = '';
      res.setEncoding('utf8');
      res.on('data', chunk => { responseBody += chunk; });
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: responseBody }));
    });
    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
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

test('QR v2 - Decodifica envelope híbrido RSA/AES e exibe link do WhatsApp', async () => {
  await withServer(async server => {
    const payload = createV2Payload(42, {
      phone: '11987654321',
      name: 'Carlos Oliveira',
      isOffline: false
    });

    const res = await request(server, 'GET', `/scan/${payload}`);
    assert.equal(res.status, 200);
    assert.match(res.body, /wa\.me\/5511987654321/);
    assert.match(res.body, /Carlos/);
  });
});

test('QR v2 - Exibe mensagem de ausência quando morador está offline', async () => {
  await withServer(async server => {
    const payload = createV2Payload(43, {
      phone: '11987654321',
      name: 'Ana Pereira',
      isOffline: true,
      offlineMessage: 'Estou em reunião no momento, favor deixar na portaria.'
    });

    const res = await request(server, 'GET', `/scan/${payload}`);
    assert.equal(res.status, 200);
    assert.match(res.body, /Estou em reunião no momento, favor deixar na portaria\./);
  });
});

test('QR v2 - Ignora modo offline quando offlineUntil está expirado', async () => {
  await withServer(async server => {
    const pastTime = Date.now() - 3600000;
    const payload = createV2Payload(44, {
      phone: '11987654321',
      name: 'Marcos Silva',
      isOffline: true,
      offlineMessage: 'Mensagem antiga expirada',
      offlineUntil: pastTime
    });

    const res = await request(server, 'GET', `/scan/${payload}`);
    assert.equal(res.status, 200);
    // Como expirou, não deve renderizar a mensagem de ausência como offline ativo
    assert.match(res.body, /wa\.me\/5511987654321/);
    assert.doesNotMatch(res.body, /Mensagem antiga expirada/);
  });
});

test('QR v2 - Protege contra XSS fazendo escape do nome e mensagem', async () => {
  await withServer(async server => {
    const payload = createV2Payload(45, {
      phone: '11987654321',
      name: '<script>alert(1)</script>João',
      isOffline: true,
      offlineMessage: '<img src=x onerror=evil()>'
    });

    const res = await request(server, 'GET', `/scan/${payload}`);
    assert.equal(res.status, 200);
    assert.doesNotMatch(res.body, /<script>alert\(1\)<\/script>/);
    assert.doesNotMatch(res.body, /<img src=x/);
    assert.match(res.body, /&lt;script&gt;alert\(1\)&lt;\/script&gt;/);
    assert.match(res.body, /&lt;img src=x/);
  });
});

test('QR v2 - Rejeita payload com ID externo divergente do JSON cifrado', async () => {
  await withServer(async server => {
    const validPayload = createV2Payload(50, { phone: '11987654321', name: 'Teste' });
    const parts = validPayload.split('.');
    parts[1] = '999'; // Forja ID diferente no prefixo
    const tamperedPayload = parts.join('.');

    const res = await request(server, 'GET', `/scan/${tamperedPayload}`);
    assert.equal(res.status, 400);
    assert.match(res.body, /QR Code inv&#225;lido|QR Code inválido/);
  });
});

test('QR v2 - Rejeita payload com dados corrompidos', async () => {
  await withServer(async server => {
    const validPayload = createV2Payload(51, { phone: '11987654321', name: 'Teste' });
    const parts = validPayload.split('.');
    parts[4] = parts[4].slice(0, -4) + 'AAAA'; // Adulteração do ciphertext/tag
    const corruptedPayload = parts.join('.');

    const res = await request(server, 'GET', `/scan/${corruptedPayload}`);
    assert.equal(res.status, 400);
    assert.match(res.body, /QR Code inv&#225;lido|QR Code inválido/);
  });
});

test('Segurança - Rotas protegidas recusam acesso sem token Bearer', async () => {
  await withServer(async server => {
    const resOwners = await request(server, 'GET', '/api/owners');
    assert.equal(resOwners.status, 401);

    const resVisits = await request(server, 'GET', '/api/visits');
    assert.equal(resVisits.status, 401);
  });
});
