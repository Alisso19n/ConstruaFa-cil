package br.com.construafacil.service;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    // INJETE SÓ AQUI, NÃO USE @Autowired EM NENHUM CAMPO
    public CustomUserDetailsService(
            ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;

        // Inicializa admin in-memory
        this.inMemoryUserDetailsManager = new InMemoryUserDetailsManager(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (inMemoryUserDetailsManager.userExists(username)) {
            return inMemoryUserDetailsManager.loadUserByUsername(username);
        }
        Cliente cliente = clienteRepository.findByEmail(username);
        if (cliente != null && "ATIVO".equals(cliente.getStatus())) {
            return User.withUsername(cliente.getEmail())
                    .password(cliente.getSenha())
                    .roles("CLIENTE")
                    .build();
        }
        Profissional profissional = profissionalRepository.findByEmail(username);
        if (profissional != null && "ATIVO".equals(profissional.getStatus())) {
            return User.withUsername(profissional.getEmail())
                    .password(profissional.getSenha())
                    .roles("PROFISSIONAL")
                    .build();
        }
        throw new UsernameNotFoundException("Usuário não encontrado ou não aprovado.");
    }
}
