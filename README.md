# 🚛 LogiDash — Backend de Gestão de Frota

Backend de um dashboard para **gestão e análise de abastecimentos de frota**, desenvolvido com **Java 21 + Spring Boot 4**. Integra com uma API externa de gestão de frotas (ProFrotas), importa dados automaticamente e expõe endpoints para um frontend de análise.

---

## 💡 Sobre o projeto

Criei o LogiDash como um projeto pessoal para aprofundar na prática temas que considero essenciais no desenvolvimento backend: segurança com JWT, integração com APIs externas, agendamento de tarefas e construção de lógica analítica real.

O problema que motivou o projeto é concreto: empresas que dependem de frota — seja transporte, logística ou qualquer operação com veículos — geralmente têm acesso aos dados brutos de abastecimento via sistemas terceiros, mas não têm uma visão consolidada e filtrável desses dados. Saber quanto gastou por motorista, qual posto cobra mais caro, como o preço do diesel variou no mês ou identificar abastecimentos suspeitos exige cruzar planilhas manualmente.

O LogiDash resolve isso automatizando a importação, tratando inconsistências dos dados (estornos, recusas, registros duplicados) e entregando tudo pronto para visualização. Nasceu como aprendizado, mas foi construído com a preocupação de resolver um problema real — e com potencial de virar um produto.

---

## 🧰 Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 |
| Segurança | Spring Security + JWT |
| Persistência | Spring Data JPA + Hibernate |
| Banco de Dados | PostgreSQL |
| Migrações | Flyway |
| HTTP Client | Spring WebFlux (WebClient) |
| Rate Limiting | Bucket4j |
| Build | Maven |
| Testes | JUnit 5, Mockito, Testcontainers, WireMock, RestTestClient |
| CI/CD | GitHub Actions |
| Qualidade de Código | Qodana |

---

## ✨ Funcionalidades

### 🔐 Autenticação e Segurança
- Login com **JWT armazenado em cookie HttpOnly + Secure + SameSite=None** — o token nunca fica exposto no corpo da resposta nem acessível via JavaScript
- Proteção contra **XSS** via `HttpOnly`; cookie trafega exclusivamente sobre **HTTPS** via `Secure=true`
- **Rate limiting** no endpoint de login: máximo de **5 tentativas por IP por minuto** via Bucket4j
- Controle de acesso por **roles (ADMIN / USER)** com dupla proteção: `SecurityConfig` + `@PreAuthorize`
- CORS restrito à origem configurada do frontend
- Senhas armazenadas com **BCrypt**

### 🔄 Integração com API Externa (ProFrotas)
- **Paginação automática** de até 100 páginas (10.000 registros por execução)
- **Retry automático** ao receber HTTP 429 (Too Many Requests) da API parceira
- Filtros aplicados antes de persistir: remove estornos, recusas, registros sem itens e fora do intervalo de datas
- **Importação idempotente**: verificação por identificador único antes de salvar, sem risco de duplicação

### 📅 Agendamento Automático
- **Importação diária** às 11h com janela de 3 dias (cobre atrasos de sincronização)
- **Reimportação mensal** no dia 1º de cada mês, capturando registros retroativos do mês anterior
- Importação inicial automática ao subir a aplicação (`ApplicationRunner`)

### 📊 Dashboard Analítico
- Total geral de gastos, litros e número de abastecimentos
- Separação por **posto interno vs externo**
- **Preço médio** por tipo de combustível (Diesel, Arla Granel, Arla Balde, Gasolina)
- **Ranking de postos e motoristas** por consumo e valor gasto
- **Séries temporais** de gastos diários e preço do diesel para gráficos
- Listas de placas e motoristas para filtros dinâmicos no frontend
- Lógica de distinção entre **Arla balde e granel** com conversão automática de litros

### 👤 Gestão de Usuários
- CRUD completo de usuários (exclusivo para ADMIN)
- Ativação/desativação de conta sem deletar o registro
- Usuário desativado tem login bloqueado imediatamente via `isAccountNonLocked()`

---

## 🧪 Testes e CI/CD

O projeto tem uma suíte de testes em duas camadas, além de um pipeline de CI/CD automatizado.

### Testes unitários (JUnit 5 + Mockito)
Cobrem a camada de serviço isoladamente (`DashboardService`, `UsuarioService`, `AbastecimentoService`), validando regras de negócio com mocks das dependências.

### Testes de integração (Testcontainers + WireMock + RestTestClient)
Sobem um Postgres real via Testcontainers e o contexto Spring completo, cobrindo:

- **Repository**: query com `JOIN FETCH` validada contra N+1 (a sessão do Hibernate é fechada antes da asserção, provando que a coleção não depende de lazy loading), filtro por intervalo de datas, e `existsByIdentificador`
- **Integração com API externa**: paginação e retentativa automática em respostas 429, simuladas via WireMock
- **Endpoints HTTP**: login (credenciais válidas e inválidas), atributos de segurança do cookie (`HttpOnly`, `Secure`, `SameSite`, `maxAge`) e logout, testados via `RestTestClient` contra o servidor real (`RANDOM_PORT`)

### Pipeline de CI/CD (GitHub Actions)
Três jobs encadeados a cada push/PR na branch principal:

1. **`unit-tests`** → roda os testes unitários (`mvnw test`)
2. **`integration-tests`** → roda os testes de integração via `maven-failsafe-plugin` (`mvnw verify -Dsurefire.skip=true`), com Docker disponível nativamente no runner para o Testcontainers
3. **`build`** → gera o `.jar` final (sem reexecutar os testes) e o publica como artefato do workflow

### Qualidade de código (Qodana)
Análise estática contínua via [Qodana](https://www.jetbrains.com/qodana/) (JetBrains), rodando a cada push/PR para detectar code smells, dependências vulneráveis e outros problemas antes do merge.

---

## 🏗️ Arquitetura

```
src/
├── config/          # SecurityConfig, WebClientConfig
├── controller/      # AbastecimentoController, AuthController, DashboardController, UsuarioController
├── database/
│   ├── model/       # Entities JPA (AbastecimentoEntity, UsuarioEntity...)
│   └── repository/  # Interfaces Spring Data JPA
├── dto/
│   ├── abastecimento/  # DTOs de entrada/saída de abastecimento
│   ├── auth/           # LoginRequest, LoginResponse, LoginResponsePublico
│   ├── dashboard/      # DashboardResponse e objetos analíticos
│   └── usuario/        # UsuarioRequest, UsuarioResponse
├── exception/       # Exceções customizadas
├── filter/          # JwtFilter, RateLimitFilter
├── handler/         # GlobalExceptionHandler
├── scheduler/       # ImportacaoScheduler
├── service/         # AbastecimentoService, AuthService, DashboardService...
└── utils/           # CookieUtil
```

---

## 🔒 Decisões de Segurança

### Por que JWT em Cookie e não no corpo da resposta?

**Problema que resolve:** a abordagem mais comum de guardar o token no `localStorage` ou `sessionStorage` do browser expõe o JWT a qualquer script rodando na página. Um ataque **XSS** — mesmo via biblioteca de terceiro comprometida — consegue ler o token e impersonar o usuário em todas as requisições.

**Como foi resolvido:** o token é enviado via `Set-Cookie` com `HttpOnly=true`, que impede qualquer acesso via JavaScript — o browser envia o cookie automaticamente nas requisições, mas nenhum script consegue lê-lo ou roubá-lo. O atributo `Secure=true` garante que o cookie **só é transmitido em conexões HTTPS**, nunca em HTTP puro, eliminando o risco de interceptação em redes não seguras.

**Por que `SameSite=None` e não `Strict`?** O frontend e o backend rodam em origens diferentes (domínios/portas distintos). Com `SameSite=Strict`, o browser bloquearia o cookie em requisições cross-origin e o login nunca funcionaria. `SameSite=None` permite o envio cross-origin, mas **exige obrigatoriamente `Secure=true`** — os browsers recusam cookies `SameSite=None` sem HTTPS, o que mantém a proteção de transporte. A proteção contra CSRF é garantida pela combinação de **CORS restrito** à origem do frontend e pela validação do JWT em cada requisição.

### Por que BCrypt?
BCrypt aplica um salt aleatório e um fator de custo configurável, tornando ataques de dicionário e rainbow table inviáveis mesmo em caso de vazamento do banco.

### Por que Rate Limiting apenas no login?
Ataques de força bruta visam o endpoint de autenticação. Os demais endpoints já são protegidos por JWT válido, então o custo de rate limiting global seria desnecessário.

---

## ⚙️ Como executar

### Pré-requisitos
- Java 21+
- Maven 3.9+ (ou use o Maven Wrapper incluído: `./mvnw`)
- PostgreSQL rodando
- Docker rodando (necessário para os testes de integração via Testcontainers)

### Configuração (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/logidash
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

jwt.secret=sua_chave_secreta_longa_e_segura
jwt.expiration=86400000

profrotas.api.url=https://api.profrotas.com.br
profrotas.api.token=seu_token_profrotas

frontend.url=http://localhost:3000
```

### Executando

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/logidash-backend.git
cd logidash-backend

# Build e execução
./mvnw spring-boot:run

# Rodar testes unitários
./mvnw test

# Rodar testes de integração
./mvnw verify -Dsurefire.skip=true
```

A aplicação sobe na porta `8080` e já executa uma importação inicial dos últimos 3 dias automaticamente.

---

## 📡 Endpoints

### Autenticação
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Login (retorna cookie JWT) | Pública |
| POST | `/api/v1/auth/logout` | Logout (remove cookie) | Pública |

### Abastecimentos
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/api/v1/abastecimentos/consultar?dataInicio=&dataFim=` | Lista abastecimentos por período | JWT |

### Dashboard
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/api/v1/dashboard?dataInicial=&dataFinal=&placa=&motorista=` | Dados analíticos completos | JWT |

### Usuários
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/api/v1/usuarios` | Lista todos os usuários | ADMIN |
| POST | `/api/v1/usuarios` | Cria novo usuário | ADMIN |
| PUT | `/api/v1/usuarios/{id}` | Edita usuário | ADMIN |
| PATCH | `/api/v1/usuarios/{id}/status` | Ativa/desativa usuário | ADMIN |

---

## 🧪 Boas práticas aplicadas

- **`JOIN FETCH`** no JPA para evitar o problema de N+1 queries
- **`BigDecimal`** em todos os cálculos financeiros e de volume (sem perda de precisão)
- **`@Transactional(readOnly = true)`** nas operações de leitura para otimização do JPA
- **Tratamento global de exceções** com `@RestControllerAdvice`: respostas padronizadas sem vazar stack traces para o cliente
- **DTOs imutáveis** com `record` do Java para transporte de dados
- **Suíte de testes unitários e de integração** cobrindo services, repositories e controllers
- **Pipeline de CI/CD automatizado** com GitHub Actions e análise estática contínua via Qodana

---
