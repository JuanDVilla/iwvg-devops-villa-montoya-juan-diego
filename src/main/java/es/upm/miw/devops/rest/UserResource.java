package es.upm.miw.devops.rest;

import es.upm.miw.devops.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserResource {

    private static final String MESSAGE = "message";

    private final UserRepository userRepository;

    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "Usuario no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(MESSAGE, "Usuario no encontrado"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(MESSAGE, "Usuario eliminado"));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<Object> updateActiveStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        // Validamos que el body contenga la clave 'active'
        if (body == null || !body.containsKey("active")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(MESSAGE, "Formato incorrecto. Se esperaba {'active': true/false}"));
        }

        return userRepository.findById(id)
                .map(user -> {
                    // Actualizamos el estado.
                    // NOTA: Asumo que tu entidad User tiene un método setter llamado setActive()
                    user.setActive(body.get("active"));

                    // Guardamos los cambios en la base de datos
                    userRepository.save(user);

                    return ResponseEntity
                            .ok((Object) Map.of(MESSAGE, "Estado del usuario actualizado a: " + body.get("active")));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "Usuario no encontrado")));
    }
}