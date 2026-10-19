package com.JBVision.repository;
 
import com.JBVision.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
 
    Optional<Pessoa> findByIdentificador(String identificador);
 
    List<Pessoa> findByTipo(String tipo);
}
 