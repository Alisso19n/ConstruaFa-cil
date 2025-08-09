package br.com.construafacil.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/painel");
                return;
            } else if (authority.getAuthority().equals("ROLE_CLIENTE")) {
                response.sendRedirect("/clientes/painel");
                return;
            } else if (authority.getAuthority().equals("ROLE_PROFISSIONAL")) {
                response.sendRedirect("/profissionais/painel");
                return;
            }
        }
        response.sendRedirect("/login");
    }
}
