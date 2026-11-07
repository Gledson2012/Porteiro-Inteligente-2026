# Assinatura do APK

O APK publicado é assinado com uma chave de release dedicada. **Nunca
versione a keystore, senhas ou `keystore.properties`.**

## Arquivos locais (não versionados)

- Keystore: `~/.config/porteiro-inteligente/release.keystore`
- Credenciais: `~/.config/porteiro-inteligente/CREDENTIALS.txt` (chmod 600)

## Build de release local

1. Copie as credenciais:

   ```bash
   cp ~/.config/porteiro-inteligente/CREDENTIALS.txt app/keystore.properties
   ```

2. Rode o build assinado:

   ```bash
   ./gradlew assembleRelease
   ```

3. O APK assinado sai em `app/build/outputs/apk/release/app-release.apk`.

## CI (GitHub Actions)

Os segredos do repositório são:

- `KEYSTORE_BASE64` — saída de `base64 -w0 release.keystore`
- `KEYSTORE_PASSWORD`
- `KEY_PASSWORD`
- `KEY_ALIAS` (valor atual: `porteiro`)

Para (re)configurar, use `gh secret set ...` (ver `gh secret list`). O
workflow decodifica a keystore em `app/release.keystore` e gera
`app/keystore.properties` a cada execução.

## Backup

A keystore **não pode ser regenerada** sem perder a capacidade de publicar
atualizações sobre a versão em produção. Faça backup imediato e seguro de:

- `release.keystore`
- `CREDENTIALS.txt`

A perda da keystore exige uma nova chave e força reinstalação única em
todos os dispositivos.

## Distribuição

O APK de produção **não é versionado**. Publique-o em GitHub Releases; o
backend e o Vercel redirecionam `/PorteiroInteligente.apk` para
`releases/latest/download/PorteiroInteligente.apk`.
