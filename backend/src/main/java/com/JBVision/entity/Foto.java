package com.JBVision.entity;
 
import jakarta.persistence.*;
 
@Entity
@Table(name = "fotos")
public class Foto {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String identificador; // identificador da pessoa dona da foto
    private String caminho;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
 
    public String getCaminho() { return caminho; }
    public void setCaminho(String caminho) { this.caminho = caminho; }
}
 