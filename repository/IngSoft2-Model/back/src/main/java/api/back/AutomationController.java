package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/automation")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class AutomationController {

    @Autowired
    private AutomationService automationService;

    /**
     * Endpoint para distribuir automáticamente un ingreso
     */
    @PostMapping("/distribuir")
    public ResponseEntity<?> distribuirIngreso(@RequestBody DistribucionRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            List<Transacciones> transaccionesCreadas = automationService.distribuirIngresoAutomaticamente(
                request.getMontoIngreso(),
                request.getFechaIngreso(),
                userEmail,
                request.getMotivoOriginal()
            );

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Distribución realizada exitosamente",
                "transaccionesCreadas", transaccionesCreadas.size(),
                "transacciones", transaccionesCreadas
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al distribuir ingreso: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para verificar si se puede distribuir automáticamente
     */
    @GetMapping("/puede-distribuir")
    public ResponseEntity<?> puedeDistribuir(@RequestParam String fecha, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            LocalDate fechaLocal = LocalDate.parse(fecha);
            
            boolean puedeDistribuir = automationService.puedeDistribuirAutomaticamente(userEmail, fechaLocal);
            
            return ResponseEntity.ok().body(Map.of(
                "puedeDistribuir", puedeDistribuir
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al verificar distribución: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para obtener una previsualización de la distribución
     */
    @PostMapping("/previsualizar")
    public ResponseEntity<?> previsualizarDistribucion(@RequestBody DistribucionRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            AutomationService.DistributionPreview preview = automationService.obtenerPrevisualizacionDistribucion(
                request.getMontoIngreso(),
                userEmail,
                request.getFechaIngreso()
            );

            if (preview.getError() != null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", preview.getError()
                ));
            }

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "preview", preview
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al generar previsualización: " + e.getMessage()
            ));
        }
    }

    // Clase para el request de distribución
    public static class DistribucionRequest {
        private Double montoIngreso;
        private LocalDate fechaIngreso;
        private String motivoOriginal;

        public Double getMontoIngreso() {
            return montoIngreso;
        }

        public void setMontoIngreso(Double montoIngreso) {
            this.montoIngreso = montoIngreso;
        }

        public LocalDate getFechaIngreso() {
            return fechaIngreso;
        }

        public void setFechaIngreso(LocalDate fechaIngreso) {
            this.fechaIngreso = fechaIngreso;
        }

        public String getMotivoOriginal() {
            return motivoOriginal;
        }

        public void setMotivoOriginal(String motivoOriginal) {
            this.motivoOriginal = motivoOriginal;
        }
    }
}
