package br.com.construafacil.service;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public CustomUserDetailsService(ClienteRepository clienteRepository,
                                    ProfissionalRepository profissionalRepository) {
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(username);
        if (cliente != null) {
            return User.builder()
                    .username(cliente.getEmail())
                    .password(cliente.getSenha()) // texto puro (NoOp)
                    .roles("CLIENTE")
                    .build();
        }

        Profissional profissional = profissionalRepository.findByEmail(username);
        if (profissional != null) {
            return User.builder()
                    .username(profissional.getEmail())
                    .password(profissional.getSenha()) // texto puro (NoOp)
                    .roles("PROFISSIONAL")
                    .build();
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}
