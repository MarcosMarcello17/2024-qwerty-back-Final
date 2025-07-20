package api.back;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String motivo;
    private Double valor;
    private String categoria;
    private String tipoGasto;
    private String frecuencia; // Ej: "MENSUAL", "SEMANAL"
    private LocalDate nextExecution; // Fecha de la próxima ejecución

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public RecurringTransaction() {}

    public RecurringTransaction(String motivo, Double valor, String categoria, String tipoGasto, String frecuencia, LocalDate nextExecution, User user) {
        this.motivo = motivo;
        this.valor = valor;
        this.categoria = categoria;
        this.tipoGasto = tipoGasto;
        this.frecuencia = frecuencia;
        this.nextExecution = nextExecution;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTipoGasto() { return tipoGasto; }
    public void setTipoGasto(String tipoGasto) { this.tipoGasto = tipoGasto; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public LocalDate getNextExecution() { return nextExecution; }
    public void setNextExecution(LocalDate nextExecution) { this.nextExecution = nextExecution; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
