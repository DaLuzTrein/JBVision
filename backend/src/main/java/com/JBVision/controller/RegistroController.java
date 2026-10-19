package com.JBVision.controller;

import com.JBVision.dto.Subject;
import com.JBVision.entity.Alerta;
import com.JBVision.entity.Camera;
import com.JBVision.entity.Pessoa;
import com.JBVision.entity.Registro;
import com.JBVision.repository.AlertaRepository;
import com.JBVision.repository.CameraRepository;
import com.JBVision.repository.PessoaRepository;
import com.JBVision.repository.RegistroRepository;
import com.JBVision.service.CompreFaceService;
import com.JBVision.service.RegistroService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/registro")
public class RegistroController {

    private final CompreFaceService compreFaceService;
    private final RegistroService registroService;
    private final RegistroRepository registroRepository;
    private final CameraRepository cameraRepository;
    private final PessoaRepository pessoaRepository;
    private final AlertaRepository alertaRepository;
    private final SimpMessagingTemplate messagingTemplate; // envia mensagens via WebSocket

    public RegistroController(CompreFaceService compreFaceService,
                              RegistroService registroService,
                              RegistroRepository registroRepository,
                              CameraRepository cameraRepository,
                              PessoaRepository pessoaRepository,
                              AlertaRepository alertaRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.compreFaceService = compreFaceService;
        this.registroService = registroService;
        this.registroRepository = registroRepository;
        this.cameraRepository = cameraRepository;
        this.pessoaRepository = pessoaRepository;
        this.alertaRepository = alertaRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Listar todos os registros
    @GetMapping
    public ResponseEntity<List<Registro>> listarRegistros() {
        return ResponseEntity.ok(registroRepository.findAll());
    }

    // Listar registros de uma pessoa específica
    @GetMapping("/pessoa/{identificador}")
    public ResponseEntity<List<Registro>> listarRegistrosPorPessoa(@PathVariable String identificador) {
        return ResponseEntity.ok(registroRepository.findByIdentificador(identificador));
    }

    // Listar registros por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Registro>> listarRegistrosPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(registroRepository.findByTipoPessoa(tipo.toUpperCase()));
    }

    // Recebe foto da câmera e tenta reconhecer a pessoa
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImagem(
            @RequestParam("image") MultipartFile file,
            @RequestParam("cameraToken") String cameraToken) {

        try {
            Optional<Camera> camera = cameraRepository.findByToken(cameraToken);
            if (camera.isEmpty()) {
                return ResponseEntity.badRequest().body("Câmera não encontrada para o token: " + cameraToken);
            }
            String nomeCamera = camera.get().getNome();

            byte[] bytes = file.getBytes();
            Optional<Subject> resultado = compreFaceService.reconhecerPessoa(bytes);

            if (resultado.isPresent()) {
                String identificador = resultado.get().getSubject();
                double similaridade  = resultado.get().getSimilarity();

                Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
                String nomePessoa = pessoaOpt.map(Pessoa::getNome).orElse(identificador);
                String tipoPessoa = pessoaOpt.map(Pessoa::getTipo).orElse("DESCONHECIDO");

                boolean registrado = registroService.registrarRegistro(identificador, nomeCamera, similaridade);

                if (registrado) {
                    return ResponseEntity.ok("[" + tipoPessoa + "] " + nomePessoa
                            + " reconhecido(a) e registrado(a)"
                            + " (similaridade: " + String.format("%.0f%%", similaridade * 100) + ")");
                } else {
                    return ResponseEntity.ok("[" + tipoPessoa + "] " + nomePessoa
                            + " reconhecido(a) mas ignorado(a) (visto recentemente)");
                }

            } else {
                // Pessoa desconhecida — cria alerta e envia via WebSocket
                Alerta alerta = new Alerta();
                alerta.setCamera(nomeCamera);
                alerta.setHorario(LocalDateTime.now());
                alerta.setDispensado(false);
                alertaRepository.save(alerta);

                // Envia o alerta para todos os clientes conectados ao WebSocket
                messagingTemplate.convertAndSend("/topic/alertas", alerta);

                return ResponseEntity.ok("Pessoa desconhecida detectada em: " + nomeCamera);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao processar imagem: " + e.getMessage());
        }
    }

    // Apagar todos os registros
    @DeleteMapping("/resetar")
    public ResponseEntity<String> apagarTudo() {
        try {
            registroRepository.deleteAll();
            return ResponseEntity.ok("Todos os registros apagados.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao resetar: " + e.getMessage());
        }
    }
}