package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    // GET do cadastro (exibe formulário)
    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }

    // POST do cadastro (processa formulário)
    @PostMapping("/cadastro")
    public String processarCadastro(@ModelAttribute("cliente") Cliente cliente, Model model) {
        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            model.addAttribute("erro", "Informe uma senha válida.");
            return "cadastro-cliente";
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            model.addAttribute("erro", "Informe um e-mail válido.");
            return "cadastro-cliente";
        }
        if (clienteRepository.findByEmail(cliente.getEmail()) != null) {
            model.addAttribute("erro", "Já existe um cliente com esse e-mail.");
            return "cadastro-cliente";
        }

        cliente.setStatus("ATIVO");
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        clienteRepository.save(cliente);

        return "redirect:/login?cadastroSucesso";
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

    // GET editar
    @GetMapping("/editar")
    public String editarCliente(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        model.addAttribute("cliente", cliente);
        return "editar-cliente";
    }

    // POST editar (não altera a senha aqui)
    @PostMapping("/editar")
    public String salvarEdicaoCliente(@ModelAttribute("cliente") Cliente clienteEditado, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        cliente.setNome(clienteEditado.getNome());
        cliente.setTelefone(clienteEditado.getTelefone());
        clienteRepository.save(cliente);
        return "redirect:/clientes/painel?editado";
    }

    // GET confirmar exclusão
    @GetMapping("/excluir")
    public String confirmarExclusao(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        model.addAttribute("cliente", cliente);
        return "confirmar-exclusao-cliente";
    }

    // POST excluir (remove solicitações -> remove cliente)
    @PostMapping("/excluir")
    @Transactional
    public String excluirCliente(Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        if (cliente != null) {
            // apaga todas as solicitações vinculadas a este cliente
            solicitacaoRepository.deleteByCliente(cliente);
            // agora apaga o cliente
            clienteRepository.delete(cliente);
        }
        return "redirect:/logout";
    }
}
