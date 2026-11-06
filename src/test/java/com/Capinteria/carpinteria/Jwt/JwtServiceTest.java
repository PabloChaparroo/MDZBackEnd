package com.Capinteria.carpinteria.Jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Clave de firma de JWT: se exige por configuración y un token de otra clave no se acepta. */
class JwtServiceTest {

    private static final UserDetails ADMIN = User.withUsername("admin@mdz.local").password("x").authorities(List.of()).build();

    private static String claveAleatoria(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    @Test
    void sinClaveLaAppNoArranca() {
        assertThatThrownBy(() -> new JwtService("")).isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void conClaveCortaLaAppNoArranca() {
        assertThatThrownBy(() -> new JwtService(claveAleatoria(16)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("demasiado corta");
    }

    @Test
    void conClaveQueNoEsBase64LaAppNoArranca() {
        assertThatThrownBy(() -> new JwtService("esto no es base64 !!!")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unTokenFirmadoConLaMismaClaveEsValido() {
        JwtService service = new JwtService(claveAleatoria(32));

        String token = service.getToken(ADMIN);

        assertThat(service.getUsernameFromToken(token)).isEqualTo("admin@mdz.local");
        assertThat(service.isTokenValid(token, ADMIN)).isTrue();
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        // Es el ataque que permitía la clave hardcodeada: fabricar un token "de admin" con una clave conocida.
        JwtService produccion = new JwtService(claveAleatoria(32));
        JwtService atacante = new JwtService(claveAleatoria(32));

        String tokenFalso = atacante.getToken(ADMIN);

        assertThatThrownBy(() -> produccion.getUsernameFromToken(tokenFalso))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    void laClaveViejaDelRepoYaNoSirveConUnaClaveNueva() {
        // La clave que estuvo hardcodeada (y quedó en el historial de git) no puede firmar tokens aceptados.
        JwtService claveFiltrada = new JwtService("586E3272357538782F413F4428472B4B6250655368566B597033733676397924");
        JwtService produccion = new JwtService(claveAleatoria(32));

        String tokenFalso = claveFiltrada.getToken(ADMIN);

        assertThatThrownBy(() -> produccion.getUsernameFromToken(tokenFalso)).isInstanceOf(RuntimeException.class);
    }
}
