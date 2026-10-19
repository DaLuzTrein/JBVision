package com.JBVision.repository;

import com.JBVision.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {

    // Busca câmera pelo token — usado pelo script Python
    Optional<Camera> findByToken(String token);
}