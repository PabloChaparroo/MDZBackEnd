#!/usr/bin/env node
/*
 * Obtiene el GMAIL_REFRESH_TOKEN para mandar mails por la API de Gmail (MAIL_PROVIDER=gmail-api).
 * Se corre UNA vez, en tu máquina:
 *
 *   node scripts/obtener-token-gmail.js
 *
 * 1. Pide el Client ID y el Client Secret del cliente OAuth "App de escritorio" de Google Cloud
 *    (se escriben acá, no quedan en el historial de la terminal ni en ningún archivo).
 * 2. Abre el navegador: iniciá sesión con la cuenta REMITENTE (mdzmuebles.web@gmail.com) y aceptá.
 *    Google va a avisar "Google no verificó esta app": es esperable (la app es solo para esta cuenta).
 *    Tocá "Configuración avanzada" -> "Ir a ... (no seguro)" -> "Continuar".
 * 3. Imprime el refresh token y manda un mail de prueba a la misma cuenta para confirmar que funciona.
 *
 * El refresh token deja de servir si se revoca el acceso o se cambia la contraseña de la cuenta:
 * en ese caso, volver a correr este script y actualizar GMAIL_REFRESH_TOKEN en Render.
 */
const http = require("http");
const https = require("https");
const readline = require("readline");
const { exec } = require("child_process");

const REMITENTE = "mdzmuebles.web@gmail.com";
const SCOPE = "https://www.googleapis.com/auth/gmail.send";
const PUERTO = 53682;
const REDIRECT = `http://127.0.0.1:${PUERTO}`;

function preguntar(texto) {
  const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
  return new Promise((ok) => rl.question(texto, (r) => { rl.close(); ok(r.trim()); }));
}

function post(url, datos, headers) {
  return new Promise((ok, mal) => {
    const req = https.request(url, { method: "POST", headers }, (res) => {
      let body = "";
      res.on("data", (c) => (body += c));
      res.on("end", () => (res.statusCode < 300 ? ok(JSON.parse(body || "{}")) : mal(new Error(`${res.statusCode}: ${body}`))));
    });
    req.on("error", mal);
    req.end(datos);
  });
}

function esperarCodigo() {
  return new Promise((ok, mal) => {
    const server = http.createServer((req, res) => {
      const url = new URL(req.url, REDIRECT);
      const code = url.searchParams.get("code");
      const error = url.searchParams.get("error");
      res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
      res.end(code ? "<h2>Listo. Ya podés cerrar esta pestaña y volver a la terminal.</h2>"
                   : `<h2>No se autorizó (${error}). Volvé a la terminal.</h2>`);
      server.close();
      code ? ok(code) : mal(new Error(`Google respondió: ${error}`));
    });
    server.listen(PUERTO, "127.0.0.1");
  });
}

(async () => {
  console.log("\n=== Token de Gmail para MDZ Muebles ===\n");
  const clientId = await preguntar("Client ID: ");
  const clientSecret = await preguntar("Client Secret: ");
  if (!clientId || !clientSecret) throw new Error("Faltan el Client ID o el Client Secret.");

  const auth = new URL("https://accounts.google.com/o/oauth2/v2/auth");
  auth.search = new URLSearchParams({
    client_id: clientId, redirect_uri: REDIRECT, response_type: "code", scope: SCOPE,
    access_type: "offline", prompt: "consent", login_hint: REMITENTE,
  }).toString();

  console.log(`\nSe abre el navegador. Iniciá sesión con ${REMITENTE} y aceptá.`);
  console.log(`Si no se abre, copiá esta dirección en el navegador:\n\n${auth}\n`);
  exec(`start "" "${auth}"`);

  const code = await esperarCodigo();
  const form = (o) => new URLSearchParams(o).toString();
  const tokens = await post("https://oauth2.googleapis.com/token",
    form({ code, client_id: clientId, client_secret: clientSecret, redirect_uri: REDIRECT, grant_type: "authorization_code" }),
    { "Content-Type": "application/x-www-form-urlencoded" });

  if (!tokens.refresh_token) {
    throw new Error("Google no devolvió refresh token. Quitá el acceso de la app en https://myaccount.google.com/permissions y volvé a correr el script.");
  }

  // Mail de prueba a la misma cuenta, por el mismo camino que va a usar el backend
  const mime = [
    `From: MDZ Muebles <${REMITENTE}>`, `To: ${REMITENTE}`,
    "Subject: =?UTF-8?B?" + Buffer.from("MDZ Muebles - Prueba de envío por la API de Gmail").toString("base64") + "?=",
    "MIME-Version: 1.0", "Content-Type: text/plain; charset=UTF-8", "Content-Transfer-Encoding: base64", "",
    Buffer.from("Si ves este mail, el envío por la API de Gmail funciona.").toString("base64"),
  ].join("\r\n");
  const raw = Buffer.from(mime).toString("base64url");
  await post("https://gmail.googleapis.com/gmail/v1/users/me/messages/send", JSON.stringify({ raw }),
    { Authorization: `Bearer ${tokens.access_token}`, "Content-Type": "application/json" });

  console.log("✅ Autorizado. Se mandó un mail de prueba a " + REMITENTE + " (revisá esa bandeja).\n");
  console.log("Copiá este valor en Render como GMAIL_REFRESH_TOKEN (no lo pegues en ningún otro lado):\n");
  console.log(tokens.refresh_token + "\n");
})().catch((e) => { console.error("\n❌ " + e.message + "\n"); process.exit(1); });
