# Project Report - X-Clone

**Course:** Advanced Programming - Summer 2026
**Instructor:** Dr. Saeed Reza Kheradpisheh
**Repository:** `Triple-Force/X-Clone` (Maven `groupId`: `org.tripleforce`)

---

## Table of Contents

1. [System Architecture](#1-system-architecture)
2. [Database Design](#2-database-design)
3. [Object-Oriented Design](#3-object-oriented-design)
4. [AI Usage Disclosure](#4-ai-usage-disclosure)

---

## 1. System Architecture

### 1.1 Overview

X-Clone is a **client–server desktop application**. A single, long-running Java server accepts
concurrent TCP socket connections from any number of JavaFX clients. All durable state lives in
a central PostgreSQL database; each client additionally keeps a small local SQLite cache for
offline/read-optimized access to previously fetched data.

There is no HTTP layer - client and server exchange **JSON-encoded request/response envelopes**
over raw sockets, serialized with Gson.

### 1.2 Client and Server Components

**Client (`Client` package)**
- `MainApp` - JavaFX application entry point and stage bootstrap.
- `NavigationManager` - controls scene/view transitions.
- `controllers/*` - one FXML controller per screen (Login, Register, Timeline, Profile,
  Messages, Tweet item, password reset flow).
- `Service/*` - thin client-side service classes (`AuthClientService`, `TweetClientService`,
  `TimelineClientService`, `RelationClientService`, `MessageClientService`,
  `ConversationClientService`) that build request DTOs and talk to the server through
  `transport.SocketClient`.
- `session.ClientSession` - holds the authenticated session/token for the running client.
- `ClientCacheDatabase` / `ClientDAOManager` - local SQLite-backed cache, sharing the same
  Hibernate entity model as the server's persistence unit.

**Server (`Server` package + `logic_core.infrastructure.transport.server`)**
- `ServerMain` - process entry point; resolves the listening port from CLI args or the
  `XCLONE_SERVER_PORT` environment variable (defaulting to `8888`).
- `SocketServer` - opens the listening socket and hands off each accepted connection.
- `ClientHandler` - runs on a dedicated thread per connected client, reading request envelopes
  and writing response envelopes.
- `RequestDispatcher` - routes each `RequestType` to the appropriate application-layer facade.
- `ServerDAOManager` - manages the server-side JPA `EntityManagerFactory` and connection pool.

**Shared (`Shared` package)**
- JPA entity model (`Shared.Models.*`), the generic DAO (`GenericDAO<T>`), and
  `DatabaseInitializer` classes (`DatabaseCreator`, `DatabaseSeeder`) used to provision and seed
  the schema. This package is compiled into both the client (for its local cache) and the
  server (for the primary database), which is why the two `persistence.xml` units
  (`X-Clone-PU` for PostgreSQL, `X-Clone-Client-Cache-PU` for SQLite) share the same entity
  list.

### 1.3 Communication Flow

1. The client serializes a request DTO (e.g. `CreateTweetRequest`) into a `RequestEnvelope`
   carrying a `RequestType` and a JSON payload, and writes it to its socket.
2. `ClientHandler` on the server reads the envelope and hands it to `RequestDispatcher`.
3. The dispatcher resolves the matching **facade** (e.g. `TweetFacade`), which invokes one or
   more **use cases** (e.g. `CreateTweetUseCase`).
4. The use case validates the request (`TweetValidator`), checks authorization (`InteractionPolicy`),
   performs the operation through a domain **repository**, and publishes a **domain event**
   (`TweetCreatedEvent`) via the `EventBus`.
5. The result is mapped to a response DTO (`CreateTweetResponse`) and wrapped in a
   `ResponseEnvelope`, which is written back to the requesting client's socket.

### 1.4 Architectural Patterns

The `logic_core` module is organized as a **layered / Clean Architecture** system:

| Layer | Package | Responsibility |
|---|---|---|
| Domain | `logic_core.domain` | Entity-agnostic models, repository interfaces, policies, domain events |
| Application | `logic_core.app` | Use cases, facades, DTOs, validators, mappers, security context |
| Infrastructure | `logic_core.infrastructure` | JPA repository implementations, DAOs, the socket transport layer, the event bus |
| Common | `logic_core.common` | Cross-cutting exceptions, the `Result<T>` wrapper, and utilities |

This keeps business logic (use cases, policies) free of any dependency on Hibernate, sockets, or
JSON - those concerns are confined to the infrastructure layer and injected through interfaces,
wired together in `DependencyContainer`.

### 1.5 Concurrency Model

- Each client connection is handled on its own thread (`ClientHandler`), allowing many users to
  be served simultaneously.
- `SessionManager` / `SessionFactory` and `ThreadLocalUserSessionContext` isolate the
  authenticated user's identity per request thread, preventing cross-talk between concurrently
  handled clients.
- Database access is pooled through **HikariCP**, avoiding one physical connection per client
  thread.
- Concurrency correctness is exercised directly by tests such as
  `RobustConcurrentAuthIntegrationTest`, `BrutalAuthConcurrencyTest`,
  `ConcurrentClientSocketIntegrationTest`, and `SessionContextIsolationTest`.

---

## 2. Database Design

### 2.1 Overview

Persistence is implemented with **Hibernate/JPA** against **PostgreSQL**, configured through
`src/main/resources/META-INF/persistence.xml`. UUIDs (`GenerationType.UUID`) are used as primary
keys for all entities, generated through a shared `BaseEntity` superclass that also provides a
`createdAt` timestamp via `@CreationTimestamp`.

### 2.2 Core Entities

| Entity | Purpose |
|---|---|
| `User` | Account, profile, and credential data |
| `Tweet` | Tweet content; also models replies, quotes, and retweets |
| `TweetEdit` | Historical snapshot of a tweet prior to an edit |
| `TweetHashtag`, `Hashtag` | Many-to-many association between tweets and hashtags |
| `TweetMention` | Mentions of users within a tweet |
| `Follow`, `Block`, `Mute` | Directed user-to-user relationship tables |
| `Like` | User-to-tweet interaction |
| `Media` | Attached media metadata for a tweet |
| `Poll`, `PollOption`, `PollVote` | Poll definitions, their options, and cast votes |
| `Conversation`, `ConversationMember`, `DirectMessage` | Direct-messaging domain |
| `Notification` | Per-user notification records |
| `Session` | Persisted authentication sessions |
| `HashtagFollow` | User subscriptions to hashtags |

### 2.3 Relationships and Keys

Association entities that represent a relationship between two other entities
(`Follow`, `Block`, `Mute`, `Like`, `TweetHashtag`, `TweetMention`, `HashtagFollow`,
`ConversationMember`, `PollVote`) use **composite primary keys** (e.g. `FollowId`, `BlockId`,
`LikeId`) rather than a synthetic UUID, since their identity is naturally defined by the pair of
entities they connect. This keeps the relationship tables narrow and lets the database enforce
uniqueness directly on the composite key.

Foreign keys used in timeline and relationship lookups are indexed to keep feed-generation and
relationship-check queries efficient at scale.

### 2.4 Soft Delete and Redaction

Entities that must not be physically removed immediately (most notably `Tweet`) implement the
`SoftDeletable` interface, which defines:

- `isDeleted()` / `setDeleted(boolean)` - the soft-delete flag.
- `redact()` - an optional hook to scrub sensitive content while retaining the row (tweet
  redaction after deletion).
- `onSoftDelete(EntityManager em)` - a hook for cascading side effects at the moment of soft
  deletion.
- `hardDeleteWhere(...)` - a helper for cascading a genuine hard delete of dependent rows
  where retention is not required.

### 2.5 Persistence Configuration

Two persistence units are declared against the same entity list:

- **`X-Clone-PU`** - the server's primary unit, pointed at PostgreSQL, using HikariCP pooling
  (min idle 5 / max pool 20), JDBC batching (`hibernate.jdbc.batch_size=20`), and
  `hibernate.hbm2ddl.auto=update` for schema evolution during development.
- **`X-Clone-Client-Cache-PU`** - a lightweight SQLite unit used by the client for local
  caching, with foreign keys disabled and WAL mode enabled for simpler single-user access.

### 2.6 Query Strategy

Read-heavy paths - most notably timeline generation - avoid loading full entity graphs. Instead,
JPQL **constructor projections** (e.g. `TimelineTweetProjection`) select only the columns needed
to render a timeline row, combined with aggregate subqueries for like/reply/retweet counts and
`EXISTS` checks for block/mute filtering. This is handled through dedicated DAO classes
(`AbstractJpaDao` and per-entity DAOs such as `TweetDao`, `FollowDao`, `LikeDao`) rather than by
traversing lazy-loaded entity associations.

---

## 3. Object-Oriented Design

### 3.1 Key Classes and Responsibilities

- **`BaseEntity`** - abstract superclass providing the UUID identifier, creation timestamp, and
  identity-based `equals`/`hashCode` for all JPA entities.
- **`SoftDeletable`** - interface contract for entities supporting soft delete and redaction.
- **`GenericDAO<T>`** - a generic, reusable persistence helper used by `Shared.Database` for
  simple entity CRUD, reducing duplication across entity-specific access code.
- **`AbstractJpaDao`** - the `logic_core.infrastructure` counterpart used by the application's
  DAOs, standardizing JPQL execution for the query-heavy, use-case-facing layer.
- **`Result<T>` / `Success` / `Failure`** - a discriminated result type used as the return value
  of use cases instead of throwing exceptions for expected failure paths (validation errors,
  not-found, conflicts).
- **Facades** (`AuthFacade`, `TweetFacade`, `TimelineFacade`, `RelationFacade`,
  `ConversationFacade`, `MessageFacade`) - the entry points the transport layer calls into;
  each composes one or more use cases and returns a response DTO.
- **Use cases** (e.g. `RegisterUserUseCase`, `LoginUserUseCase`, `CreateTweetUseCase`,
  `FollowUserUseCase`, `GetTimelineUseCase`, `VotePollUseCase`) - each encapsulates exactly one
  business operation, its validation, and its side effects.

### 3.2 Inheritance, Composition, and Abstraction

- **Inheritance** is used sparingly and deliberately: `BaseEntity` is the sole entity
  superclass; `MutableEntity` / `ImmutableEntity` further distinguish entities whose state can
  change after creation from those that cannot.
- **Composition** dominates the application layer - facades are composed of use cases, use
  cases are composed of repositories/validators/policies, rather than relying on deep
  inheritance chains.
- **Abstraction** is enforced through interfaces at every architectural seam: domain
  repositories (`UserRepository`, `TweetRepository`, …) are interfaces implemented by JPA
  classes in `infrastructure.repository`; `SystemMessageDispatcher`,
  `PasswordResetDeliveryPort`, and `EventPublisher` similarly decouple the application layer
  from a specific delivery/transport mechanism.
- **Polymorphism** appears through the domain event hierarchy (`DomainEvent` and its many
  subclasses per aggregate - `TweetEvent`, `AuthenticationEvent`, `RelationshipEvent`,
  `ConversationEvent`, `MessageEvent`, `PollEvent`) and through the custom exception hierarchy
  (`AppException` as the root of `ValidationException`, `NotFoundException`,
  `ConflictException`, `ForbiddenException`, `DatabaseException`, etc.), letting callers catch
  broadly or narrowly as needed.

### 3.3 Design Patterns Applied

| Pattern | Where |
|---|---|
| Repository | `domain.repository.*` interfaces + `infrastructure.repository.Jpa*Repository` implementations |
| Facade | `logic_core.app.facade.*` |
| Use Case / Command | `logic_core.app.usecase.*` |
| DTO / Mapper | `logic_core.app.dto.*`, `logic_core.app.mapper.*` |
| Observer / Publish-Subscribe | `domain.event.EventBus`, `EventPublisher`, `EventListener`, `AsyncEventBus` |
| Strategy (delivery ports) | `PasswordResetDeliveryPort` with `LoggingPasswordResetDeliveryAdapter` |
| Generic / Template DAO | `GenericDAO<T>`, `AbstractJpaDao` |
| Result / Either | `Result<T>` (`Success` / `Failure`) |
| Dependency Injection (manual) | `DependencyContainer` wiring facades, use cases, and repositories |

---

## 4. AI Usage Disclosure

In compliance with academic integrity standards and to ensure transparency in the project development process, the details regarding the use of Artificial Intelligence (AI) tools in this project are disclosed below:

### 4.1 AI Tool(s) and Model(s) Used
* **Google Gemini (Gemini 3.6 Flash)**

### 4.2 Purpose of Use
The AI tool was primarily utilized for **guidance, educational purposes, brainstorming, and workflow facilitation** across the following areas:

* **Development Roadmap:** Outlining a step-by-step path for developing various modules, prioritizing method implementations, and breaking down complex problems into manageable tasks.
* **Education and Learning:** Guidance on utilizing Scene Builder, constructing FXML code, and managing controller lifecycles in JavaFX.
* **Visual Brainstorming and UI Design:** Assistance with selecting engaging color palettes, element placement, and leveraging design tools to create a modern user interface.
* **CSS Styling:** Designing and providing inline CSS code and stylesheets to enhance the overall visual appeal of graphical elements.
* **Naming Standardization:** Suggesting and refining clear, meaningful, and standardized names for variables, methods, classes, and controllers across the codebase.
* **Version Control Management (Git):** Recommending Git commands and standardized commit messages (following conventional commit guidelines).
* **Debugging and Integration:** Guidance on resolving View-layer issues and connecting UI event handlers to backend services.

### 4.3 Extent and Scope of AI Assistance
The core architecture, primary business logic, service layer, network communication, database structure, and DTOs were entirely designed and implemented by the team/developer. The estimated proportion of AI involvement across different layers is as follows:

* **Consultation, Roadmap, CSS, and Git:** Approximately **70–80%** (The primary focus and largest share of AI contribution was in structuring the development roadmap, visual brainstorming, color palette design, inline CSS styling, and standardizing variable/method naming conventions).
* **FXML Code:** Low (**10–15%**); Graphical layouts were built using Scene Builder, with AI assisting primarily in resolving structural FXML bugs and applying specific tags.
* **Controller Code:** Limited (**15–20%**); Mainly focused on structural suggestions for controllers and advice on invoking backend service methods.

### 4.4 Practical Application Examples

* **Development Roadmap Creation:**
  * *Example:* Defining a step-by-step roadmap for implementing the Profile module—ranging from initial FXML design in Scene Builder to controller creation, DTO integration, and handling followers/following lists sequentially.

* **Color Selection, CSS Styling, and UI Brainstorming:**
  * *Example:* Designing the application's visual theme by proposing a modern color scheme (primary text `#0f1419`, brand button color `#1d9bf0`, background `#f7f9fa`) and providing CSS properties such as drop-shadow effects and rounded corners (`-fx-background-radius: 20`).

* **Standardized Naming Across the Codebase:**
  * *Example:* Guiding the selection of consistent and descriptive identifiers across all files (e.g., using `followersCountLabel` instead of ambiguous names, or `handleShowFollowers` for event handler methods).

* **Scene Builder Learning & FXML Troubleshooting:**
  * *Example:* Demonstrating the use of `<clip>` with a `<Circle>` shape to achieve circular profile images (`ImageView`) in Scene Builder, and resolving background-radius application issues on images within `UserItem.fxml` and `Profile.fxml`.

### 4.5 Review, Modification, and Validation
All AI-generated suggestions, roadmaps, and guidance were reviewed, validated, and executed through the following processes:

1. **Architectural Alignment:** All recommendations were carefully reviewed to ensure seamless integration with the existing `ClientApplicationContext` and custom service layer architecture.
2. **Scene Builder Evaluation:** Proposed FXML modifications and visual styles were tested and verified inside Scene Builder to guarantee correct layout structure and responsiveness.
3. **Comprehension & Ownership:** The implementation logic, JavaFX threading rules (such as `Platform.runLater`), and design patterns were fully understood and mastered to ensure complete readiness for project presentation and evaluation.