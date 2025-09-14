package br.com.construafacil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Página de login do Spring Security
    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }
}
