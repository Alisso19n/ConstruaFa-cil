package br.com.construafacil.controller;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ClienteRepository;
import br.com.construafacil.repository.ProfissionalRepository;
import br.com.construafacil.repository.SolicitacaoRepository;
import org.springframework.data.domain.*;
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

    // Painel ADMIN com três paginações independentes
    @GetMapping("/painel")
    public String painelAdmin(@RequestParam(defaultValue = "0") int pageC,
                              @RequestParam(defaultValue = "8") int sizeC,
                              @RequestParam(defaultValue = "0") int pageP,
                              @RequestParam(defaultValue = "8") int sizeP,
                              @RequestParam(defaultValue = "0") int pageS,
                              @RequestParam(defaultValue = "8") int sizeS,
                              Model model) {

        // Clientes: ordena por nome
        Pageable pageableC = PageRequest.of(Math.max(pageC,0), Math.max(sizeC,1),
                Sort.by(Sort.Direction.ASC, "nome"));
        Page<Cliente> clientesPage = clienteRepository.findAll(pageableC);

        // Profissionais: ordena por nome
        Pageable pageableP = PageRequest.of(Math.max(pageP,0), Math.max(sizeP,1),
                Sort.by(Sort.Direction.ASC, "nome"));
        Page<Profissional> profissionaisPage = profissionalRepository.findAll(pageableP);

        // Solicitações: ordena por data desc
        Pageable pageableS = PageRequest.of(Math.max(pageS,0), Math.max(sizeS,1),
                Sort.by(Sort.Direction.DESC, "dataSolicitacao"));
        var solicitacoesPage = solicitacaoRepository.findAll(pageableS);

        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("profissionaisPage", profissionaisPage);
        model.addAttribute("solicitacoesPage", solicitacoesPage);

        model.addAttribute("sizeC", sizeC);
        model.addAttribute("sizeP", sizeP);
        model.addAttribute("sizeS", sizeS);

        return "painel-admin";
    }

    // Aprovar Profissional
    @PostMapping("/aprovar-profissional/{id}")
    public String aprovarProfissional(@PathVariable Long id,
                                      @RequestParam(defaultValue="0") int pageP,
                                      @RequestParam(defaultValue="8") int sizeP) {
        profissionalRepository.findById(id).ifPresent(p -> {
            p.setStatus("ATIVO");
            profissionalRepository.save(p);
        });
        return "redirect:/admin/painel?pageP="+pageP+"&sizeP="+sizeP+"#profissionais";
    }

    // Aprovar Cliente
    @PostMapping("/aprovar-cliente/{id}")
    public String aprovarCliente(@PathVariable Long id,
                                 @RequestParam(defaultValue="0") int pageC,
                                 @RequestParam(defaultValue="8") int sizeC) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setStatus("ATIVO");
            clienteRepository.save(c);
        });
        return "redirect:/admin/painel?pageC="+pageC+"&sizeC="+sizeC+"#clientes";
    }

    // Recusar (deletar) Profissional — apaga solicitações antes
    @PostMapping("/recusar-profissional/{id}")
    @Transactional
    public String recusarProfissional(@PathVariable Long id,
                                      @RequestParam(defaultValue="0") int pageP,
                                      @RequestParam(defaultValue="8") int sizeP) {
        return excluirProfissionalInterno(id, pageP, sizeP);
    }

    // Recusar (deletar) Cliente — apaga solicitações antes
    @PostMapping("/recusar-cliente/{id}")
    @Transactional
    public String recusarCliente(@PathVariable Long id,
                                 @RequestParam(defaultValue="0") int pageC,
                                 @RequestParam(defaultValue="8") int sizeC) {
        return excluirClienteInterno(id, pageC, sizeC);
    }

    // Editar Profissional
    @GetMapping("/editar-profissional/{id}")
    public String editarProfissional(@PathVariable Long id, Model model) {
        return profissionalRepository.findById(id)
                .map(p -> {
                    model.addAttribute("profissional", p);
                    return "editar-profissional-admin";
                })
                .orElse("redirect:/admin/painel#profissionais");
    }

    @PostMapping("/editar-profissional/{id}")
    public String salvarEdicaoProfissional(@PathVariable Long id,
                                           @ModelAttribute Profissional profissionalEditado,
                                           @RequestParam(defaultValue="0") int pageP,
                                           @RequestParam(defaultValue="8") int sizeP) {
        profissionalRepository.findById(id).ifPresent(p -> {
            p.setNome(profissionalEditado.getNome());
            p.setEmail(profissionalEditado.getEmail());
            p.setTelefone(profissionalEditado.getTelefone());
            p.setProfissao(profissionalEditado.getProfissao());
            // não altera senha/status aqui
            profissionalRepository.save(p);
        });
        return "redirect:/admin/painel?pageP="+pageP+"&sizeP="+sizeP+"#profissionais";
    }

    // Editar Cliente
    @GetMapping("/editar-cliente/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        return clienteRepository.findById(id)
                .map(c -> {
                    model.addAttribute("cliente", c);
                    return "editar-cliente-admin";
                })
                .orElse("redirect:/admin/painel#clientes");
    }

    @PostMapping("/editar-cliente/{id}")
    public String salvarEdicaoCliente(@PathVariable Long id,
                                      @ModelAttribute Cliente clienteEditado,
                                      @RequestParam(defaultValue="0") int pageC,
                                      @RequestParam(defaultValue="8") int sizeC) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setNome(clienteEditado.getNome());
            c.setEmail(clienteEditado.getEmail());
            c.setTelefone(clienteEditado.getTelefone());
            // não altera senha/status aqui
            clienteRepository.save(c);
        });
        return "redirect:/admin/painel?pageC="+pageC+"&sizeC="+sizeC+"#clientes";
    }

    // Excluir Profissional definitivamente (com cascade manual)
    @PostMapping("/excluir-profissional/{id}")
    @Transactional
    public String excluirProfissional(@PathVariable Long id,
                                      @RequestParam(defaultValue="0") int pageP,
                                      @RequestParam(defaultValue="8") int sizeP) {
        return excluirProfissionalInterno(id, pageP, sizeP);
    }

    // Excluir Cliente definitivamente (com cascade manual)
    @PostMapping("/excluir-cliente/{id}")
    @Transactional
    public String excluirCliente(@PathVariable Long id,
                                 @RequestParam(defaultValue="0") int pageC,
                                 @RequestParam(defaultValue="8") int sizeC) {
        return excluirClienteInterno(id, pageC, sizeC);
    }

    // ---------- Helpers privados ----------

    private String excluirProfissionalInterno(Long id, int pageP, int sizeP) {
        return profissionalRepository.findById(id).map(p -> {
            // apaga as solicitações vinculadas a este profissional
            solicitacaoRepository.deleteByProfissional(p);
            // apaga o profissional
            profissionalRepository.delete(p);
            return "redirect:/admin/painel?pageP="+pageP+"&sizeP="+sizeP+"#profissionais";
        }).orElse("redirect:/admin/painel#profissionais");
    }

    private String excluirClienteInterno(Long id, int pageC, int sizeC) {
        return clienteRepository.findById(id).map(c -> {
            // apaga as solicitações vinculadas a este cliente
            solicitacaoRepository.deleteByCliente(c);
            // apaga o cliente
            clienteRepository.delete(c);
            return "redirect:/admin/painel?pageC="+pageC+"&sizeC="+sizeC+"#clientes";
        }).orElse("redirect:/admin/painel#clientes");
    }
}
