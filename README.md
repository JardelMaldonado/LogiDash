# 🚛 LogiDash — Backend de Gestão de Frota

Backend de um dashboard para gestão e análise de abastecimentos de frota, desenvolvido com **Java 21 + Spring Boot 4**. O projeto integra uma API externa de gestão de frotas (ProFrotas), importa dados automaticamente e expõe endpoints para um frontend de análise.

## 💡 Sobre o projeto

O **LogiDash** foi criado como projeto pessoal para aprofundar conceitos essenciais de backend na prática: segurança com JWT, integração com APIs externas, agendamento de tarefas e construção de lógica analítica real.

O problema que motivou o projeto é comum em operações com frota: os dados de abastecimento costumam ficar espalhados em sistemas terceiros, dificultando análises como gasto por motorista, posto mais caro, variação do diesel ao longo do tempo e identificação de abastecimentos suspeitos.

O LogiDash resolve isso automatizando a importação, tratando inconsistências dos dados e entregando tudo pronto para visualização. O projeto nasceu como aprendizado, mas foi construído com foco em resolver um problema real.

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

## ✨ Funcionalidades

### 🔐 Autenticação e Segurança
- Login com JWT armazenado em cookie **HttpOnly + Secure + SameSite=None**.
- Proteção contra XSS, evitando exposição do token no JavaScript.
- Rate limiting no login: máximo de **5 tentativas por IP por minuto**.
- Controle de acesso por roles (**ADMIN / USER**) com dupla proteção.
- CORS restrito à origem configurada do frontend.
- Senhas armazenadas com **BCrypt**.

### 🔄 Integração com API Externa
- Paginação automática de até **100 páginas** por execução.
- Retry automático em respostas **HTTP 429** da API parceira.
- Filtros antes da persistência: estornos, recusas, registros sem itens e fora do intervalo de datas.
- Importação idempotente, evitando duplicidades.

### 📅 Agendamento Automático
- Importação diária às **11h** com janela de **3 dias**.
- Reimportação mensal no dia **1º**, capturando registros retroativos do mês anterior.
- Importação inicial automática ao subir a aplicação.

### 📊 Dashboard Analítico
- Total geral de gastos, litros e número de abastecimentos.
- Separação por posto interno vs externo.
- Preço médio por tipo de combustível.
- Ranking de postos e motoristas por consumo e valor gasto.
- Séries temporais de gastos diários e preço do diesel.
- Filtros dinâmicos por placas e motoristas.
- Distinção entre **Arla balde** e **Arla granel** com conversão automática.

### 👤 Gestão de Usuários
- CRUD completo de usuários exclusivo para **ADMIN**.
- Ativação e desativação de contas sem deletar registros.
- Usuário desativado tem login bloqueado imediatamente.

## 🧪 Testes e CI/CD

O projeto possui testes em duas camadas e pipeline automatizado.

### Testes unitários
Cobrem a camada de serviço isoladamente com **JUnit 5 + Mockito**, validando regras de negócio com mocks.

### Testes de integração
Usam **Testcontainers + WireMock + RestTestClient** com:
- Postgres real via Testcontainers.
- Contexto Spring completo.
- Validação de queries com `JOIN FETCH` e prevenção de N+1.
- Integração com API externa, incluindo paginação e retry em 429.
- Endpoints HTTP testados contra servidor real em `RANDOM_PORT`.

### Pipeline de CI/CD
Fluxo executado a cada push/PR na branch principal:
- `unit-tests` → roda os testes unitários.
- `integration-tests` → roda os testes de integração.
- `build` → gera o `.jar` final e publica como artefato.

### Qualidade de código
Análise estática contínua com **Qodana** para detectar code smells, dependências vulneráveis e outros problemas antes do merge.

## 🏗️ Arquitetura

```text
src/
├── config/          # SecurityConfig, WebClientConfig
├── controller/      # AbastecimentoController, AuthController, DashboardController, UsuarioController
├── database/
│   ├── model/       # Entities JPA
│   └── repository/  # Interfaces Spring Data JPA
├── dto/
│   ├── abastecimento/  # DTOs de entrada/saída
│   ├── auth/           # LoginRequest, LoginResponse, LoginResponsePublico
│   ├── dashboard/      # DashboardResponse e objetos analíticos
│   └── usuario/        # UsuarioRequest, UsuarioResponse
├── exception/       # Exceções customizadas
├── filter/          # JwtFilter, RateLimitFilter
├── handler/         # GlobalExceptionHandler
├── scheduler/       # ImportacaoScheduler
├── service/         # Regras de negócio
└── utils/           # CookieUtil
```

## 🔒 Decisões de Segurança

### Por que JWT em cookie e não no corpo da resposta?
Guardar o token em `localStorage` ou `sessionStorage` expõe o JWT a scripts executados no navegador. Em um ataque XSS, um script malicioso poderia ler o token e agir como o usuário.

Aqui, o token é enviado via `Set-Cookie` com `HttpOnly=true`, impedindo acesso por JavaScript. O atributo `Secure=true` garante que o cookie só trafegue via HTTPS.

### Por que `SameSite=None`?
Frontend e backend rodam em origens diferentes. Com `SameSite=Strict`, o navegador bloquearia o cookie em requisições cross-origin. `SameSite=None` permite o envio, mas exige `Secure=true`, mantendo a proteção de transporte.

### Por que BCrypt?
O BCrypt aplica salt aleatório e custo configurável, dificultando ataques de dicionário e rainbow table em caso de vazamento do banco.

### Por que rate limiting apenas no login?
Ataques de força bruta geralmente miram a autenticação. Os demais endpoints já exigem JWT válido, então limitar globalmente não traria tanto benefício.

## ⚙️ Como executar

### Pré-requisitos
- Java 21+
- Maven 3.9+ ou Maven Wrapper
- PostgreSQL rodando
- Docker rodando para os testes de integração

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
git clone https://github.com/seu-usuario/logidash-backend.git
cd logidash-backend

./mvnw spring-boot:run
```

### Testes
```bash
./mvnw test
./mvnw verify -Dsurefire.skip=true
```

A aplicação sobe na porta **8080** e executa automaticamente a importação inicial dos últimos 3 dias.

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

## 🧪 Boas práticas aplicadas
- `JOIN FETCH` no JPA para evitar N+1 queries.
- `BigDecimal` em cálculos financeiros e de volume.
- `@Transactional(readOnly = true)` em consultas.
- Tratamento global de exceções com `@RestControllerAdvice`.
- DTOs imutáveis com `record`.
- Suíte de testes cobrindo services, repositories e controllers.
- Pipeline de CI/CD automatizado com GitHub Actions e Qodana.
