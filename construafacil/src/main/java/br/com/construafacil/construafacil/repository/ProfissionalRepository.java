package br.com.construafacil.repository;

import br.com.construafacil.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    Profissional findByEmail(String email);

    // Para buscar todos por status:
    List<Profissional> findByStatus(String status);
    // Ou se preferir (apenas o nome muda):
    // List<Profissional> findAllByStatus(String status);
}

