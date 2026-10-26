# Ticket Triage Agent

Spring Boot 4.0.2 / Java 21 REST API for ticket management and AI-assisted triage using Google Gemini through Spring AI.

## Use case

Support tickets often arrive as unstructured text. This application stores those tickets, sends an eligible ticket to Gemini for analysis, validates the returned decision, and stores the triage result for later review or operational processing.

The application is not a general-purpose chatbot. It is a REST-based classification and review workflow.

## End-to-end flow

```text
Create or import ticket
        ↓
Validate and save ticket in tickets
        ↓
Request single or batch triage
        ↓
Apply deterministic safety checks
        ↓
Summarize oversized descriptions when necessary
        ↓
Select Gemini model and strategy
        ↓
Call Gemini for structured triage output
        ↓
Retry transient failures and use fallback model
        ↓
Validate category, priority, fields, and confidence
        ↓
Mark low-confidence results for review
        ↓
Save result in ticket_triage_results
```

The AI proposes a classification. It does not directly execute database administration, send messages, delete records, or perform unrestricted external actions.

## Triage output

Each successful result contains:

- Category: `PAYMENT`, `AUTHENTICATION`, `PERFORMANCE`, `BUG`, `FEATURE_REQUEST`, `INFRASTRUCTURE`, `SECURITY`, or `OTHER`
- Priority: `P1`, `P2`, `P3`, or `P4`
- Assigned team
- Concise summary
- Recommended action
- Confidence from `0.0` to `1.0`
- Gemini model used

## Database structure

The application maps the existing tables and uses `spring.jpa.hibernate.ddl-auto=validate` so Hibernate validates mappings without changing the schema.

### `tickets`

| Column | Purpose |
|---|---|
| `id` | Auto-increment ticket identifier |
| `title` | Short ticket title |
| `description` | Full customer/support description |
| `category` | Optional stored category |
| `customer_impact` | Impact scope such as one or many customers |
| `status` | `OPEN`, `IN_PROGRESS`, `RESOLVED`, or `CLOSED` |
| `created_at` | Creation timestamp |

### `ticket_triage_results`

| Column | Purpose |
|---|---|
| `id` | Triage result identifier |
| `ticket_id` | Foreign-key link to `tickets.id` |
| `category` | AI or rule-derived category |
| `priority` | `P1` through `P4` |
| `assigned_team` | Suggested team |
| `summary` | Concise issue summary |
| `recommended_action` | Suggested next action |
| `confidence` | Model confidence signal |
| `model_name` | Gemini model selected |
| `created_at` | Result creation timestamp |

Multiple triage results can be retained for the same ticket. `GET /api/tickets/{id}/triage` returns the latest one.

## Guardrails

The triage flow includes:

- Ticket content is treated as untrusted input; instructions inside a ticket cannot override the system rules.
- Oversized descriptions are summarized before classification. The default limit is 300 words, configurable with `TRIAGE_MAX_DESCRIPTION_WORDS`.
- Categories and priorities are validated against allow-lists.
- Required output fields must be present.
- Confidence must be between `0.0` and `1.0`.
- Security indicators such as `data breach`, `hacked`, or `stolen credentials` force `SECURITY` and `P1`.
- Low-confidence results below `0.70` move the ticket to `IN_PROGRESS` for review. Configure this with `TRIAGE_CONFIDENCE_REVIEW_THRESHOLD`.
- AI calls have a bounded timeout. Configure it with `TRIAGE_REQUEST_TIMEOUT_SECONDS`.
- The system retries only a bounded number of times and never retries indefinitely.
- Failed or malformed AI responses are not persisted as successful triage results.

## Model selection and fallback

Normal tickets use the configured fast model. High-impact or security-related tickets use the strong model. If the selected model fails, the application retries it once and then tries the fallback model.

Default configuration:

```yaml
app:
  triage:
    fast-model: gemini-3.5-flash-lite
    strong-model: gemini-3.5-flash-lite
    fallback-model: gemini-3.1-flash-lite
    max-retries: 1
```

Override these with `GEMINI_FAST_MODEL`, `GEMINI_STRONG_MODEL`, `GEMINI_FALLBACK_MODEL`, and `TRIAGE_MAX_RETRIES`. If all attempts fail, the API returns an error and does not fabricate or save a result.

## API documentation

The project uses `springdoc-openapi-starter-webmvc-ui` 3.2.0, which is the Spring Boot 4-compatible springdoc line.

After starting the application, open:

- Swagger UI: <http://localhost:8081/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8081/v3/api-docs>

The OpenAPI document is generated from the implemented controllers and DTO validation constraints. As ticket controllers and DTOs are added, their CRUD, triage, request/response, validation, and status-code documentation should be verified in Swagger UI. No credentials or API keys are included in the OpenAPI metadata.

## Run locally

```bash
./mvnw clean test
./mvnw spring-boot:run
```

The current application uses Google Gemini through Spring AI. Set `GEMINI_API_KEY` and optionally `GEMINI_MODEL` in the environment. Keep Gemini keys out of source control and logs.

For local MySQL overrides, use the ignored `src/main/resources/application-local.yml` and run with `--spring.profiles.active=local`, or provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `GEMINI_API_KEY` as environment variables. Tests use an in-memory H2 database and do not call Gemini.

## Ticket endpoints

- `GET /api/tickets` — paginated ticket list
- `GET /api/tickets/{id}` — ticket details
- `POST /api/tickets` — create a ticket
- `GET /api/tickets/{id}/triage` — latest saved triage result
- `POST /api/tickets/{id}/triage` — invoke Gemini triage and persist the result
- `POST /api/triage/batch` — triage up to 50 open tickets and report partial failures

## Lombok

Lombok is retained as an optional dependency and is configured as a Java 21 Maven annotation processor. Prefer constructor injection, `@Getter`/`@Setter` for mutable DTOs and entities, and avoid `@Data` on JPA entities.
