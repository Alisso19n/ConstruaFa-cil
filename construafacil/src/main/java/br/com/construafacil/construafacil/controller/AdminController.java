package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    // Painel principal do admin
    @GetMapping("/painel")
    public String painelAdmin(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("profissionais", profissionalRepository.findAll());
        model.addAttribute("solicitacoes", solicitacaoRepository.findAll());
        return "painel-admin";
    }

    // Aprovar Profissional
    @PostMapping("/aprovar-profissional/{id}")
    public String aprovarProfissional(@PathVariable Long id) {
        Profissional profissional = profissionalRepository.findById(id).orElse(null);
        if (profissional != null) {
            profissional.setStatus("ATIVO");
            profissionalRepository.save(profissional);
        }
        return "redirect:/admin/painel";
    }

    // Aprovar Cliente
    @PostMapping("/aprovar-cliente/{id}")
    public String aprovarCliente(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            cliente.setStatus("ATIVO");
            clienteRepository.save(cliente);
        }
        return "redirect:/admin/painel";
    }

    // Recusar Profissional
    @PostMapping("/recusar-profissional/{id}")
    public String recusarProfissional(@PathVariable Long id) {
        profissionalRepository.deleteById(id);
        return "redirect:/admin/painel";
    }

    // Recusar Cliente
    @PostMapping("/recusar-cliente/{id}")
    public String recusarCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/admin/painel";
    }

    // Editar Profissional
    @GetMapping("/editar-profissional/{id}")
    public String editarProfissional(@PathVariable Long id, Model model) {
        Profissional profissional = profissionalRepository.findById(id).orElse(null);
        if (profissional == null) return "redirect:/admin/painel";
        model.addAttribute("profissional", profissional);
        return "editar-profissional-admin";
    }

    @PostMapping("/editar-profissional/{id}")
    public String salvarEdicaoProfissional(@PathVariable Long id, @ModelAttribute Profissional profissionalEditado) {
        Profissional profissional = profissionalRepository.findById(id).orElse(null);
        if (profissional != null) {
            profissional.setNome(profissionalEditado.getNome());
            profissional.setEmail(profissionalEditado.getEmail());
            profissional.setTelefone(profissionalEditado.getTelefone());
            profissional.setProfissao(profissionalEditado.getProfissao());
            // Não mexe em senha nem status aqui!
            profissionalRepository.save(profissional);
        }
        return "redirect:/admin/painel";
    }

    // Editar Cliente
    @GetMapping("/editar-cliente/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) return "redirect:/admin/painel";
        model.addAttribute("cliente", cliente);
        return "editar-cliente-admin";
    }

    @PostMapping("/editar-cliente/{id}")
    public String salvarEdicaoCliente(@PathVariable Long id, @ModelAttribute Cliente clienteEditado) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            cliente.setNome(clienteEditado.getNome());
            cliente.setEmail(clienteEditado.getEmail());
            cliente.setTelefone(clienteEditado.getTelefone());
            // Não mexe em senha nem status aqui!
            clienteRepository.save(cliente);
        }
        return "redirect:/admin/painel";
    }

    // Excluir Profissional definitivamente
    @PostMapping("/excluir-profissional/{id}")
    public String excluirProfissional(@PathVariable Long id) {
        profissionalRepository.deleteById(id);
        return "redirect:/admin/painel";
    }

    // Excluir Cliente definitivamente
    @PostMapping("/excluir-cliente/{id}")
    public String excluirCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/admin/painel";
    }
}
