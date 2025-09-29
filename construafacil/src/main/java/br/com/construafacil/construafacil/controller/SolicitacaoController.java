package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public SolicitacaoController(
            SolicitacaoRepository solicitacaoRepository,
            ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Formulário para nova solicitação (cliente OU profissional) com paginação de profissionais
    @GetMapping("/nova")
    public String novaSolicitacao(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Profissional> profissionaisPage = profissionalRepository.findByStatus("ATIVO", pageable);

        model.addAttribute("solicitacao", new Solicitacao());
        model.addAttribute("profissionaisPage", profissionaisPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "solicitacao";
    }

    // Salvar solicitação enviada
    @PostMapping("/nova")
    public String salvarSolicitacao(@ModelAttribute Solicitacao solicitacao,
                                    Principal principal,
                                    Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "12") int size) {

        if (principal == null) {
            // Usuário não autenticado (apenas por segurança)
            return "redirect:/login?error";
        }

        // 1) Quem está logado?
        String email = principal.getName();

        // 2) Primeiro tenta como Cliente
        Cliente cliente = clienteRepository.findByEmail(email);

        // 3) Se não for Cliente, tenta como Profissional e cria/usa "cliente espelho"
        if (cliente == null) {
            Profissional p = profissionalRepository.findByEmail(email);
            if (p != null) {
                cliente = ensureClienteEspelho(p);
            }
        }

        if (cliente == null) {
            model.addAttribute("erro", "Não foi possível identificar o solicitante.");
            // recarrega paginação para reexibir o formulário
            Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
            Page<Profissional> profissionaisPage = profissionalRepository.findByStatus("ATIVO", pageable);
            model.addAttribute("profissionaisPage", profissionaisPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "solicitacao";
        }

        // Profissional selecionado no formulário
        Profissional profissional = null;
        if (solicitacao.getProfissional() != null && solicitacao.getProfissional().getId() != null) {
            profissional = profissionalRepository.findById(solicitacao.getProfissional().getId()).orElse(null);
        }

        if (profissional == null || !"ATIVO".equalsIgnoreCase(profissional.getStatus())) {
            model.addAttribute("erro", "Selecione um profissional válido.");
            // recarrega paginação para reexibir o formulário
            Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
            Page<Profissional> profissionaisPage = profissionalRepository.findByStatus("ATIVO", pageable);
            model.addAttribute("profissionaisPage", profissionaisPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "solicitacao";
        }

        solicitacao.setCliente(cliente);
        solicitacao.setProfissional(profissional);
        solicitacao.setDataSolicitacao(LocalDateTime.now());
        solicitacao.setStatus("PENDENTE");

        solicitacaoRepository.save(solicitacao);

        // Redireciona de acordo com o papel "provável" do usuário
        if (clienteRepository.findByEmail(email) != null) {
            return "redirect:/clientes/painel?sucesso";
        } else {
            return "redirect:/profissionais/painel?sucesso";
        }
    }

    /**
     * Garante que exista um "Cliente espelho" para um Profissional.
     * Se já existir um cliente com esse e-mail, retorna ele.
     * Caso contrário, cria um novo Cliente ATIVO com os dados do Profissional.
     */
    private Cliente ensureClienteEspelho(Profissional p) {
        Cliente existente = clienteRepository.findByEmail(p.getEmail());
        if (existente != null) {
            return existente;
        }
        Cliente novo = new Cliente();
        novo.setNome(p.getNome());
        novo.setEmail(p.getEmail());
        // senha técnica aleatória (não usada para login), armazenada com hash
        String senhaAleatoria = "PX-" + UUID.randomUUID();
        novo.setSenha(passwordEncoder.encode(senhaAleatoria));
        novo.setTelefone(p.getTelefone());
        novo.setStatus("ATIVO");
        return clienteRepository.save(novo);
    }
}
