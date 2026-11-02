#!/usr/bin/env node
/**
 * Orquesta el entorno de desarrollo:
 * 1. Verifica que Docker esté corriendo (lo intenta iniciar si no).
 * 2. Levanta el contenedor de MySQL definido en compose.dev.yaml.
 * 3. Inicia el backend Spring Boot con el perfil 'local' (application-local.properties),
 *    para no depender de las variables DB_HOST/DB_PORT/etc. que usa Docker/producción.
 */
const { spawn, spawnSync } = require("child_process");
const fs = require("fs");
const path = require("path");

const isWindows = process.platform === "win32";
const root = path.resolve(__dirname, "..");

function run(cmd, args) {
  const result = spawnSync(cmd, args, {
    stdio: "inherit",
    cwd: root,
  });
  if (result.error) throw result.error;
  return result.status;
}

// En Windows, spawnSync con shell:true resuelve el comando a través de
// cmd.exe, que no siempre busca .cmd/.bat en el directorio actual. Usar
// una ruta absoluta entre comillas, como un único string, evita ese problema.
function runShellCommand(commandString) {
  const result = spawnSync(commandString, {
    stdio: "inherit",
    cwd: root,
    shell: true,
  });
  if (result.error) throw result.error;
  return result.status;
}

function dockerIsRunning() {
  const result = spawnSync("docker", ["info"], { stdio: "ignore" });
  return result.status === 0;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function ensureDockerRunning() {
  if (dockerIsRunning()) {
    console.log("Docker ya está en ejecución.");
    return;
  }

  console.log("Docker no está corriendo. Intentando iniciar Docker Desktop...");

  if (isWindows) {
    spawn(
      "cmd",
      ["/c", "start", '""', '"C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe"'],
      { detached: true, stdio: "ignore" }
    ).unref();
  } else if (process.platform === "darwin") {
    spawn("open", ["-a", "Docker"], { detached: true, stdio: "ignore" }).unref();
  } else {
    console.error(
      'No se pudo iniciar Docker automáticamente en este sistema operativo.\n' +
      'Iniciá el servicio de Docker manualmente y volvé a correr "npm run dev".'
    );
    process.exit(1);
  }

  const timeoutMs = 90_000;
  const intervalMs = 3000;
  const start = Date.now();

  while (Date.now() - start < timeoutMs) {
    await sleep(intervalMs);
    if (dockerIsRunning()) {
      console.log("Docker Desktop está listo.");
      return;
    }
    console.log("Esperando a que Docker Desktop termine de iniciar...");
  }

  console.error(
    'Docker Desktop no arrancó a tiempo. Iniciálo manualmente y volvé a correr "npm run dev".'
  );
  process.exit(1);
}

// Con --smtp-real usa el SMTP del .env en lugar de Mailpit. SPRING_APPLICATION_JSON
// tiene prioridad sobre application-local.properties.
function usarSmtpReal() {
  const envPath = path.join(root, ".env");
  if (!fs.existsSync(envPath)) {
    console.error("No se encontró el archivo .env en la carpeta del backend.");
    process.exit(1);
  }

  const env = {};
  for (const linea of fs.readFileSync(envPath, "utf8").split(/\r?\n/)) {
    const match = linea.match(/^\s*([A-Z_]+)\s*=\s*(.*?)\s*$/);
    if (match) env[match[1]] = match[2].replace(/^(["'])(.*)\1$/, "$2");
  }

  const faltantes = ["MAIL_HOST", "MAIL_USERNAME", "MAIL_PASSWORD", "MAIL_ADMIN"].filter((k) => !env[k]);
  if (faltantes.length > 0) {
    console.error(`Faltan estos datos en el .env: ${faltantes.join(", ")}`);
    process.exit(1);
  }

  process.env.SPRING_APPLICATION_JSON = JSON.stringify({
    "spring.mail.host": env.MAIL_HOST,
    "spring.mail.port": env.MAIL_PORT || "587",
    "spring.mail.username": env.MAIL_USERNAME,
    "spring.mail.password": env.MAIL_PASSWORD,
    "spring.mail.properties.mail.smtp.auth": "true",
    "spring.mail.properties.mail.smtp.starttls.enable": "true",
    "mdz.mail.from": env.MAIL_FROM || env.MAIL_USERNAME,
    "mdz.mail.admin": env.MAIL_ADMIN,
  });
  console.log(`SMTP real activado (${env.MAIL_HOST}): los mails salen de verdad.`);
}

async function main() {
  if (process.argv.includes("--smtp-real")) usarSmtpReal();

  await ensureDockerRunning();

  console.log("\nLevantando MySQL (Docker: compose.dev.yaml)...");
  const composeStatus = run("docker", [
    "compose",
    "-f",
    "compose.dev.yaml",
    "up",
    "-d",
    "--wait",
  ]);
  if (composeStatus !== 0) {
    console.error("No se pudo levantar el contenedor de MySQL. Revisá los logs con: npm run db:logs");
    process.exit(composeStatus ?? 1);
  }
  console.log("MySQL listo en localhost:3306 (base de datos: mdzmueblesbd).");

  console.log(
    "\nIniciando el backend (Spring Boot, perfil 'local'). El esquema de la base de " +
    "datos se actualiza automáticamente al arrancar (ddl-auto=update)...\n"
  );
  const mvnwPath = path.join(root, isWindows ? "mvnw.cmd" : "mvnw");
  const springArgs = "-Dspring-boot.run.profiles=local";
  const backendStatus = isWindows
    ? runShellCommand(`"${mvnwPath}" spring-boot:run ${springArgs}`)
    : run(mvnwPath, ["spring-boot:run", springArgs]);
  process.exit(backendStatus ?? 0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
