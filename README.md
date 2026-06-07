# GITME - BACKEND

Dashboard stateful de métricas de produtividade individual e de equipe baseado em dados do GitHub, com persistência em Supabase/PostgreSQL e documentação interativa via OpenAPI/Swagger.

## Sobre o Projeto

Este é um backend Spring Boot 4.0.5 que:
- **Ingere dados** do GitHub via API REST + GraphQL (Feign + RestClient)
- **Persiste** em Supabase/PostgreSQL com otimizações de batch e N+1 queries
- **Fornece 5 endpoints de métricas** calculadas sobre dados históricos (Cycle Time, Lead Time, TCM, etc.)
- **Expõe documentação interativa** via Swagger UI (OpenAPI 3.0)
- **Oferece modo demonstração** com dados pré-carregados de `axios/axios` e `vuejs/core`

O frontend **não consome a GitHub API em runtime** — todas as métricas são calculadas sobre dados persistidos, garantindo performance e auditoria.

## Stack Técnico

| Componente | Tecnologia |
|---|---|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 4.0.5 |
| **HTTP Client** | Spring Cloud OpenFeign (REST) + GraphQL manual |
| **HTTP Paginado** | Spring RestClient |
| **Persistência** | Spring Data JPA + PostgreSQL (Supabase) |
| **Utilitários** | Lombok, Jackson |
| **Validação** | Hibernate Validator (JSR-303) |
| **Documentação** | Springdoc OpenAPI 2.8.8 |

## Estrutura do Projeto

```
br.com.tcc.github_poc
├── client/              # GithubClient (Feign): REST + GraphQL contra api.github.com
├── controller/
│   ├── AuthController       # Troca de code OAuth por token GitHub
│   ├── GithubController     # Endpoints proxy para a GitHub API (repos, commits, PRs, etc.)
│   ├── DemoController       # Endpoints de demonstração (repos pré-carregados + top contributor)
│   ├── HealthController     # Health check endpoint para monitoramento
│   └── ProductivityScoreController
├── etl/                 # Motor de seeding assíncrono
│   ├── SeedController
│   ├── SeedOrchestrator (@Async)
│   ├── SeedJobState
│   ├── ingestion/       # 6 ingestion services (Commit, PR, Issue, Review, etc.)
│   ├── extraction/      # PaginatedRestFetcher + GraphQLCommitFetcher
│   ├── mapper/          # DtoToEntityMapper
│   └── ratelimit/       # RateLimitGuard
├── metrics/             # 5 endpoints de métricas + cálculos
│   ├── MetricsController
│   ├── service/         # OverviewMetricsService, FlowMetricsService, etc.
│   ├── dto/             # Response DTOs
│   └── support/         # ConventionalCommitClassifier
├── entities/            # JPA entities (User, Commit, PR, Issue, Review, etc.)
├── repositories/        # Spring Data JPA repositories
├── dto/                 # DTOs de cliente/ingestão
└── config/              # Configuração (OpenAPI, CORS, etc.)
```

## Como Executar

### 1. Requisitos

- **Java 21** instalado
- **Maven 3.8+**
- **PostgreSQL/Supabase** com acesso via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- **Token do GitHub** (Classic ou Fine-grained) com `repo` ou `public_repo`

### 2. Configuração de Ambiente

Criar `.env` na raiz do projeto:

```properties
DB_URL=jdbc:postgresql://<session-pooler-host>:5432/postgres
DB_USERNAME=postgres.<project-ref>
DB_PASSWORD=<password>
GITHUB_CLIENT_ID=your_github_oauth_client_id
GITHUB_CLIENT_SECRET=your_github_oauth_client_secret
```

> **Supabase:** use a URL do **Session pooler** (não a conexão direta), pois ambientes cloud como Render usam IPv4. A URL do session pooler e o username correto (`postgres.<project-ref>`) estão disponíveis em Dashboard → Connect → Connection string → JDBC → Session pooler.

> O banco atualmente configurado já possui os dados de `axios/axios` e `vuejs/core` pré-carregados, habilitando o modo demonstração sem necessidade de ETL.

> `GITHUB_CLIENT_ID` e `GITHUB_CLIENT_SECRET` são obrigatórios para o fluxo OAuth do frontend. Crie um OAuth App em https://github.com/settings/developers com callback `http://localhost:8080/auth-callback` (desenvolvimento) ou `https://gitme.enzocerq.workers.dev/auth-callback` (produção).

### 3. Rodar a Aplicação

```bash
./mvnw spring-boot:run
```

A aplicação iniciará em `http://localhost:8081`.

## Endpoints Disponíveis

### 📊 Swagger UI (Documentação Interativa)

**URL:** `http://localhost:8081/swagger-ui.html`

### 🔌 Endpoints de Métricas

**Base:** `/api/poc/metrics`

Todos requerem `authorLogin` e opcionalmente `from` / `to` (ISO-8601 dates).

| Endpoint | Descrição |
|---|---|
| `GET /overview` | Volume de commits/PRs, taxa de aceitação e série diária |
| `GET /flow` | Cycle Time, Lead Time, TCM, Time in Review, dias ativos; `recentActivity` inclui `additions` e `deletions` nos itens de tipo `commit` (campos são `null` para PRs) |
| `GET /repos` | Participação relativa por repositório |
| `GET /collaboration` | Distribuição de revisões, comparativo individual vs equipe |
| `GET /insights` | Classificação Conventional Commits + mapa de produtividade (grid 7×24) |

**Validação aplicada:**
- ✓ `repoId` deve ser positivo (`@Positive`)
- ✓ `authorLogin` não pode ser branco (`@NotBlank`)
- ✓ `from` ≤ `to` obrigatoriamente
- ✓ `repoId` deve existir no banco de dados (404 se não encontrado)

**Exemplo:**
```bash
curl "http://localhost:8081/api/poc/metrics/overview?repoId=23088740&authorLogin=DigitalBrainJS&from=2025-05-19&to=2026-05-19"
```

### 🌱 Endpoints de ETL

**Base:** `/api/poc/etl`

| Endpoint | Método | Descrição |
|---|---|---|
| `/seed` | `POST` | Iniciar carga de dados. Body `{ "repos": ["owner/repo"] }` sobrescreve config |
| `/status` | `GET` | Status e contadores do job em execução |

```bash
# Iniciar seed
curl -X POST http://localhost:8081/api/poc/etl/seed \
  -H "Authorization: Bearer ghp_xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"repos": ["axios/axios"]}'

# Consultar status
curl http://localhost:8081/api/poc/etl/status
```

### 🎯 Endpoints de Demonstração

**Base:** `/api/poc/demo`

Não requerem autenticação. Retornam dados dos repositórios pré-carregados no banco.

| Endpoint | Método | Descrição |
|---|---|---|
| `/repos` | `GET` | Lista os repositórios de demonstração disponíveis (`axios` e `vuejs`) |
| `/top-contributor` | `GET` | Retorna o `authorLogin` com mais commits nos repositórios informados |

**Parâmetros de `/top-contributor`:**
- `repoIds` (query, repetido) — IDs dos repositórios selecionados

**Exemplo:**
```bash
# Listar repos de demo
curl http://localhost:8081/api/poc/demo/repos

# Top contributor do axios (repoId de exemplo)
curl "http://localhost:8081/api/poc/demo/top-contributor?repoIds=23088740"
```

**Resposta de `/top-contributor`:**
```json
{ "login": "DigitalBrainJS" }
```

### 🔐 Endpoints de Autenticação

| Endpoint | Método | Descrição |
|---|---|---|
| `POST /api/auth/github` | `POST` | Troca o `code` OAuth pelo token de acesso GitHub e retorna o perfil do usuário |

### 🐙 Endpoints GitHub (Proxy)

**Base:** `/api/poc/github`

Proxy para a GitHub API. Requerem header `Authorization: Bearer <token>`. Usados pelo frontend durante a seleção de repositórios (fluxo real, não demo).

| Endpoint | Método | Descrição |
|---|---|---|
| `/repositorios` | `GET` | Lista repositórios do usuário autenticado |
| `/{owner}/{repo}/commits` | `GET` | Commits do repositório |
| `/{owner}/{repo}/contribuidores` | `GET` | Contribuidores do repositório |
| `/{owner}/{repo}/prs` | `GET` | Pull requests do repositório |
| `/{owner}/{repo}/issues` | `GET` | Issues do repositório |
| `/{owner}/{repo}/prs/{pullNumber}/reviews` | `GET` | Reviews de um PR específico |
| `/{owner}/{repo}/commits/stats` | `GET` | Estatísticas de commits do repositório |

### ❤️ Health Check

| Endpoint | Método | Descrição |
|---|---|---|
| `GET /health` | `GET` | Retorna `{"status":"UP"}` — usado pelo Render e UptimeRobot para monitoramento |

## Banco de Dados

O banco Supabase/PostgreSQL contém os dados de `axios/axios` e `vuejs/core` já populados via ETL. As tabelas principais são:

| Tabela | Conteúdo |
|---|---|
| `repositories` | Metadados dos repositórios (id = GitHub repo ID) |
| `commits` | Commits com `author_login`, `commit_date`, `additions`, `deletions` |
| `pull_requests` | PRs com datas de criação, merge e estado |
| `issues` | Issues com estado e datas |
| `reviews` | Reviews de PR com estado e autor |
| `repository_contributors` | Participação de contribuidores por repositório |

## Arquitetura de Dados

### ETL (Motor de Seeding)

1. **Extração:** GraphQL para commits (cursor-paginated), REST paginado para PRs/Issues/Reviews
2. **Rate Limit:** `RateLimitGuard` pausa automaticamente quando `X-RateLimit-Remaining < threshold`
3. **Persistência:** Batch pre-check + `Persistable<ID>` para evitar N+1 queries
4. **Idempotência:** Re-runs seguras — entidades duplicadas não são re-inseridas

### Cálculo de Métricas

- **Projeções SQL:** Aggregations nativas (AVG, MIN, MAX, COUNT) via `@Query` e Spring Data projections
- **Série temporal:** Preenchimento de gaps em Java (zero-fill), não em SQL
- **Período:** Defaults a 1 ano atrás / hoje se não especificado

### Score de Produtividade Individual

**Endpoint:** `GET /api/productivity-score/{authorLogin}?startDate=YYYY-MM-DDTHH:mm:ss&endDate=YYYY-MM-DDTHH:mm:ss`

Calcula um score de 0 a 100 para um contributor (identificado pelo GitHub login) com base em 5 componentes ponderados. O período padrão são os últimos 30 dias. Os parâmetros `startDate` e `endDate` são opcionais e devem estar no formato ISO-8601 com hora (`LocalDateTime`).

#### Fórmula Final

```
scoreFinal = (entrega × 0.35) + (eficiência × 0.25) + (qualidade × 0.20) + (colaboração × 0.10) + (consistência × 0.10)
```

#### Componentes

| Componente | Peso | Fórmula | Meta Padrão |
|---|---|---|---|
| **Entrega** | 35% | `(commits + PRs_merged × 3) / metaEntrega × 100` | 50 unidades |
| **Eficiência** | 25% | `(metaCycleTime / cycleTimeMédio) × 100` | 3 dias |
| **Qualidade** | 20% | `(PRs_merged / PRs_criados) / metaQualidade × 100` | 80% de merge rate |
| **Colaboração** | 10% | `reviewsFeitas / metaReviews × 100` | 8 reviews |
| **Consistência** | 10% | `diasAtivos / metaDiasAtivos × 100` | 20 dias ativos |

**Observações:**
- Um PR mergeado vale **3×** um commit no cálculo de Entrega
- **Eficiência** é inversamente proporcional ao cycle time — PR mergeado mais rápido = score maior
- **Dias ativos** é a contagem de dias distintos com qualquer atividade (commit, PR ou review)
- Todos os componentes são normalizados para 0–100 antes de aplicar os pesos
- O score final é limitado ao intervalo [0, 100]

**Exemplo de resposta:**
```json
{
  "scoreFinal": 72.50,
  "entrega": 80.00,
  "eficiencia": 66.67,
  "qualidade": 75.00,
  "colaboracao": 62.50,
  "consistencia": 60.00
}
```

## Deploy

O backend está hospedado no **Render** via Docker.

**URL de produção:** `https://gitme-backend.onrender.com`

**Repositório:** `https://github.com/Enzocerq/gitme-backend`

### Render (produção)

O deploy é feito automaticamente via Docker a cada push na branch `main`. Configuração:

| Campo | Valor |
|---|---|
| **Language** | Docker |
| **Health Check Path** | `/health` |
| **Instance Type** | Free |

**Variáveis de ambiente no Render:**

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do Session pooler do Supabase |
| `DB_USERNAME` | `postgres.<project-ref>` |
| `DB_PASSWORD` | Senha do banco |
| `GITHUB_CLIENT_ID` | Client ID do OAuth App |
| `GITHUB_CLIENT_SECRET` | Client Secret do OAuth App |

> O free tier do Render entra em sleep após inatividade. Use o [UptimeRobot](https://uptimerobot.com) com monitor HTTP para `https://gitme-backend.onrender.com/health` em intervalo de 5 minutos para manter o serviço ativo.

---

## Decisões Arquiteturais

- ✓ **Persistência stateful:** Todos os dados em Supabase para auditoria e performance
- ✓ **Modo demo separado:** `DemoController` isola a lógica de demonstração sem poluir endpoints de produção
- ✓ **BatchPreCheck:** 1 SELECT em lote + N INSERTs diretos vs N SELECTs + N INSERTs
- ✓ **GraphQL para Commits:** Mais eficiente que REST, retorna `additions`/`deletions` nativamente
- ✓ **Spring Data Projections:** Aggregations SQL reduzem transferência de dados
- ✓ **OpenAPI/Swagger:** Documentação auto-sincronizada com código
- ✓ **Validação em camada:** JSR-303 + validação de negócio
- ✓ **Tratamento centralizado de erros:** `@RestControllerAdvice` com `ErrorResponse` estruturado

---

**Desenvolvido por:** Felipe de Sousa Alves, Matheus de Oliveira Bezerra & Enzo Girão.
