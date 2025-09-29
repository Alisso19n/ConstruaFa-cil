package br.com.construafacil.construafacil.config;

import br.com.construafacil.service.CustomAuthenticationSuccessHandler;
import br.com.construafacil.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;   // <<< AQUI
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
public class SegurancaConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SegurancaConfig(CustomUserDetailsService customUserDetailsService,
                           CustomAuthenticationSuccessHandler successHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Agora usamos BCrypt (seguro)
        return new BCryptPasswordEncoder();
    }

    // ADMIN em memória (para não perder acesso ao painel)
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin@local")
                .password(encoder.encode("123")) // senha "123" com BCrypt
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public DaoAuthenticationProvider daoProviderBanco(PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(customUserDetailsService);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public DaoAuthenticationProvider daoProviderMemoria(PasswordEncoder encoder,
                                                        InMemoryUserDetailsManager inMemory) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(inMemory);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoProviderBanco,
                                                       DaoAuthenticationProvider daoProviderMemoria) {
        return new ProviderManager(List.of(daoProviderBanco, daoProviderMemoria));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager) throws Exception {
        http
                .authenticationManager(authManager)
                .authorizeHttpRequests(auth -> auth
                        // PÚBLICO
                        .requestMatchers(HttpMethod.GET, "/", "/index", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/catalogo", "/catalogo/**").permitAll()
                        .requestMatchers("/login").permitAll()

                        // CADASTROS/ATALHOS PÚBLICOS
                        .requestMatchers(
                                "/clientes/cadastro", "/profissionais/cadastro",
                                "/cliente/cadastro", "/profissional/cadastro",
                                "/cadastro", "/cadastro/**",
                                "/cadastro-cliente", "/cadastro-cliente/**",
                                "/cadastro-profissional", "/cadastro-profissionais", "/cadastro-profissionais/**",
                                "/sou-cliente", "/quero-ser-cliente",
                                "/sou-profissional", "/quero-ser-profissional"
                        ).permitAll()

                        // ESTÁTICOS e H2
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // ÁREAS PROTEGIDAS
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/clientes/**").authenticated()
                        .requestMatchers("/profissionais/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()));

        return http.build();
    }
}
