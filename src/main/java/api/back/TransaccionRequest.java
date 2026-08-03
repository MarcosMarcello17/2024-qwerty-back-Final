package api.back;

import java.time.LocalDate;

public class TransaccionRequest {
    private Double valor;
    private String email;
    private String motivo;
    private String id_reserva; // Nuevo campo id_reserva
    private LocalDate fecha; // Fecha por defecto al crear

    // Constructor
    public TransaccionRequest() {
        this.fecha = LocalDate.now(); // Establecer fecha actual por defecto
    }

    public TransaccionRequest(Double valor, String email, String motivo, String id_reserva, LocalDate fecha) {
        this.valor = valor;
        this.email = email;
        this.motivo = motivo;
        this.fecha = fecha;
        this.id_reserva = id_reserva;
    }

    // Getters y Setters
    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getId_reserva() {
        return id_reserva;
    }

    public void setId_reserva(String id_reserva) {
        this.id_reserva = id_reserva;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
