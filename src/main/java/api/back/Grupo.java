package api.back;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@Table(name = "grupos")
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;  // Nombre del grupo

    @Column(nullable = false)
    private boolean estado;  // Estado del grupo: abierto o cerrado

    // Relación de muchos a muchos con User
    @ManyToMany
    @JoinTable(
        name = "grupo_usuarios",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> usuarios;

    // Relación de uno a muchos con GrupoTransacciones
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GrupoTransacciones> transacciones;

    public Grupo(String nombre, User creadorGrupo) {
        this.nombre = nombre;
        this.usuarios = new ArrayList<>();
        this.transacciones = new ArrayList<>();
        this.usuarios.add(creadorGrupo);
    }

    public Grupo() {
        this.usuarios = new ArrayList<>();
        this.transacciones = new ArrayList<>();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public List<User> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<User> usuarios) {
        this.usuarios = usuarios;
    }

    public List<GrupoTransacciones> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<GrupoTransacciones> transacciones) {
        this.transacciones = transacciones;
    }
}
