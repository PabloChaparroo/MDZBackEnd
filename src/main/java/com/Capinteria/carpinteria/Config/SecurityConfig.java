package com.Capinteria.carpinteria.Config;


import com.Capinteria.carpinteria.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                //Rutas publicas:

                                .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll() //HABILITACION GLOBAL
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                                //.requestMatchers(new AntPathRequestMatcher("api/v1/mueble")).hasAuthority("CLIENTE")
                                //.requestMatchers(new AntPathRequestMatcher("api/v1/categoria")).hasAuthority("CLIENTE")
                                //.requestMatchers(HttpMethod.PUT , "/api/v1/categoria").hasAuthority("CLIENTE")



                                .requestMatchers(new AntPathRequestMatcher("/auth/register")).permitAll() //Registro Cliente
                                 /*
                                .requestMatchers(new AntPathRequestMatcher("/auth/login")).permitAll() //Login general
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/articuloInsumo/paged")).permitAll() //de articuloInsumo
                                .requestMatchers(new AntPathRequestMatcher("api/v1/articuloInsumo/searchByNombre")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("api/v1/articuloInsumo/searchByRubroNombre")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/articuloManufacturado/paged")).permitAll() //de articuloManufacturado
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/articuloManufacturado/{id}")).permitAll()

                                .requestMatchers(new AntPathRequestMatcher("api/v1/articuloManufacturado/searchByNombre")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("api/v1/articuloManufacturado/searchByPrecioVentaRange")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("api/v1/articuloManufacturado/searchByCategoriaNombre")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/DetalleProductoManufacturado/paged")).permitAll() //de DetalleProductoManufacturado
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/rubro/paged")).permitAll() //de rubro
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/rubro/searchByNombre")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/CategoriaArticuloManufacturado/paged")).permitAll() //de categoriaArticuloManufacturado
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/Localidad/paged")).permitAll() //de Localidad
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/UnidadMedida/paged")).permitAll() //de UnidadMedida

                                //TEST
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/pedido/paged")).permitAll() //de pedido
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/factura/paged")).permitAll() //de factura

                                 */



                                //Segun el rol
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/showProfile")).hasAnyAuthority( "CLIENTE")
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/updateProfile")).hasAnyAuthority("CLIENTE")

                                .requestMatchers(new AntPathRequestMatcher("/auth/registerEmployee")).hasAuthority("ADMIN") //Autenticacion
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/modifyCliente")).hasAuthority("ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/deleteCliente")).hasAuthority("ADMIN")


                )

                .sessionManagement(sessionManager->
                        sessionManager
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
/*
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(List.of("Authorization", "Access-Control-Allow-Origin", "Content-Type",
                "X-Requested-With", "accept", "Origin", " Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
    configuration.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}


}
