package com.JBVision.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cameras")
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String token; // identificador único usado pelo script Python
    private String fotoCapa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getFotoCapa() { return fotoCapa; }
    public void setFotoCapa(String fotoCapa) { this.fotoCapa = fotoCapa; }

    @Override
    public String toString() {
        return "Camera [id=" + id + ", nome=" + nome + ", token=" + token + "]";
    }
}