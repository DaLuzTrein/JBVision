package com.JBVision.entity;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "pessoas")
public class Pessoa {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String identificador; // matrícula para alunos, número de registro para funcionários
    private String nome;
    private String tipo;          // ALUNO ou FUNCIONARIO
    private LocalDateTime vistoPorUltimo;
    private String fotoRosto;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
 
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
 
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
 
    public LocalDateTime getVistoPorUltimo() { return vistoPorUltimo; }
    public void setVistoPorUltimo(LocalDateTime vistoPorUltimo) { this.vistoPorUltimo = vistoPorUltimo; }
 
    public String getFotoRosto() { return fotoRosto; }
    public void setFotoRosto(String fotoRosto) { this.fotoRosto = fotoRosto; }
 
    @Override
    public String toString() {
        return "Pessoa [id=" + id + ", identificador=" + identificador + ", nome=" + nome
                + ", tipo=" + tipo + ", vistoPorUltimo=" + vistoPorUltimo + "]";
    }
}