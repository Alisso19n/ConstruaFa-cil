package br.com.construafacil.repository;

import br.com.construafacil.model.Cliente;
import br.com.construafacil.model.Profissional;
import br.com.construafacil.model.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // Listagens
    List<Solicitacao> findByCliente(Cliente cliente);
    List<Solicitacao> findByProfissional(Profissional profissional);

    // Exclusões derivadas (Spring Data gera DELETE … WHERE cliente = ? / profissional = ?)
    void deleteByCliente(Cliente cliente);
    void deleteByProfissional(Profissional profissional);
}
