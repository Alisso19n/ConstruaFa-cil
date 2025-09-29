package br.com.construafacil.repository;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // Listagens paginadas (painéis)
    Page<Solicitacao> findByCliente(Cliente cliente, Pageable pageable);
    Page<Solicitacao> findByProfissional(Profissional profissional, Pageable pageable);

    // Exclusões em cascata manual usadas no Admin
    void deleteByCliente(Cliente cliente);
    void deleteByProfissional(Profissional profissional);
}


