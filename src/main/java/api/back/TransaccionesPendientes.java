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
@Table(name = "transaccionesPendientes")
public class TransaccionesPendientes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String motivo;
    private String id_reserva;
    private LocalDate fecha;
    private String sentByEmail;
    private Long grupoId;

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public TransaccionesPendientes() {
    }

    public TransaccionesPendientes(Double valor, User user, String motivo, String id_reserva, LocalDate fecha) {
        this.valor = valor;
        this.user = user;
        this.motivo = motivo;
        this.id_reserva = id_reserva;
        this.fecha = fecha;
    }

    public TransaccionesPendientes(Double valor, User user, String motivo, LocalDate fecha) {
        this.valor = valor;
        this.user = user;
        this.motivo = motivo;
        this.id_reserva = null;
        this.fecha = fecha;
    }

    public String getSentByEmail() {
        return sentByEmail;
    }

    public void setSentByEmail(String sentByEmail) {
        this.sentByEmail = sentByEmail;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
