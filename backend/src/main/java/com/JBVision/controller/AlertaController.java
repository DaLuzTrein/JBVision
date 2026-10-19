package com.JBVision.controller;

import com.JBVision.entity.Alerta;
import com.JBVision.repository.AlertaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaRepository alertaRepository;

    public AlertaController(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    // Lista alertas ativos (não dispensados) — usado ao carregar o dashboard
    @GetMapping
    public ResponseEntity<List<Alerta>> listarAtivos() {
        return ResponseEntity.ok(alertaRepository.findByDispensadoFalse());
    }

    // Dispensa um alerta específico
    @PutMapping("/{id}/dispensar")
    public ResponseEntity<String> dispensar(@PathVariable Long id) {
        Optional<Alerta> alertaOpt = alertaRepository.findById(id);
        if (alertaOpt.isEmpty()) return ResponseEntity.notFound().build();
        alertaOpt.get().setDispensado(true);
        alertaRepository.save(alertaOpt.get());
        return ResponseEntity.ok("Alerta dispensado.");
    }

    // Dispensa todos os alertas de uma vez
    @PutMapping("/dispensar-todos")
    public ResponseEntity<String> dispensarTodos() {
        List<Alerta> ativos = alertaRepository.findByDispensadoFalse();
        ativos.forEach(a -> a.setDispensado(true));
        alertaRepository.saveAll(ativos);
        return ResponseEntity.ok("Todos os alertas dispensados.");
    }
}