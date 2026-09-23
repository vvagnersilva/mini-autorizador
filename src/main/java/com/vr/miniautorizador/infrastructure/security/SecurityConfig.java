package com.vr.miniautorizador.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguranca da API: autenticacao HTTP Basic com um unico usuario tecnico
 * (contrato: login = username, senha = password), sem sessao (API stateless).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
                                                  @Value("${app.seguranca.usuario}") String usuario,
                                                  @Value("${app.seguranca.senha}") String senha) {
        return new InMemoryUserDetailsManager(
                User.withUsername(usuario)
                        .password(passwordEncoder.encode(senha))
                        .roles("MAQUININHA")
                        .build());
    }

    /**
     * CSRF desabilitado de proposito: a API e stateless (sem sessao e sem cookies) e cada
     * requisicao se autentica via cabecalho HTTP Basic, que o navegador nao envia sozinho
     * em uma requisicao forjada. Sem cookie de sessao, nao ha o que um ataque CSRF explorar.
     */
    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> {})
                .build();
    }
}
