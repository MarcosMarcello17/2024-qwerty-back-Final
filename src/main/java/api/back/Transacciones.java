package api.back;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "transacciones") // Nombre de la tabla en la base de datos
public class Transacciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double valor;
    private String motivo;
    private String categoria;
    private String tipoGasto;
    private Boolean distribuida = false; // Indica si esta transacción ya fue distribuida automáticamente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Relación con la entidad User
    // private Calendar fecha;

    private LocalDate fecha;

    // Constructor por defecto
    public Transacciones() {
    }

    // Constructor con parámetros (opcional)
    public Transacciones(Double valor, String motivo, LocalDate fecha,
            String categoria, String tipoGasto) {
        this.valor = valor;
        this.motivo = motivo;
        this.fecha = fecha;
        this.categoria = categoria;
        this.tipoGasto = tipoGasto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTipoGasto() {
        return tipoGasto;
    }

    public void setTipoGasto(String tipoGasto) {
        this.tipoGasto = tipoGasto;
    }

    public Boolean getDistribuida() {
        return distribuida;
    }

    public void setDistribuida(Boolean distribuida) {
        this.distribuida = distribuida;
    }
}
