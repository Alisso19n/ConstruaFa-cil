package br.com.construafacil.controller;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
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

    public ProfissionalController(
            ProfissionalRepository profissionalRepository,
            SolicitacaoRepository solicitacaoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.profissionalRepository = profissionalRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Formulário de cadastro de profissional
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("profissional", new Profissional());
        return "cadastro-profissionais";
    }

    // Salva novo profissional
    @PostMapping("/cadastro")
    public String cadastrarProfissional(@ModelAttribute Profissional profissional, Model model) {
        profissional.setStatus("INATIVO"); // Aguarda aprovação do admin
        profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));
        profissionalRepository.save(profissional);

        model.addAttribute("mensagem", "Cadastro realizado! Aguarde aprovação do administrador.");
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

    // Formulário de edição de cadastro
    @GetMapping("/editar")
    public String editarProfissional(Model model, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        model.addAttribute("profissional", profissional);
        return "editar-profissional";
    }

    // Processa a edição
    @PostMapping("/editar")
    public String salvarEdicaoProfissional(@ModelAttribute Profissional profissionalEditado, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        profissional.setNome(profissionalEditado.getNome());
        profissional.setTelefone(profissionalEditado.getTelefone());
        profissional.setProfissao(profissionalEditado.getProfissao());
        // Se quiser permitir alteração de e-mail, precisa de validações extras!
        profissionalRepository.save(profissional);
        return "redirect:/profissionais/painel?editado";
    }

    // Confirmação de exclusão
    @GetMapping("/excluir")
    public String confirmarExclusao(Model model, Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        model.addAttribute("profissional", profissional);
        return "confirmar-exclusao-profissional";
    }

    // Exclusão definitiva com remoção das solicitações associadas
    @PostMapping("/excluir")
    public String excluirProfissional(Principal principal) {
        Profissional profissional = profissionalRepository.findByEmail(principal.getName());
        if (profissional != null) {
            // Exclui todas as solicitações do profissional antes
            List<Solicitacao> solicitacoes = solicitacaoRepository.findByProfissional(profissional);
            solicitacaoRepository.deleteAll(solicitacoes);
            // Agora deleta o profissional
            profissionalRepository.delete(profissional);
        }
        return "redirect:/logout";
    }
}
