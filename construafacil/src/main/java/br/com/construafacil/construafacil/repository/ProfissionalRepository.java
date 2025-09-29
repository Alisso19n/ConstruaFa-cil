package br.com.construafacil.repository;

import br.com.construafacil.model.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // Login/segurança
    Profissional findByEmail(String email);

    // Catálogo simples por profissão (lista não paginada, já existia)
    List<Profissional> findByProfissaoIgnoreCase(String profissao);

    // Busca por nome (paginada) — usado em solicitar serviço
    Page<Profissional> findByNomeContainingIgnoreCase(String q, Pageable pageable);

    // ==== Filtros usados no Catálogo (com paginação) ====

    // 1) status + profissão + nome (todos opcionais no controller, mas aqui a assinatura é completa)
    Page<Profissional> findByStatusAndProfissaoContainingIgnoreCaseAndNomeContainingIgnoreCase(
            String status,
            String profissao,
            String nome,
            Pageable pageable
    );

    // 2) status + profissão (quando não há filtro de nome)
    Page<Profissional> findByStatusAndProfissaoContainingIgnoreCase(
            String status,
            String profissao,
            Pageable pageable
    );

    // 3) somente status + nome (se o controller usar esse atalho)
    Page<Profissional> findByStatusAndNomeContainingIgnoreCase(
            String status,
            String nome,
            Pageable pageable
    );

    // 4) somente status (fallback quando não há filtros de texto)
    Page<Profissional> findByStatus(
            String status,
            Pageable pageable
    );
}
