package com.JBVision.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String camera;
    private LocalDateTime horario;
    private boolean dispensado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCamera() { return camera; }
    public void setCamera(String camera) { this.camera = camera; }

    public LocalDateTime getHorario() { return horario; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }

    public boolean isDispensado() { return dispensado; }
    public void setDispensado(boolean dispensado) { this.dispensado = dispensado; }
}