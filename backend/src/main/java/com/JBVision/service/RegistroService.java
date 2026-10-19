package com.JBVision.service;
 
import com.JBVision.entity.Pessoa;
import com.JBVision.entity.Registro;
import com.JBVision.repository.PessoaRepository;
import com.JBVision.repository.RegistroRepository;
import org.springframework.stereotype.Service;
 
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
 
@Service
public class RegistroService {
 
    private final RegistroRepository registroRepository;
    private final PessoaRepository pessoaRepository;
 
    // Tempo de cooldown para reconhecer a mesma pessoa
    private static final int COOLDOWN_DE_RECONHECIMENTO = 20;
 
    public RegistroService(RegistroRepository registroRepository, PessoaRepository pessoaRepository) {
        this.registroRepository = registroRepository;
        this.pessoaRepository = pessoaRepository;
    }
 
    // Retorna true se registrou, false se o cooldown ainda não acabou
    public boolean registrarRegistro(String identificador, String camera, double similaridade) {
 
        Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
        String nomePessoa = "Desconhecido";
        String tipoPessoa = "DESCONHECIDO";
 
        if (pessoaOpt.isPresent()) {
            Pessoa pessoa = pessoaOpt.get();
            nomePessoa = pessoa.getNome();
            tipoPessoa = pessoa.getTipo();
 
            // Sistema de cooldown para evitar spam no banco
            if (pessoa.getVistoPorUltimo() != null) {
                long segundosDesdeUltimaVez = ChronoUnit.SECONDS.between(
                        pessoa.getVistoPorUltimo(),
                        LocalDateTime.now()
                );
 
                if (segundosDesdeUltimaVez < COOLDOWN_DE_RECONHECIMENTO) {
                    System.out.println("Ignorado (visto há " + segundosDesdeUltimaVez + "s): " + nomePessoa);
                    return false;
                }
            }
 
            // Atualiza o visto por último
            pessoa.setVistoPorUltimo(LocalDateTime.now());
            pessoaRepository.save(pessoa);
 
        } else {
            System.out.println("Pessoa reconhecida pelo CompreFace mas não encontrada no banco: " + identificador);
        }
 
        Registro registro = new Registro();
        registro.setIdentificador(identificador);
        registro.setNomePessoa(nomePessoa);
        registro.setTipoPessoa(tipoPessoa);
        registro.setCamera(camera);
        registro.setHorario(LocalDateTime.now());
        registro.setSimilaridade(similaridade);
 
        registroRepository.save(registro);
 
        System.out.println("Captura registrada: " + nomePessoa + " [" + tipoPessoa + "] (" + identificador + ") | Camera: " + camera);
        return true;
    }
}