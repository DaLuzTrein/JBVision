package com.JBVision.repository;

import com.JBVision.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    // Busca só os alertas que ainda não foram dispensados
    List<Alerta> findByDispensadoFalse();
}