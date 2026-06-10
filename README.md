# Porteiro Inteligente

Aplicativo nativo Android para gestão de portaria em condomínios.

## Sobre

O **Porteiro Inteligente** é um aplicativo Android nativo desenvolvido em Kotlin que visa modernizar e simplificar a gestão de portaria em condomínios. Ele oferece funcionalidades de cadastro de visitantes, registro de entradas e saídas, cadastro de moradores/proprietários com geração de QR Code, leitura de QR Code via câmera e redirecionamento automático para o WhatsApp do morador.

## Tecnologia

- **Linguagem:** Kotlin
- **Plataforma:** Android Nativo
- **Interface:** Jetpack Compose com Material Design 3 (Material You)
- **Navegação:** Navigation Compose (Single Activity)
- **Arquitetura:** MVVM / MVI Ready
- **Persistência local:** Room
- **Injeção de dependência:** Hilt
- **Carregamento de Imagem:** Coil
- **MinSdk:** 23 (Android 6.0 Marshmallow)
- **TargetSdk:** 35 (Android 15)
- **CompileSdk:** 35

## Estrutura do projeto

```text
PorteiroInteligente/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/
│           │   └── br/com/porteirointeligente/
│           │       ├── PorteiroInteligenteApp.kt
│           │       ├── data/
│           │       │   ├── local/
│           │       │   │   ├── AppDatabase.kt
│           │       │   │   ├── dao/
│           │       │   │   │   └── VisitDao.kt
│           │       │   │   └── entity/
│           │       │   │       └── VisitEntity.kt
│           │       │   └── repository/
│           │       │       └── VisitRepository.kt
│           │       ├── di/
│           │       │   └── AppModule.kt
│           │       ├── domain/
│           │       │   └── model/
│           │       │       └── Visit.kt
│           │       └── ui/
│           │           └── home/
│           │               ├── HomeActivity.kt
│           │               └── HomeViewModel.kt
│           └── res/
│               ├── drawable/
│               │   └── ic_launcher_foreground.xml
│               ├── layout/
│               │   └── activity_home.xml
│               ├── mipmap-anydpi-v26/
│               │   └── ic_launcher.xml
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── values-night/
│                   └── themes.xml
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

## Configuração do ambiente

### Pré-requisitos

- **Android Studio** Hedgehog (2023.1.1) ou superior
- **JDK 17** ou superior
- **Android SDK** com plataforma Android 35 instalada
- **Gradle 8.7** (gerenciado pelo wrapper)

### Importando o projeto

1. Abra o Android Studio.
2. Selecione `File > Open` e aponte para a pasta raiz do projeto (`PorteiroInteligente/`).
3. Aguarde o Gradle sincronizar as dependências.
4. Conecte um dispositivo Android ou inicie um emulador.
5. Execute o app no botão `Run`.

### Gerando o gradle-wrapper.jar

Caso o arquivo `gradle/wrapper/gradle-wrapper.jar` não esteja presente, você pode gerá-lo de duas formas:

1. Pelo Android Studio (ao abrir o projeto, o IDE oferece a opção de gerar o wrapper).
2. Pelo terminal, na raiz do projeto:

```bash
gradle wrapper --gradle-version 8.7
```

## Funcionalidades

### Implementadas (estrutura base)

- Tela inicial (`HomeActivity`) com identificação do condomínio/apartamento.
- Configuração de MVVM com `HomeViewModel`.
- Persistência local com Room (`AppDatabase`, `VisitDao`, `VisitEntity`).
- Injeção de dependência com Hilt (`AppModule`, `PorteiroInteligenteApp`).
- Modelos de domínio (`Visit`, com `VisitStatus`).

### Próximos passos

- Tela de login/cadastro de moradores.
- Cadastro de proprietário/morador com nome, apartamento e número de celular.
- Geração e exibição do QR Code do proprietário.
- Leitura de QR Code pela câmera do entregador.
- Redirecionamento automático para o WhatsApp do morador com mensagem padrão.
- Tela de cadastro de visitante.
- Tela de histórico de visitas.
- Integração com câmera para leitura de QR Code ou reconhecimento facial.
- API/backend para comunicação entre portaria e moradores.
- Notificações push.
- Integração com interfone.

## Modelos de domínio

### Visit

Representa uma visita registrada no aplicativo.

| Campo        | Tipo           | Descrição                              |
|--------------|----------------|----------------------------------------|
| `id`         | `Long`         | Identificador único                    |
| `nome`       | `String`       | Nome do visitante                      |
| `documento`  | `String`       | Documento de identificação             |
| `apartamento`| `String`       | Apartamento de destino                 |
| `telefone`   | `String`       | Telefone de contato                    |
| `motivo`     | `String`       | Motivo da visita                       |
| `dataEntrada`| `Long` (epoch) | Data/hora de entrada                   |
| `dataSaida`  | `Long?` (epoch)| Data/hora de saída (opcional)          |
| `status`     | `VisitStatus`  | Status atual da visita                 |

Status possíveis:

- `ENTRADA_REGISTRADA`
- `SAIDA_REGISTRADA`
- `CANCELADA`

### Owner

Representa o morador/proprietário cadastrado no aplicativo.

| Campo           | Tipo           | Descrição                                     |
|-----------------|----------------|-----------------------------------------------|
| `id`            | `Long`         | Identificador único                           |
| `nome`          | `String`       | Nome do morador                               |
| `apartamento`   | `String`       | Número do apartamento                         |
| `telefone`      | `String`       | Número de celular (com DDD)                   |
| `qrCodePayload` | `String`       | Conteúdo codificado no QR Code do morador     |
| `dataCadastro`  | `Long` (epoch) | Data/hora do cadastro                         |

## Regra do QR Code

- O proprietário informa o número de celular.
- O app gera um QR Code contendo um link/payload que redireciona para o WhatsApp do morador.
- O entregador lê o QR Code com a câmera do celular.
- Após a leitura, o app abre automaticamente o WhatsApp com uma mensagem padrão sobre a entrega.

## Como contribuir

1. Crie uma branch a partir de `main`: `git checkout -b feature/minha-feature`
2. Faça commits pequenos e descritivos.
3. Abra um Pull Request descrevendo as mudanças.

## Licença

Este projeto é privado e de uso restrito.
# Porteiro-Inteligente-2026
