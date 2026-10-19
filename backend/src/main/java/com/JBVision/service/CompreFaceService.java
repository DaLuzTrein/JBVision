package com.JBVision.service;
 
import com.JBVision.dto.CompreFaceResponse;
import com.JBVision.dto.Result;
import com.JBVision.dto.Subject;
import com.JBVision.dto.SubjectListResponse;
import com.JBVision.entity.Foto;
import com.JBVision.entity.Pessoa;
import com.JBVision.repository.FotoRepository;
import com.JBVision.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
 
@Service
public class CompreFaceService {
 
    private final RestTemplate restTemplate = new RestTemplate();
    private final PessoaRepository pessoaRepository;
    private final FotoRepository fotoRepository;
 
    @Value("${compre-face.url}")
    private String compreFaceUrl;
 
    @Value("${compre-face.api-key}")
    private String apiKey;
 
    private static final String PASTA_FOTOS = "fotos/";
 
    public CompreFaceService(PessoaRepository pessoaRepository, FotoRepository fotoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.fotoRepository = fotoRepository;
    }
 
    public Optional<Subject> reconhecerPessoa(byte[] imagemBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
 
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imagemBytes) {
                @Override
                public String getFilename() { return "frame.jpg"; }
            });
 
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            String url = compreFaceUrl + "?limit=1&det_prob_threshold=0.7&prediction_count=1";
 
            ResponseEntity<CompreFaceResponse> response =
                    restTemplate.postForEntity(url, request, CompreFaceResponse.class);
 
            if (response.getBody() != null && !response.getBody().getResult().isEmpty()) {
                Result result = response.getBody().getResult().get(0);
                if (result.getSubjects() != null && !result.getSubjects().isEmpty()) {
                    Subject subject = result.getSubjects().get(0);
                    if (subject.getSimilarity() >= 0.98) {
                        return Optional.of(subject);
                    }
                }
            }
 
        } catch (Exception e) {
            System.err.println("Erro ao se comunicar com CompreFace: " + e.getMessage());
            e.printStackTrace();
        }
 
        return Optional.empty();
    }
 
    public boolean cadastrarPessoa(String identificador, String nome, String tipo, List<MultipartFile> fotos) {
        try {
            String urlCompreFace = compreFaceUrl.replace("/recognize", "/faces") + "?subject=" + identificador;
            Files.createDirectories(Paths.get(PASTA_FOTOS));
 
            Optional<Pessoa> pessoaExistente = pessoaRepository.findByIdentificador(identificador);
            Pessoa pessoa = pessoaExistente.orElseGet(Pessoa::new);
            pessoa.setIdentificador(identificador);
            pessoa.setNome(nome);
            pessoa.setTipo(tipo);
 
            for (int i = 0; i < fotos.size(); i++) {
                MultipartFile foto = fotos.get(i);
                if (!foto.isEmpty()) {
                    byte[] bytes = foto.getBytes();
                    final String nomeArquivo = foto.getOriginalFilename() != null
                            ? foto.getOriginalFilename() : "foto.jpg";
 
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("x-api-key", apiKey);
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add("file", new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() { return nomeArquivo; }
                    });
                    restTemplate.postForEntity(urlCompreFace, new HttpEntity<>(body, headers), String.class);
 
                    String extensao = nomeArquivo.contains(".")
                            ? nomeArquivo.substring(nomeArquivo.lastIndexOf(".")) : ".jpg";
                    String nomeSalvo = identificador + "_" + System.currentTimeMillis() + "_" + i + extensao;
                    Path destino = Paths.get(PASTA_FOTOS + nomeSalvo);
                    Files.write(destino, bytes);
 
                    Foto fotoEntity = new Foto();
                    fotoEntity.setIdentificador(identificador);
                    fotoEntity.setCaminho(PASTA_FOTOS + nomeSalvo);
                    fotoRepository.save(fotoEntity);
 
                    if (pessoa.getFotoRosto() == null) {
                        pessoa.setFotoRosto(PASTA_FOTOS + nomeSalvo);
                    }
 
                    System.out.println("Foto salva: " + nomeSalvo);
                }
            }
 
            pessoaRepository.save(pessoa);
            System.out.println("Pessoa salva: " + nome + " (" + identificador + ") [" + tipo + "]");
            return true;
 
        } catch (Exception e) {
            System.err.println("Erro ao cadastrar pessoa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean deletarPessoa(String identificador) {
        try {
            String url = compreFaceUrl.replace("/recognize", "/faces") + "?subject=" + identificador;
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
 
            List<Foto> fotos = fotoRepository.findByIdentificador(identificador);
            for (Foto foto : fotos) {
                try {
                    Files.deleteIfExists(Paths.get(foto.getCaminho()));
                } catch (IOException e) {
                    System.err.println("Aviso: não foi possível remover foto: " + foto.getCaminho());
                }
            }
 
            fotoRepository.deleteByIdentificador(identificador);
            System.out.println("Pessoa removida do CompreFace: " + identificador);
            return true;
 
        } catch (Exception e) {
            System.err.println("Erro ao remover pessoa do CompreFace: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean resetarTudo() {
        try {
            String urlSubjects = compreFaceUrl.replace("/recognize", "/faces/subjects");
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
 
            ResponseEntity<SubjectListResponse> response = restTemplate.exchange(
                    urlSubjects, HttpMethod.GET, new HttpEntity<>(headers), SubjectListResponse.class);
 
            if (response.getBody() != null && response.getBody().getSubjects() != null) {
                for (String subject : response.getBody().getSubjects()) {
                    try {
                        String urlDeletar = compreFaceUrl.replace("/recognize", "/faces") + "?subject=" + subject;
                        restTemplate.exchange(urlDeletar, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
                        System.out.println("Subject removido do CompreFace: " + subject);
                    } catch (Exception e) {
                        System.err.println("Erro ao remover subject: " + subject);
                    }
                }
            }
 
            try {
                Path pastaFotos = Paths.get(PASTA_FOTOS);
                if (Files.exists(pastaFotos)) {
                    Files.walk(pastaFotos)
                            .filter(Files::isRegularFile)
                            .forEach(arquivo -> {
                                try { Files.delete(arquivo); }
                                catch (IOException e) { System.err.println("Aviso: não foi possível apagar: " + arquivo); }
                            });
                }
            } catch (IOException e) {
                System.err.println("Aviso: erro ao limpar pasta de fotos: " + e.getMessage());
            }
 
            fotoRepository.deleteAll();
            System.out.println("Reset completo — CompreFace, disco e banco limpos.");
            return true;
 
        } catch (Exception e) {
            System.err.println("Erro ao resetar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
 