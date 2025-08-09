package br.com.construafacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String root() {
        // Redireciona para tela de login
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        // Pode customizar para mostrar mensagem, etc
        return "redirect:/login?logout";
    }
}
