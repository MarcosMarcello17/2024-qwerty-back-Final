package api.back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.back.model.Grupo;
import api.back.model.User;
import api.back.repository.GrupoRepository;

import java.util.List;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;


    public Grupo crearGrupo(String nombre, User creadorGrupo) {
        Grupo grupo = new Grupo(nombre, creadorGrupo);
        grupo.setEstado(true); // Defino el estado inicial como abierto
        return grupoRepository.save(grupo);
    }

    public List<Grupo> obtenerGruposPorUsuario(String usuarioEmail) {
        return grupoRepository.findByUsuariosEmail(usuarioEmail); 
    }

    // Nuevo método para encontrar un grupo por ID
    public Grupo findById(Long id) {
        return grupoRepository.findById(id).orElse(null);
    }

    // Método para guardar el grupo con los cambios
    public Grupo save(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    public void delete(Long id) {
        grupoRepository.deleteById(id);
    }
    
}
