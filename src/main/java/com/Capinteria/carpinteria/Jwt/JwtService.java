package com.Capinteria.carpinteria.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// getClaim() pasa un Claims (no-null, viene de getAllClaims()) a un Function<Claims,T>
// generico sin anotar; el analizador de nulabilidad de Eclipse lo marca como
// "unchecked conversion" aunque nunca es null en la práctica.
@SuppressWarnings("null")
@Service
public class JwtService {

    /** Mínimo para HS256: 256 bits. */
    private static final int MIN_KEY_BYTES = 32;

    private final Key signingKey;

    /**
     * La clave sale de la propiedad jwt.secret (en producción, la variable de entorno JWT_SECRET).
     * Se valida al arrancar: sin clave, o con una clave débil, la app no levanta.
     */
    public JwtService(@Value("${jwt.secret:}") String secret) {
        this.signingKey = buildKey(secret);
    }

    static Key buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "Falta la clave de firma de JWT: definí la variable de entorno JWT_SECRET "
                + "(Base64 de al menos 32 bytes aleatorios, por ejemplo: openssl rand -base64 32).");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException e) {
            throw new IllegalStateException("JWT_SECRET no es Base64 válido.", e);
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET es demasiado corta: tiene " + keyBytes.length
                + " bytes y HS256 necesita al menos " + MIN_KEY_BYTES + ".");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user, 1000 * 60 * 15); // 15 minutos
    }

    public String getRefreshToken(UserDetails user) {
        return getToken(new HashMap<>(), user, 1000 * 60 * 60 * 24 * 7); // 7 días
    }

    private String getToken(Map<String, Object> extraClaims, UserDetails user, long expirationMillis) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        return signingKey;
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username=getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired (String token) {
        return getExpiration(token).before(new Date());
    }

    //Agregado para extraer id del cliente
    public Long getIdClienteFromToken(String token) {
        Claims claims = getAllClaims(token);
        Long idCliente = claims.get("idCliente", Long.class); // Reemplaza "idCliente" con la clave real en el token que contiene el ID del cliente.
        return idCliente;
    }

}
