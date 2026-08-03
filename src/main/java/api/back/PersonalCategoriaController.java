package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/personal-categoria")
public class PersonalCategoriaController {

    @Autowired
    private PersonalCategoriaService personalCategoriaService;

    @Autowired
    private TransaccionesController transaccionesController;

    @GetMapping
    public List<CategoriaRequest> getPersonalCategoria(Authentication authentication) {
        String email = authentication.getName();
        List<PersonalCategoria> categorias = personalCategoriaService.getPersonalCategoria(email);
        return categorias.stream()
                .map(cat -> new CategoriaRequest(cat.getNombre(), cat.getIconPath()))
                .collect(Collectors.toList());

    }

    @PostMapping
    public ResponseEntity<CategoriaRequest> addPersonalCategoria(@RequestBody CategoriaRequest categoria,
            Authentication authentication) {
        String email = authentication.getName();
        if (personalCategoriaService.checkIfNotExist(email, categoria)) {
            personalCategoriaService.addPersonalCategoria(email, categoria.getNombre(), categoria.getIconPath());
            CategoriaRequest categoriaResponse = new CategoriaRequest(categoria.getNombre(), categoria.getIconPath());
            return ResponseEntity.ok(categoriaResponse);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePersonalCategoria(@RequestBody CategoriaRequest categoria,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Transacciones> transaccionesUser = transaccionesController.getTransaccionesByUser(authentication);
            for (Transacciones transaccion : transaccionesUser) {
                if (transaccion.getCategoria().equals(categoria.getNombre())) {
                    transaccion.setCategoria("Otros");
                    transaccionesController.updateTransaccion(transaccion.getId(), transaccion, authentication);
                }
            }
            personalCategoriaService.findAndDeleteCategoria(email, categoria.getNombre(), categoria.getIconPath());
            return ResponseEntity.ok().build();
        } catch (TransaccionNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{nombre}")
    public ResponseEntity<Void> editPersonalCategoria(@PathVariable String nombre,
            @RequestBody CategoriaRequest newCategoria, Authentication authentication) {
        try {
            String email = authentication.getName();
            List<PersonalCategoria> categorias = personalCategoriaService.getPersonalCategoria(email);

            boolean found = false;
            if (personalCategoriaService.checkIfNotExist(email, newCategoria)) {
                for (PersonalCategoria item : categorias) {
                    if (item.getNombre().equals(nombre)) {
                        item.setNombre(newCategoria.getNombre());
                        item.setIconPath(newCategoria.getIconPath());
                        personalCategoriaService.save(item);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    List<Transacciones> transaccionesUser = transaccionesController.getTransaccionesByUser(authentication);
                    for (Transacciones transaccion : transaccionesUser) {
                        if (transaccion.getCategoria().equals(nombre)) {
                            transaccion.setCategoria(newCategoria.getNombre());
                            transaccionesController.updateTransaccion(transaccion.getId(), transaccion, authentication);
                        }
                    }
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para saber exactamente qué ocurre
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
