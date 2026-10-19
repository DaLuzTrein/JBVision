package com.JBVision.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "registros")
public class Registro {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String identificador; // identificador da pessoa reconhecida
    private String nomePessoa;
    private String tipoPessoa;    // ALUNO ou FUNCIONARIO
    private String camera;
    private double similaridade;
    private LocalDateTime horario;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
 
    public String getNomePessoa() { return nomePessoa; }
    public void setNomePessoa(String nomePessoa) { this.nomePessoa = nomePessoa; }
 
    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }
 
    public String getCamera() { return camera; }
    public void setCamera(String camera) { this.camera = camera; }
    
    public double getSimilaridade() { return similaridade; }
    public void setSimilaridade(double similaridade) { this.similaridade = similaridade; }
 
    public LocalDateTime getHorario() { return horario; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }
 
    @Override
    public String toString() {
        return "Registro [id=" + id + ", identificador=" + identificador + ", nomePessoa=" + nomePessoa
                + ", tipoPessoa=" + tipoPessoa + ", camera=" + camera + ", similaridade=" + similaridade + ", horario=" + horario + "]";
    }
}
 