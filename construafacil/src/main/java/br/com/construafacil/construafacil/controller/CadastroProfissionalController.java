package br.com.construafacil.controller;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ProfissionalRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profissionais")
public class CadastroProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroProfissionalController(ProfissionalRepository profissionalRepository,
                                          PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model) {
        model.addAttribute("profissional", new Profissional());
        return "cadastro-profissionais";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@ModelAttribute("profissional") Profissional p, Model model) {
        if (p.getEmail() == null || p.getEmail().isBlank()) {
            model.addAttribute("erro", "Informe um e-mail válido.");
            return "cadastro-profissionais";
        }
        if (profissionalRepository.findByEmail(p.getEmail()) != null) {
            model.addAttribute("erro", "Já existe um profissional com esse e-mail.");
            return "cadastro-profissionais";
        }
        // BCrypt
        if (p.getSenha() != null && !p.getSenha().isBlank()) {
            p.setSenha(passwordEncoder.encode(p.getSenha()));
        }
        p.setStatus("INATIVO"); // aguarda aprovação do admin
        profissionalRepository.save(p);
        return "redirect:/login?aguardeAprovacao";
    }
}
