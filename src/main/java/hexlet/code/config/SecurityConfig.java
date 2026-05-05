package hexlet.code.config;

import hexlet.code.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public SecurityConfig(JwtDecoder jwtDecoder, PasswordEncoder passwordEncoder, UserService userService) {
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Configures the security filter chain.
     * Sets up authorization rules, session management, CSRF, CORS, and JWT resource server.
     * @param http HttpSecurity configuration.
     * @param env Environment variables.
     * @return SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment env)
        throws Exception {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("production");

        if (!isProd) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console", "/h2-console/**").permitAll()
                ).headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
        }

        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/task_statuses", "/api/task_statuses/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/welcome").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico").permitAll()
                .requestMatchers("/index.html").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.decoder(this.jwtDecoder)))
            .httpBasic(Customizer.withDefaults())
            .build();
    }

    /**
     * Configures the AuthenticationManager bean.
     * @param http HttpSecurity configuration.
     * @return AuthenticationManager instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    /**
     * Configures the DaoAuthenticationProvider bean.
     * Uses custom UserService and PasswordEncoder.
     * @param auth AuthenticationManagerBuilder configuration.
     * @return AuthenticationProvider instance.
     */
    @Bean
    public AuthenticationProvider daoAuthProvider(AuthenticationManagerBuilder auth) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(this.userService);
        provider.setPasswordEncoder(this.passwordEncoder);
        return provider;
    }
}
