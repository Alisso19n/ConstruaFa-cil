package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public ClienteController(ClienteRepository clienteRepository,
                             ProfissionalRepository profissionalRepository,
                             SolicitacaoRepository solicitacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    // Painel paginado
    @GetMapping("/painel")
    public String painel(@RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="5") int size,
                         Model model) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";

        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1),
                Sort.by(Sort.Direction.DESC, "dataSolicitacao"));

        Page<Solicitacao> solicitacoes = solicitacaoRepository.findByCliente(c, pageable);

        model.addAttribute("cliente", c);
        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("size", size);
        return "painel-cliente";
    }

    /**
     * Form de solicitação
     * - Se vier ?profissionalId= -> mostra o resumo do profissional + formulário de envio.
     * - Caso contrário -> mostra BUSCA + LISTA paginada de profissionais para escolher.
     */
    @GetMapping("/solicitar")
    public String formSolicitar(@RequestParam(name="profissionalId", required=false) Long profissionalId,
                                @RequestParam(name="q", required=false) String q,
                                @RequestParam(defaultValue="0") int page,
                                @RequestParam(defaultValue="5") int size,
                                Model model) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";

        model.addAttribute("usuario", c);
        model.addAttribute("postAction", "/clientes/solicitar");

        // Se veio um profissional escolhido, mostra o "resumo" e o formulário
        if (profissionalId != null) {
            Profissional p = profissionalRepository.findById(profissionalId).orElse(null);
            model.addAttribute("profissional", p);
            return "solicitacao";
        }

        // Caso contrário, BUSCA + LISTA (paginada) para escolher
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by("nome").ascending());
        Page<Profissional> lista = (q == null || q.isBlank())
                ? profissionalRepository.findAll(pageable)
                : profissionalRepository.findByNomeContainingIgnoreCase(q.trim(), pageable);

        model.addAttribute("listaProfissionaisPage", lista);
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("profissional", null); // indica modo "escolha"
        return "solicitacao";
    }

    // POST de solicitação
    @PostMapping("/solicitar")
    @Transactional
    public String salvarSolicitacao(@RequestParam(name="profissionalId", required=false) Long profissionalId,
                                    @RequestParam("descricao") String descricao,
                                    Model model) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";

        if (profissionalId == null) {
            model.addAttribute("usuario", c);
            model.addAttribute("profissional", null);
            model.addAttribute("postAction", "/clientes/solicitar");
            model.addAttribute("erro", "Selecione um profissional na lista para enviar a solicitação.");
            return "solicitacao";
        }

        Profissional p = profissionalRepository.findById(profissionalId).orElse(null);
        if (p == null) {
            model.addAttribute("usuario", c);
            model.addAttribute("profissional", null);
            model.addAttribute("postAction", "/clientes/solicitar");
            model.addAttribute("erro", "Profissional não encontrado.");
            return "solicitacao";
        }

        Solicitacao s = new Solicitacao();
        s.setCliente(c);
        s.setProfissional(p);
        s.setDescricao(descricao);
        s.setDataSolicitacao(LocalDateTime.now());
        s.setStatus("PENDENTE");
        solicitacaoRepository.save(s);

        return "redirect:/clientes/painel?ok=solicitado";
    }

    @PostMapping("/solicitacoes/{id}/cancelar")
    @Transactional
    public String cancelar(@PathVariable Long id,
                           @RequestParam(defaultValue="0") int page,
                           @RequestParam(defaultValue="5") int size) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";
        Optional<Solicitacao> opt = solicitacaoRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/clientes/painel?page="+page+"&size="+size;

        Solicitacao s = opt.get();
        if (s.getCliente() == null || !s.getCliente().getId().equals(c.getId()))
            return "redirect:/clientes/painel?page="+page+"&size="+size;

        if (s.getStatus() == null || s.getStatus().equalsIgnoreCase("PENDENTE")) {
            s.setStatus("CANCELADA");
            solicitacaoRepository.save(s);
        }
        return "redirect:/clientes/painel?page="+page+"&size="+size+"&ok=cancelada";
    }

    @GetMapping("/editar")
    public String formEditar(Model model) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";
        model.addAttribute("cliente", c);
        return "editar-cliente";
    }

    @PostMapping("/editar")
    @Transactional
    public String salvarEdicao(@ModelAttribute("cliente") Cliente dto) {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";
        c.setNome(dto.getNome());
        c.setEmail(dto.getEmail());
        c.setTelefone(dto.getTelefone());
        clienteRepository.save(c);
        return "redirect:/clientes/painel?ok=editado";
    }

    @PostMapping("/excluir")
    @Transactional
    public String excluirConta() {
        Cliente c = getLogado();
        if (c == null) return "redirect:/login";
        solicitacaoRepository.deleteByCliente(c);
        clienteRepository.delete(c);
        return "redirect:/logout";
    }

    // ---------- helper ----------
    private Cliente getLogado() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return clienteRepository.findByEmail(auth.getName());
    }
}
