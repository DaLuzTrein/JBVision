package com.JBVision.repository;
 
import com.JBVision.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long> {
 
    List<Registro> findByIdentificador(String identificador);
 
    List<Registro> findByTipoPessoa(String tipoPessoa);
}
 