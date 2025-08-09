package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public SolicitacaoController(
            SolicitacaoRepository solicitacaoRepository,
            ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    // Formulário de nova solicitação (exibido para o cliente)
    @GetMapping("/nova")
    public String novaSolicitacao(Model model) {
        model.addAttribute("solicitacao", new Solicitacao());
        model.addAttribute("profissionais", profissionalRepository.findByStatus("ATIVO"));
        return "solicitacao"; // nome do template: solicitacao.html
    }

    // Salvar nova solicitação
    @PostMapping("/nova")
    public String salvarSolicitacao(@ModelAttribute Solicitacao solicitacao,
                                    Principal principal, Model model) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        solicitacao.setCliente(cliente);

        // Pega o profissional pelo id selecionado no form
        Profissional profissional = null;
        if (solicitacao.getProfissional() != null && solicitacao.getProfissional().getId() != null) {
            profissional = profissionalRepository.findById(solicitacao.getProfissional().getId()).orElse(null);
        }
        if (profissional == null || !"ATIVO".equalsIgnoreCase(profissional.getStatus())) {
            model.addAttribute("erro", "Selecione um profissional válido.");
            model.addAttribute("profissionais", profissionalRepository.findByStatus("ATIVO"));
            return "solicitacao";
        }

        solicitacao.setProfissional(profissional);
        solicitacao.setDataSolicitacao(LocalDateTime.now());
        solicitacao.setStatus("PENDENTE");

        solicitacaoRepository.save(solicitacao);
        return "redirect:/clientes/painel?sucesso";
    }

    // --- EXTRAS: Atualização do status da solicitação pelo profissional ---
    @PostMapping("/atualizar-status/{id}")
    public String atualizarStatus(@PathVariable Long id,
                                  @RequestParam String status,
                                  Principal principal) {
        // Confirma se o profissional está logado
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        Solicitacao solicitacao = solicitacaoRepository.findById(id).orElse(null);

        if (solicitacao != null && solicitacao.getProfissional().getId().equals(profissional.getId())) {
            // Só deixa alterar se estiver pendente!
            if ("PENDENTE".equalsIgnoreCase(solicitacao.getStatus())) {
                if ("ACEITA".equalsIgnoreCase(status)) {
                    solicitacao.setStatus("ACEITA");
                } else if ("RECUSADA".equalsIgnoreCase(status)) {
                    solicitacao.setStatus("RECUSADA");
                }
                solicitacaoRepository.save(solicitacao);
            }
        }
        return "redirect:/profissionais/painel";
    }

    // (Opcional: listagem das solicitações do cliente, se quiser usar em painel do cliente)
    @GetMapping("/cliente")
    public String solicitacoesCliente(Model model, Principal principal) {
        Cliente cliente = clienteRepository.findByEmail(principal.getName());
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByCliente(cliente);
        model.addAttribute("solicitacoes", solicitacoes);
        return "painel-cliente"; // só se você quiser um painel para cliente
    }
}
