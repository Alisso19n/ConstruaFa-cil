package br.com.construafacil.service;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ProfissionalRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ProfissionalRepository profissionalRepository;

    public CustomAuthenticationSuccessHandler(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Se for profissional inativo, manda esperar aprovação
        String email = authentication.getName();
        Profissional p = profissionalRepository.findByEmail(email);
        if (p != null && !"ATIVO".equalsIgnoreCase(p.getStatus())) {
            response.sendRedirect("/login?aguardeAprovacao");
            return;
        }

        String targetUrl = "/login"; // fallback

        if (hasRole(authentication.getAuthorities(), "ROLE_ADMIN")) {
            targetUrl = "/admin/painel";
        } else if (hasRole(authentication.getAuthorities(), "ROLE_PROFISSIONAL")) {
            targetUrl = "/profissionais/painel";
        } else if (hasRole(authentication.getAuthorities(), "ROLE_CLIENTE")) {
            targetUrl = "/clientes/painel";
        }

        response.sendRedirect(targetUrl);
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> auths, String role) {
        return auths.stream().anyMatch(ga -> ga.getAuthority().equals(role));
    }
}
