package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public AdminController(ClienteRepository clienteRepository,
                           ProfissionalRepository profissionalRepository,
                           SolicitacaoRepository solicitacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

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
        profissionalRepository.findById(id).ifPresent(p -> {
            p.setStatus("ATIVO");
            profissionalRepository.save(p);
        });
        return "redirect:/admin/painel";
    }

    // Aprovar Cliente
    @PostMapping("/aprovar-cliente/{id}")
    public String aprovarCliente(@PathVariable Long id) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setStatus("ATIVO");
            clienteRepository.save(c);
        });
        return "redirect:/admin/painel";
    }

    // Recusar (deletar) Profissional — apaga solicitações antes
    @PostMapping("/recusar-profissional/{id}")
    @Transactional
    public String recusarProfissional(@PathVariable Long id) {
        return excluirProfissionalInterno(id);
    }

    // Recusar (deletar) Cliente — apaga solicitações antes
    @PostMapping("/recusar-cliente/{id}")
    @Transactional
    public String recusarCliente(@PathVariable Long id) {
        return excluirClienteInterno(id);
    }

    // Editar Profissional
    @GetMapping("/editar-profissional/{id}")
    public String editarProfissional(@PathVariable Long id, Model model) {
        return profissionalRepository.findById(id)
                .map(p -> {
                    model.addAttribute("profissional", p);
                    return "editar-profissional-admin";
                })
                .orElse("redirect:/admin/painel");
    }

    @PostMapping("/editar-profissional/{id}")
    public String salvarEdicaoProfissional(@PathVariable Long id, @ModelAttribute Profissional profissionalEditado) {
        profissionalRepository.findById(id).ifPresent(p -> {
            p.setNome(profissionalEditado.getNome());
            p.setEmail(profissionalEditado.getEmail());
            p.setTelefone(profissionalEditado.getTelefone());
            p.setProfissao(profissionalEditado.getProfissao());
            // não altera senha/status aqui
            profissionalRepository.save(p);
        });
        return "redirect:/admin/painel";
    }

    // Editar Cliente
    @GetMapping("/editar-cliente/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        return clienteRepository.findById(id)
                .map(c -> {
                    model.addAttribute("cliente", c);
                    return "editar-cliente-admin";
                })
                .orElse("redirect:/admin/painel");
    }

    @PostMapping("/editar-cliente/{id}")
    public String salvarEdicaoCliente(@PathVariable Long id, @ModelAttribute Cliente clienteEditado) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setNome(clienteEditado.getNome());
            c.setEmail(clienteEditado.getEmail());
            c.setTelefone(clienteEditado.getTelefone());
            // não altera senha/status aqui
            clienteRepository.save(c);
        });
        return "redirect:/admin/painel";
    }

    // Excluir Profissional definitivamente (com cascade manual)
    @PostMapping("/excluir-profissional/{id}")
    @Transactional
    public String excluirProfissional(@PathVariable Long id) {
        return excluirProfissionalInterno(id);
    }

    // Excluir Cliente definitivamente (com cascade manual)
    @PostMapping("/excluir-cliente/{id}")
    @Transactional
    public String excluirCliente(@PathVariable Long id) {
        return excluirClienteInterno(id);
    }

    // ---------- Helpers privados ----------

    private String excluirProfissionalInterno(Long id) {
        return profissionalRepository.findById(id).map(p -> {
            // apaga as solicitações vinculadas a este profissional
            solicitacaoRepository.deleteByProfissional(p);
            // apaga o profissional
            profissionalRepository.delete(p);
            return "redirect:/admin/painel?ok";
        }).orElse("redirect:/admin/painel");
    }

    private String excluirClienteInterno(Long id) {
        return clienteRepository.findById(id).map(c -> {
            // apaga as solicitações vinculadas a este cliente
            solicitacaoRepository.deleteByCliente(c);
            // apaga o cliente
            clienteRepository.delete(c);
            return "redirect:/admin/painel?ok";
        }).orElse("redirect:/admin/painel");
    }
}
