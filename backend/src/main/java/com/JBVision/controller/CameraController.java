package com.JBVision.controller;

import com.JBVision.entity.Camera;
import com.JBVision.repository.CameraRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraRepository cameraRepository;
    private static final String PASTA_FOTOS_CAMERAS = "fotos/cameras/";

    public CameraController(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
    }

    // Lista todas as câmeras
    @GetMapping
    public ResponseEntity<List<Camera>> listarCameras() {
        return ResponseEntity.ok(cameraRepository.findAll());
    }

    // Cadastra câmera — retorna o objeto completo com id e token
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarCamera(
            @RequestParam("nome") String nome,
            @RequestParam("token") String token,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {
        try {
            // Verifica se o token já existe
            if (cameraRepository.findByToken(token).isPresent()) {
                return ResponseEntity.badRequest().body("Já existe uma câmera com esse token.");
            }

            Camera camera = new Camera();
            camera.setNome(nome);
            camera.setToken(token);
            camera = cameraRepository.save(camera);

            if (foto != null && !foto.isEmpty()) {
                Files.createDirectories(Paths.get(PASTA_FOTOS_CAMERAS));
                String extensao = foto.getOriginalFilename() != null && foto.getOriginalFilename().contains(".")
                        ? foto.getOriginalFilename().substring(foto.getOriginalFilename().lastIndexOf("."))
                        : ".jpg";
                String nomeSalvo = "camera_" + camera.getId() + extensao;
                Files.write(Paths.get(PASTA_FOTOS_CAMERAS + nomeSalvo), foto.getBytes());
                camera.setFotoCapa(PASTA_FOTOS_CAMERAS + nomeSalvo);
                camera = cameraRepository.save(camera);
            }

            return ResponseEntity.ok(camera);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    // Editar nome e/ou token da câmera
    @PutMapping("/{id}")
    public ResponseEntity<String> editarCamera(
            @PathVariable Long id,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "token", required = false) String token) {
        try {
            Optional<Camera> cameraOpt = cameraRepository.findById(id);
            if (cameraOpt.isEmpty()) return ResponseEntity.notFound().build();

            Camera camera = cameraOpt.get();

            if (nome != null && !nome.isBlank()) camera.setNome(nome);

            if (token != null && !token.isBlank()) {
                // Verifica se o novo token já pertence a outra câmera
                Optional<Camera> existente = cameraRepository.findByToken(token);
                if (existente.isPresent() && !existente.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body("Esse token já está em uso por outra câmera.");
                }
                camera.setToken(token);
            }

            cameraRepository.save(camera);
            return ResponseEntity.ok("Câmera atualizada.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    // Trocar foto capa
    @PutMapping("/{id}/foto")
    public ResponseEntity<String> trocarFoto(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile foto) {
        try {
            Optional<Camera> cameraOpt = cameraRepository.findById(id);
            if (cameraOpt.isEmpty()) return ResponseEntity.notFound().build();

            Camera camera = cameraOpt.get();

            if (camera.getFotoCapa() != null) {
                Files.deleteIfExists(Paths.get(camera.getFotoCapa()));
            }

            Files.createDirectories(Paths.get(PASTA_FOTOS_CAMERAS));
            String extensao = foto.getOriginalFilename() != null && foto.getOriginalFilename().contains(".")
                    ? foto.getOriginalFilename().substring(foto.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String nomeSalvo = "camera_" + id + extensao;
            Files.write(Paths.get(PASTA_FOTOS_CAMERAS + nomeSalvo), foto.getBytes());
            camera.setFotoCapa(PASTA_FOTOS_CAMERAS + nomeSalvo);
            cameraRepository.save(camera);

            return ResponseEntity.ok("Foto atualizada.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    // Servir foto capa
    @GetMapping("/foto/{id}")
    public ResponseEntity<Resource> buscarFoto(@PathVariable Long id) {
        try {
            Optional<Camera> cameraOpt = cameraRepository.findById(id);
            if (cameraOpt.isEmpty() || cameraOpt.get().getFotoCapa() == null) {
                return ResponseEntity.notFound().build();
            }
            Path caminho = Paths.get(cameraOpt.get().getFotoCapa());
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Deletar câmera
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarCamera(@PathVariable Long id) {
        Optional<Camera> cameraOpt = cameraRepository.findById(id);
        if (cameraOpt.isEmpty()) return ResponseEntity.notFound().build();

        if (cameraOpt.get().getFotoCapa() != null) {
            try {
                Files.deleteIfExists(Paths.get(cameraOpt.get().getFotoCapa()));
            } catch (Exception e) {
                System.err.println("Aviso: não foi possível apagar foto da câmera.");
            }
        }

        cameraRepository.deleteById(id);
        return ResponseEntity.ok("Câmera removida.");
    }
}