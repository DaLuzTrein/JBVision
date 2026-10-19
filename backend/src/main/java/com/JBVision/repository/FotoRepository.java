package com.JBVision.repository;
 
import com.JBVision.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
 
    List<Foto> findByIdentificador(String identificador);
 
    void deleteByIdentificador(String identificador);
}
 