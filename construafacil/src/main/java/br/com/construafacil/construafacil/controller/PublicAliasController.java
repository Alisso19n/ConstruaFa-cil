package br.com.construafacil.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PublicAliasController {

    // CLIENTE - atalhos comuns usados na home
    @GetMapping({"/sou-cliente", "/quero-ser-cliente", "/cadastro-cliente", "/cliente/novo", "/cliente/registrar"})
    public String aliasClienteCadastro() {
        return "redirect:/clientes/cadastro";
    }

    // PROFISSIONAL - atalhos comuns usados na home
    @GetMapping({"/sou-profissional", "/quero-ser-profissional", "/cadastro-profissional", "/cadastro-profissionais", "/profissional/novo", "/profissional/registrar"})
    public String aliasProfissionalCadastro() {
        return "redirect:/profissionais/cadastro";
    }

    // Fallback genérico
    @GetMapping({"/cadastro"})
    public String aliasCadastroGenerico() {
        return "redirect:/clientes/cadastro";
    }

    // Painéis legados (singular) -> plural
    @GetMapping("/cliente/painel")
    public String legacyClientePainel() {
        return "redirect:/clientes/painel";
    }

    @GetMapping("/profissional/painel")
    public String legacyProfPainel() {
        return "redirect:/profissionais/painel";
    }

    // Solicitar serviço (atalho usado pelo index ou catálogo antigo)
    @GetMapping({"/solicitar-servico", "/solicitar", "/nova-solicitacao"})
    public String aliasSolicitacao(Authentication auth, HttpServletRequest request) {
        String query = request.getQueryString(); // preserva ?profissionalId=...
        String suffix = (query != null && !query.isBlank()) ? ("?" + query) : "";
        if (auth == null) {
            return "redirect:/login";
        }
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
        boolean isCliente = hasRole(auth, "ROLE_CLIENTE");
        boolean isProf = hasRole(auth, "ROLE_PROFISSIONAL");

        if (isAdmin) {
            // admin não solicita serviço; manda para painel admin
            return "redirect:/admin/painel";
        } else if (isCliente) {
            return "redirect:/clientes/solicitar" + suffix;
        } else if (isProf) {
            return "redirect:/profissionais/solicitar" + suffix;
        }
        // fallback
        return "redirect:/login";
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (role.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
