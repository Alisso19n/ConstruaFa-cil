package br.com.construafacil.controller;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfissionalController(ProfissionalRepository profissionalRepository,
                                  SolicitacaoRepository solicitacaoRepository,
                                  PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET do cadastro (exibe formulário)
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("profissional", new Profissional());
        return "cadastro-profissionais";
    }

    // POST do cadastro (processa formulário)
    @PostMapping("/cadastro")
    public String cadastrarProfissional(@ModelAttribute("profissional") Profissional profissional, Model model) {
        if (profissional.getSenha() == null || profissional.getSenha().isBlank()) {
            model.addAttribute("erro", "Informe uma senha válida.");
            return "cadastro-profissionais";
        }
        if (profissional.getEmail() == null || profissional.getEmail().isBlank()) {
            model.addAttribute("erro", "Informe um e-mail válido.");
            return "cadastro-profissionais";
        }
        if (profissionalRepository.findByEmail(profissional.getEmail()) != null) {
            model.addAttribute("erro", "Já existe um profissional com esse e-mail.");
            return "cadastro-profissionais";
        }

        profissional.setStatus("INATIVO"); // aguarda aprovação do admin
        profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));
        profissionalRepository.save(profissional);

        return "redirect:/login?aguardeAprovacao";
    }

    // Painel do profissional autenticado
    @GetMapping("/painel")
    public String painelProfissional(Model model, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByProfissional(profissional);
        model.addAttribute("profissional", profissional);
        model.addAttribute("solicitacoes", solicitacoes);
        return "painel-profissional";
    }

    // GET editar
    @GetMapping("/editar")
    public String editarProfissional(Model model, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        model.addAttribute("profissional", profissional);
        return "editar-profissional";
    }

    // POST editar (não altera a senha aqui)
    @PostMapping("/editar")
    public String salvarEdicaoProfissional(@ModelAttribute("profissional") Profissional profissionalEditado,
                                           Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        profissional.setNome(profissionalEditado.getNome());
        profissional.setTelefone(profissionalEditado.getTelefone());
        profissional.setProfissao(profissionalEditado.getProfissao());
        profissionalRepository.save(profissional);
        return "redirect:/profissionais/painel?editado";
    }

    // GET confirmar exclusão
    @GetMapping("/excluir")
    public String confirmarExclusao(Model model, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        model.addAttribute("profissional", profissional);
        return "confirmar-exclusao-profissional";
    }

    // POST excluir (remove solicitações -> remove profissional)
    @PostMapping("/excluir")
    @Transactional
    public String excluirProfissional(Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        if (profissional != null) {
            // apaga todas as solicitações vinculadas a este profissional
            solicitacaoRepository.deleteByProfissional(profissional);
            // agora apaga o profissional
            profissionalRepository.delete(profissional);
        }
        return "redirect:/logout";
    }
}
