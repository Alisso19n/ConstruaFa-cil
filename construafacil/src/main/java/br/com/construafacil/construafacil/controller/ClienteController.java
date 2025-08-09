package br.com.construafacil.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public ClienteController(ClienteRepository clienteRepository,
                             SolicitacaoRepository solicitacaoRepository,
                             PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // EXIBE o formulário de cadastro!
    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }

    // PROCESSA o cadastro (POST)
    @PostMapping("/cadastro")
    public String processarCadastro(@ModelAttribute Cliente cliente) {
        cliente.setStatus("ATIVO");
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        clienteRepository.save(cliente);
        return "redirect:/login?cadastradoClienteSucesso";
    }

    // Painel do cliente
    @GetMapping("/painel")
    public String painelCliente(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByCliente(cliente);
        model.addAttribute("cliente", cliente);
        model.addAttribute("solicitacoes", solicitacoes);
        return "painel-cliente";
    }

    // Formulário de edição
    @GetMapping("/editar")
    public String editarCliente(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        model.addAttribute("cliente", cliente);
        return "editar-cliente";
    }

    // Processa edição
    @PostMapping("/editar")
    public String salvarEdicaoCliente(@ModelAttribute Cliente clienteEditado, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        cliente.setNome(clienteEditado.getNome());
        cliente.setTelefone(clienteEditado.getTelefone());
        // Outros campos editáveis, se necessário
        clienteRepository.save(cliente);
        return "redirect:/clientes/painel?editado";
    }

    // Confirmação de exclusão
    @GetMapping("/excluir")
    public String confirmarExclusao(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        model.addAttribute("cliente", cliente);
        return "confirmar-exclusao-cliente";
    }

    @PostMapping("/excluir")
    public String excluirCliente(Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        if (cliente != null) {
            // Exclui todas as solicitações desse cliente primeiro!
            solicitacaoRepository.deleteAll(solicitacaoRepository.findByCliente(cliente));
            clienteRepository.delete(cliente);
        }
        return "redirect:/logout";
    }
}