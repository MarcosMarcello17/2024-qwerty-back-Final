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

        // Mapear las categorías a CategoriaRequest
        return categorias.stream()
                .map(cat -> new CategoriaRequest(cat.getNombre(), cat.getIconPath()))
                .collect(Collectors.toList());

    }

    @PostMapping
    public ResponseEntity<Void> addPersonalCategoria(@RequestBody CategoriaRequest categoria,
            Authentication authentication) {
        String email = authentication.getName();
        List<PersonalCategoria> categorias = personalCategoriaService.getPersonalCategoria(email);
        System.out.println(categorias);
        String nombreCategoria = categoria.getNombre().trim().replaceAll("\"", "");
        for (PersonalCategoria catPersonal : categorias) {
            System.out.println("Categoria transacción: '" + catPersonal.getNombre() + "'");
            if (catPersonal.getNombre() != null
                    && nombreCategoria.equalsIgnoreCase(catPersonal.getNombre().trim())) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Si no hay coincidencias, añadir la categoría
        personalCategoriaService.addPersonalCategoria(email, nombreCategoria, categoria.getIconPath());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePersonalCategoria(@RequestBody CategoriaRequest categoria,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Transacciones> transaccionesUser = transaccionesController.getTransaccionesByUser(authentication);

            for (Transacciones transaccion : transaccionesUser) {
                // Comparar las categorías usando equals()
                if (transaccion.getCategoria().equals(categoria.getNombre())) {
                    transaccion.setCategoria("Otros");
                    // Actualizar la transacción con la nueva categoría
                    transaccionesController.updateTransaccion(transaccion.getId(), transaccion, authentication);
                }
            }

            // Llamar al servicio para eliminar la categoría
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
            for (PersonalCategoria item : categorias) {
                if (item.getNombre().equals(nombre)) {
                    System.out.println("Found: " + item);
                    item.setNombre(newCategoria.getNombre());
                    item.setIconPath(newCategoria.getIconPath());
                    // Persistimos los cambios
                    personalCategoriaService.save(item);
                    found = true;
                    break; // Si ya lo encontramos, podemos salir del loop
                }
            }
            if (found) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para saber exactamente qué ocurre
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
