package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class CadastroClienteController {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroClienteController(ClienteRepository clienteRepository,
                                     PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@ModelAttribute("cliente") Cliente cliente, Model model) {
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            model.addAttribute("erro", "Informe um e-mail válido.");
            return "cadastro-cliente";
        }
        if (clienteRepository.findByEmail(cliente.getEmail()) != null) {
            model.addAttribute("erro", "Já existe um cliente com esse e-mail.");
            return "cadastro-cliente";
        }
        // BCrypt na senha
        if (cliente.getSenha() != null && !cliente.getSenha().isBlank()) {
            cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }
        cliente.setStatus("ATIVO");
        clienteRepository.save(cliente);
        return "redirect:/login?cadastroSucesso";
    }
}
