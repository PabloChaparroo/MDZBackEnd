---
name: backend-context
description: Context, architecture and dev-environment conventions for the MDZMuebles backend (MDZBackEnd) — Spring Boot/Java, Docker/MySQL, and how "npm run dev" orchestrates everything. Load this before making changes to this repo, especially anything touching Docker, ports, the database, or the dev startup flow.
---

# MDZBackEnd — system context

This is the backend for **MDZMuebles**, a furniture-store ("carpintería") management system. It is a **Java/Spring Boot** project, not a Node project — `npm`/`package.json` exist only as a thin orchestration layer for local dev (see below), not as the app's runtime.

## Stack

- **Java 17** (declared in `pom.xml`), built/run with **JDK 21** (Eclipse Temurin) actually installed on this machine — the Dockerfile's base image is also JDK 21. Maven via the wrapper (`mvnw` / `mvnw.cmd`), no system-wide Maven needed.
- **Spring Boot 3.2.3**: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-devtools` (hot reload).
- **MySQL** via `mysql-connector-j`, `org.hibernate.dialect.MySQL8Dialect`.
- **Auth**: JWT via `io.jsonwebtoken` (jjwt), Spring Security filter chain (see `JwtAuthenticationFilter`, `SecurityConfig`).
- **ModelMapper** for DTO/entity mapping.
- Domain entities live under `src/main/java/com/Capinteria/carpinteria/` — `Cliente`, `Mueble`, `Categoria`, `Pedido`, `Factura`, `Usuario`, `Domicilio`, etc., with matching Controllers/Services/Repositories and an `Auth` package for login/register.

## Database / schema strategy — no migration tool

`spring.jpa.hibernate.ddl-auto=create` in `src/main/resources/application.properties` means **Hibernate drops and recreates the entire schema from the `@Entity` classes on every application startup**. There is no Flyway/Liquibase in this project — that was a deliberate choice (offered and declined) when the dev workflow was set up. Practical consequences:

- **All data is wiped on every restart.** Do not expect data to persist between `npm run dev` runs.
- There are no versioned SQL migration files anywhere in the repo — the schema only exists implicitly, as whatever the current entity classes describe.
- If a future task calls for real migrations, that's a deliberate architecture change (add Flyway, set `ddl-auto=validate`, generate initial SQL) — don't assume it's already half-done.

## Local dev environment: Docker scope is DB-only

Only **MySQL** runs in Docker for local development. The Spring Boot app itself runs **locally** via `mvnw spring-boot:run`, not containerized — this was chosen specifically so Spring Boot DevTools hot-reload keeps working (a fully containerized backend would need a rebuild+restart on every code change).

- `compose.dev.yaml` — dev-only, defines just the `db` service (`mysql:8.0`, database `carpinteriadb`, user `root`/password `root`, port `3306`, healthcheck, persistent volume). This is what `npm run dev` uses.
- `compose.yaml` / `compose.debug.yaml` — a separate, full containerized setup that builds the backend from `Dockerfile` and expects a **pre-built jar** at `target/carpinteria-0.0.1-SNAPSHOT.jar`. This is closer to a prod-like run, not the everyday dev loop. `compose.debug.yaml` additionally exposes a remote JDWP debug port (5005).

### `npm run dev`

`package.json` + `scripts/dev.js` exist purely to give a one-command dev startup (`npm run dev`) on top of the Java project:
1. Checks `docker info`; if Docker Desktop isn't running, tries to launch it (Windows/macOS) and polls until it's up.
2. Runs `docker compose -f compose.dev.yaml up -d --wait` to create/start the MySQL container and wait for it to be healthy.
3. Runs `mvnw spring-boot:run` (Windows: invoked as an absolute-quoted-path single shell command — plain `spawnSync('mvnw.cmd', args, {shell:true})` does NOT reliably find `.cmd` files in the cwd on this machine, see comments in `scripts/dev.js`). Hibernate creates the schema as part of this step — there is no separate "migrate" command.

Other scripts: `npm run db:up` / `db:down` / `db:logs` operate on `compose.dev.yaml` only.

## Ports — deliberately non-default

The backend listens on **port 8081** (`server.port` in `application.properties`, matching `EXPOSE`/port mappings in `Dockerfile`, `compose.yaml`, `compose.debug.yaml`) — **not** Spring's default 8080, and not port 3000 (which the Docker files originally, inconsistently, exposed without anything actually configuring the app to listen there).

This is a deliberate, user-confirmed choice, not the framework default — 8080 and 3000 are routinely occupied on this developer's machine by two unrelated sibling projects that are often running at the same time:
- `Inventia/FrontEndInventia` (Vite) → port 8080
- `Inventia/BackEndInventia` (Node) → port 3000

Keep port 8081 (or ask before changing it) rather than reverting to 3000/8080 — those will likely collide again.

## Related repos on this machine

- Frontend for this same product: `MDZMuebles/FrontEnd/MDZFrontEnd` (separate repo, listed as an additional working directory in this session).
- **Not related to MDZMuebles at all**: `Inventia/FrontEndInventia` and `Inventia/BackEndInventia` — different product, same developer, just worth knowing they exist and commonly occupy 8080/3000 (see Ports above).

## Known gotchas

- **JDK install + PATH/JAVA_HOME on Windows**: a JDK (Eclipse Temurin 21) was installed via `winget` and `JAVA_HOME`/`PATH` were set at the **Machine** environment level. Any terminal/process that was already open before the install does **not** see the new PATH (Windows env vars are inherited at process-creation time, not live-reloaded) — a genuinely new terminal window picks it up fine. If `mvnw`/`java` suddenly "isn't recognized" mid-session, that's almost always this, not a broken install.
- **`.gitignore` was added late**: the repo's first commit predates it, so `target/` (Maven build output, including compiled `.class` files) is still tracked in git. It wasn't untracked (`git rm --cached`) to avoid rewriting history without being asked — flag this to the user if it becomes relevant, but don't silently untrack it.
- Node `spawnSync(..., {shell:true})` on Windows can fail to resolve a bare `mvnw.cmd` (cmd.exe "no se reconoce como un comando") depending on the environment; `scripts/dev.js` works around this by building an absolute, quoted path and passing it as a single command string.
