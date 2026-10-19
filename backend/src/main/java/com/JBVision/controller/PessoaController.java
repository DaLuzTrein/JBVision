package com.JBVision.controller;
 
import com.JBVision.entity.Foto;
import com.JBVision.entity.Pessoa;
import com.JBVision.repository.FotoRepository;
import com.JBVision.repository.PessoaRepository;
import com.JBVision.service.CompreFaceService;
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
@RequestMapping("/api/pessoas")
public class PessoaController {
 
    private final CompreFaceService compreFaceService;
    private final PessoaRepository pessoaRepository;
    private final FotoRepository fotoRepository;
    
    private static final String PASTA_FOTOS = "fotos/";
 
    public PessoaController(CompreFaceService compreFaceService,
                            PessoaRepository pessoaRepository,
                            FotoRepository fotoRepository) {
        this.compreFaceService = compreFaceService;
        this.pessoaRepository = pessoaRepository;
        this.fotoRepository = fotoRepository;
    }
 
    // Listar todas as pessoas
    @GetMapping
    public ResponseEntity<List<Pessoa>> listarPessoas() {
        return ResponseEntity.ok(pessoaRepository.findAll());
    }
 
    // Listar por tipo (ALUNO ou FUNCIONARIO)
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Pessoa>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(pessoaRepository.findByTipo(tipo.toUpperCase()));
    }
 
    // Buscar pessoa específica pelo identificador
    @GetMapping("/{identificador}")
    public ResponseEntity<?> buscarPessoa(@PathVariable String identificador) {
        return pessoaRepository.findByIdentificador(identificador)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    // Servir foto capa
    @GetMapping("/foto/{identificador}")
    public ResponseEntity<Resource> buscarFoto(@PathVariable String identificador) {
        try {
            Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
            if (pessoaOpt.isEmpty() || pessoaOpt.get().getFotoRosto() == null) {
                return ResponseEntity.notFound().build();
            }
            Path caminho = Paths.get(pessoaOpt.get().getFotoRosto());
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
 
    // Servir foto específica pelo id
    @GetMapping("/foto/id/{id}")
    public ResponseEntity<Resource> buscarFotoPorId(@PathVariable Long id) {
        try {
            Optional<Foto> fotoOpt = fotoRepository.findById(id);
            if (fotoOpt.isEmpty()) return ResponseEntity.notFound().build();
            Path caminho = Paths.get(fotoOpt.get().getCaminho());
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
 
    // Listar fotos de uma pessoa
    @GetMapping("/{identificador}/fotos")
    public ResponseEntity<List<Foto>> listarFotos(@PathVariable String identificador) {
        return ResponseEntity.ok(fotoRepository.findByIdentificador(identificador));
    }
 
    // Cadastrar pessoa
    @PostMapping("/cadastrar")
    public ResponseEntity<String> cadastrarPessoa(
            @RequestParam("identificador") String identificador,
            @RequestParam("nome") String nome,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            if (!tipo.equalsIgnoreCase("ALUNO") && !tipo.equalsIgnoreCase("FUNCIONARIO")) {
                return ResponseEntity.badRequest().body("Tipo inválido. Use ALUNO ou FUNCIONARIO.");
            }
 
            boolean semFotos = fotos == null || fotos.isEmpty() || fotos.stream().allMatch(MultipartFile::isEmpty);
            if (semFotos) return ResponseEntity.badRequest().body("É necessário enviar pelo menos 1 foto.");
 
            boolean sucesso = compreFaceService.cadastrarPessoa(identificador, nome, tipo.toUpperCase(), fotos);
            return sucesso
                    ? ResponseEntity.ok(tipo + " " + nome + " cadastrado(a) com sucesso!")
                    : ResponseEntity.badRequest().body("Erro ao cadastrar.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
 
    // Editar nome, identificador e/ou tipo
    @PutMapping("/{identificador}")
    public ResponseEntity<String> editarPessoa(
            @PathVariable String identificador,
            @RequestParam(value = "novoNome", required = false) String novoNome,
            @RequestParam(value = "novoIdentificador", required = false) String novoIdentificador,
            @RequestParam(value = "novoTipo", required = false) String novoTipo) {
        try {
            Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
            if (pessoaOpt.isEmpty()) return ResponseEntity.notFound().build();
 
            Pessoa pessoa = pessoaOpt.get();
            if (novoNome != null && !novoNome.isBlank()) pessoa.setNome(novoNome);
            if (novoIdentificador != null && !novoIdentificador.isBlank()) pessoa.setIdentificador(novoIdentificador);
            if (novoTipo != null && !novoTipo.isBlank()) {
                if (!novoTipo.equalsIgnoreCase("ALUNO") && !novoTipo.equalsIgnoreCase("FUNCIONARIO")) {
                    return ResponseEntity.badRequest().body("Tipo inválido. Use ALUNO ou FUNCIONARIO.");
                }
                pessoa.setTipo(novoTipo.toUpperCase());
            }
            pessoaRepository.save(pessoa);
            return ResponseEntity.ok("Pessoa atualizada com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
 
    // Adicionar fotos
    @PostMapping("/{identificador}/fotos")
    public ResponseEntity<String> adicionarFotos(
            @PathVariable String identificador,
            @RequestParam("fotos") List<MultipartFile> fotos) {
        try {
            boolean semFotos = fotos == null || fotos.isEmpty() || fotos.stream().allMatch(MultipartFile::isEmpty);
            if (semFotos) return ResponseEntity.badRequest().body("Nenhuma foto enviada.");
 
            Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
            if (pessoaOpt.isEmpty()) return ResponseEntity.notFound().build();
 
            Pessoa pessoa = pessoaOpt.get();
            boolean sucesso = compreFaceService.cadastrarPessoa(identificador, pessoa.getNome(), pessoa.getTipo(), fotos);
            return sucesso
                    ? ResponseEntity.ok("Fotos adicionadas com sucesso.")
                    : ResponseEntity.badRequest().body("Erro ao adicionar fotos.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
 
    // Apagar foto específica
    @DeleteMapping("/fotos/{id}")
    public ResponseEntity<String> deletarFoto(@PathVariable Long id) {
        try {
            Optional<Foto> fotoOpt = fotoRepository.findById(id);
            if (fotoOpt.isEmpty()) return ResponseEntity.notFound().build();
 
            Foto foto = fotoOpt.get();
 
            Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(foto.getIdentificador());
            if (pessoaOpt.isPresent() && foto.getCaminho().equals(pessoaOpt.get().getFotoRosto())) {
                pessoaOpt.get().setFotoRosto(null);
                pessoaRepository.save(pessoaOpt.get());
            }
 
            Files.deleteIfExists(Paths.get(foto.getCaminho()));
            fotoRepository.delete(foto);
            return ResponseEntity.ok("Foto removida.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
 
    // Trocar foto capa
    @PutMapping("/{identificador}/fotoCapa/{id}")
    public ResponseEntity<String> trocarFotoCapa(
            @PathVariable String identificador,
            @PathVariable Long id) {
        try {
            Optional<Pessoa> pessoaOpt = pessoaRepository.findByIdentificador(identificador);
            Optional<Foto> fotoOpt = fotoRepository.findById(id);
 
            if (pessoaOpt.isEmpty() || fotoOpt.isEmpty()) return ResponseEntity.notFound().build();
 
            pessoaOpt.get().setFotoRosto(fotoOpt.get().getCaminho());
            pessoaRepository.save(pessoaOpt.get());
            return ResponseEntity.ok("Foto capa atualizada.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
 
    // Deletar pessoa
    @DeleteMapping("/{identificador}")
    public ResponseEntity<String> deletarPessoa(@PathVariable String identificador) {
        return pessoaRepository.findByIdentificador(identificador)
                .map(pessoa -> {
                    compreFaceService.deletarPessoa(identificador);
                    pessoaRepository.delete(pessoa);
                    return ResponseEntity.ok("Pessoa " + pessoa.getNome() + " removida com sucesso.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
 
    // Resetar tudo
    @DeleteMapping("/resetar")
    public ResponseEntity<String> resetarTudo() {
        try {
            compreFaceService.resetarTudo();
            pessoaRepository.deleteAll();
            return ResponseEntity.ok("Todas as pessoas removidas do banco e do CompreFace.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao resetar: " + e.getMessage());
        }
    }
}