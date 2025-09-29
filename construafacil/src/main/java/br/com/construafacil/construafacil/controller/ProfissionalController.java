

package br.com.construafacil.controller;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

// IMPORT CORRETO DO SPRING SECURITY
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public ProfissionalController(ProfissionalRepository profissionalRepository,
                                  SolicitacaoRepository solicitacaoRepository) {
        this.profissionalRepository = profissionalRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @GetMapping("/painel")
    public String painel(@RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="5") int size,
                         Model model) {
        Profissional p = getLogado();
        if (p == null) return "redirect:/login";
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1),
                Sort.by(Sort.Direction.DESC, "dataSolicitacao"));
        Page<Solicitacao> solicitacoes = solicitacaoRepository.findByProfissional(p, pageable);
        model.addAttribute("profissional", p);
        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("size", size);
        return "painel-profissional";
    }

    @PostMapping("/solicitacoes/{id}/aceitar")
    @Transactional
    public String aceitar(@PathVariable Long id,
                          @RequestParam(defaultValue="0") int page,
                          @RequestParam(defaultValue="5") int size) {
        Profissional prof = getLogado();
        if (prof == null) return "redirect:/login";
        Optional<Solicitacao> opt = solicitacaoRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/profissionais/painel?page="+page+"&size="+size;
        Solicitacao s = opt.get();
        if (s.getProfissional() == null || !s.getProfissional().getId().equals(prof.getId()))
            return "redirect:/profissionais/painel?page="+page+"&size="+size;
        if (s.getStatus() == null || s.getStatus().equalsIgnoreCase("PENDENTE")) {
            s.setStatus("ACEITA");
            solicitacaoRepository.save(s);
        }
        return "redirect:/profissionais/painel?page="+page+"&size="+size+"&ok=aceita";
    }

    @PostMapping("/solicitacoes/{id}/recusar")
    @Transactional
    public String recusar(@PathVariable Long id,
                          @RequestParam(defaultValue="0") int page,
                          @RequestParam(defaultValue="5") int size) {
        Profissional prof = getLogado();
        if (prof == null) return "redirect:/login";
        Optional<Solicitacao> opt = solicitacaoRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/profissionais/painel?page="+page+"&size="+size;
        Solicitacao s = opt.get();
        if (s.getProfissional() == null || !s.getProfissional().getId().equals(prof.getId()))
            return "redirect:/profissionais/painel?page="+page+"&size="+size;
        if (s.getStatus() == null || s.getStatus().equalsIgnoreCase("PENDENTE")) {
            s.setStatus("RECUSADA");
            solicitacaoRepository.save(s);
        }
        return "redirect:/profissionais/painel?page="+page+"&size="+size+"&ok=recusada";
    }

    @GetMapping("/solicitar")
    public String formSolicitar(@RequestParam(name="profissionalId", required=false) Long profissionalId,
                                @RequestParam(name="q", required=false) String q,
                                @RequestParam(defaultValue="0") int page,
                                @RequestParam(defaultValue="5") int size,
                                Model model) {
        Profissional solicitante = getLogado();
        if (solicitante == null) return "redirect:/login";

        model.addAttribute("usuario", solicitante);
        model.addAttribute("postAction", "/profissionais/solicitar");

        if (profissionalId != null) {
            Profissional p = profissionalRepository.findById(profissionalId).orElse(null);
            model.addAttribute("profissional", p);
            return "solicitacao";
        }

        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by("nome").ascending());
        Page<Profissional> lista = (q == null || q.isBlank())
                ? profissionalRepository.findAll(pageable)
                : profissionalRepository.findByNomeContainingIgnoreCase(q.trim(), pageable);

        model.addAttribute("listaProfissionaisPage", lista);
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("profissional", null);
        return "solicitacao";
    }

    @PostMapping("/solicitar")
    @Transactional
    public String salvarSolicitacao(@RequestParam(name="profissionalId", required=false) Long profissionalId,
                                    @RequestParam("descricao") String descricao,
                                    Model model) {
        Profissional solicitante = getLogado();
        if (solicitante == null) return "redirect:/login";

        if (profissionalId == null) {
            model.addAttribute("usuario", solicitante);
            model.addAttribute("profissional", null);
            model.addAttribute("postAction", "/profissionais/solicitar");
            model.addAttribute("erro", "Selecione um profissional na lista para enviar a solicitação.");
            return "solicitacao";
        }

        Profissional destino = profissionalRepository.findById(profissionalId).orElse(null);
        if (destino == null) {
            model.addAttribute("usuario", solicitante);
            model.addAttribute("profissional", null);
            model.addAttribute("postAction", "/profissionais/solicitar");
            model.addAttribute("erro", "Profissional não encontrado.");
            return "solicitacao";
        }

        Solicitacao s = new Solicitacao();
        s.setCliente(null); // se no futuro vincular cliente, ajustar aqui
        s.setProfissional(destino);
        s.setDescricao(descricao + " (solicitado por profissional: " + solicitante.getNome() + ")");
        s.setDataSolicitacao(LocalDateTime.now());
        s.setStatus("PENDENTE");
        solicitacaoRepository.save(s);

        return "redirect:/profissionais/painel?ok=solicitado";
    }

    @GetMapping("/editar")
    public String formEditar(Model model) {
        Profissional p = getLogado();
        if (p == null) return "redirect:/login";
        model.addAttribute("profissional", p);
        return "editar-profissional";
    }

    @PostMapping("/editar")
    @Transactional
    public String salvarEdicao(@ModelAttribute("profissional") Profissional dto) {
        Profissional p = getLogado();
        if (p == null) return "redirect:/login";
        p.setNome(dto.getNome());
        p.setEmail(dto.getEmail());
        p.setTelefone(dto.getTelefone());
        p.setProfissao(dto.getProfissao());
        profissionalRepository.save(p);
        return "redirect:/profissionais/painel?ok=editado";
    }

    @PostMapping("/excluir")
    @Transactional
    public String excluirConta() {
        Profissional p = getLogado();
        if (p == null) return "redirect:/login";
        solicitacaoRepository.deleteByProfissional(p);
        profissionalRepository.delete(p);
        return "redirect:/logout";
    }

    private Profissional getLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return profissionalRepository.findByEmail(auth.getName());
    }
}
