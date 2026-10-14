package com.coruja.ocorrencias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.coruja.ocorrencias.security.LoginAttemptService;
import com.coruja.ocorrencias.security.LoginAuthenticationProvider;

@Configuration
public class SecurityConfig {
        @Bean
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.frontend-origin:http://localhost:5173}") String frontendOrigin) {
                CorsConfiguration cors = new CorsConfiguration();
                cors.setAllowedOrigins(List.of(frontendOrigin));
                cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN"));
                cors.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", cors);
                return source;
        }

        @Bean
        public AuthenticationEventPublisher authenticationEventPublisher(
                        ApplicationEventPublisher applicationEventPublisher) {

                return new DefaultAuthenticationEventPublisher(
                                applicationEventPublisher);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(Customizer.withDefaults())
                                .requestCache(cache -> cache.disable())

                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/auth/csrf", "/login").permitAll()
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .usernameParameter("email")
                                                .successHandler((request, response, authentication) -> response.setStatus(204))
                                                .failureHandler((request, response, exception) -> {
                                                        response.setStatus(401);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"mensagem\":\"Nao foi possivel autenticar\"}");
                                                })
                                                .permitAll())

                                .exceptionHandling(errors -> errors
                                                .authenticationEntryPoint((request, response, exception) -> {
                                                        response.setStatus(401);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"mensagem\":\"Autenticacao necessaria\"}");
                                                })
                                                .accessDeniedHandler((request, response, exception) -> {
                                                        response.setStatus(403);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"mensagem\":\"Acesso negado\"}");
                                                }))

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation(fixation -> fixation.changeSessionId()))

                                .logout(logout -> logout
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204))
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        LoginAttemptService tentativas) {

                return new LoginAuthenticationProvider(userDetailsService, passwordEncoder, tentativas);

        }
}
