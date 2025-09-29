package br.com.construafacil.controller;

import br.com.construafacil.model.Profissional;
import br.com.construafacil.repository.ProfissionalRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CatalogoController {

    private final ProfissionalRepository profissionalRepository;

    public CatalogoController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    // GET /catalogo?profissao=Pedreiro&q=marcio&page=0&size=12
    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String profissao,
            @RequestParam(required = false, name = "q") String termo,
            Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("nome").ascending());

        Page<Profissional> resultados;
        boolean temProfissao = profissao != null && !profissao.isBlank();
        boolean temTermo = termo != null && !termo.isBlank();

        if (temProfissao && temTermo) {
            resultados = profissionalRepository
                    .findByStatusAndProfissaoContainingIgnoreCaseAndNomeContainingIgnoreCase(
                            "ATIVO", profissao, termo, pageable);
        } else if (temProfissao) {
            resultados = profissionalRepository
                    .findByStatusAndProfissaoContainingIgnoreCase("ATIVO", profissao, pageable);
        } else if (temTermo) {
            resultados = profissionalRepository
                    .findByStatusAndNomeContainingIgnoreCase("ATIVO", termo, pageable);
        } else {
            resultados = profissionalRepository.findByStatus("ATIVO", pageable);
        }

        model.addAttribute("page", resultados);
        model.addAttribute("profissao", profissao);
        model.addAttribute("q", termo);

        return "catalogo-profissionais";
    }
}

