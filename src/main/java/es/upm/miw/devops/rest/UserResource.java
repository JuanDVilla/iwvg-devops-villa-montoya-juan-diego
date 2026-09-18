package es.upm.miw.devops.rest;

import es.upm.miw.devops.model.User;
import java.util.Optional;
import es.upm.miw.devops.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserResource {

    private static final String MESSAGE = "message";

    private final UserRepository userRepository;

    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record UserActiveDto(Long id, Boolean active) {
    }

    public record UserUpdateDto(
            String firstName,
            String familyName,
            String identity,
            String email,
            String address,
            String city,
            String province,
            String postalCode,
            Boolean active) {
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

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userDto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Mapeamos los datos del DTO a la entidad
                    existingUser.setFirstName(userDto.firstName());
                    existingUser.setFamilyName(userDto.familyName());
                    existingUser.setIdentity(userDto.identity());
                    existingUser.setEmail(userDto.email());
                    existingUser.setAddress(userDto.address());
                    existingUser.setCity(userDto.city());
                    existingUser.setProvince(userDto.province());
                    existingUser.setPostalCode(userDto.postalCode());
                    existingUser.setActive(userDto.active());

                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok((Object) updatedUser);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "Usuario no encontrado")));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<Object> updateActiveStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        if (body == null || !body.containsKey("active")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(MESSAGE, "Formato incorrecto. Se esperaba {'active': true/false}"));
        }

        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(body.get("active"));
                    userRepository.save(user);

                    return ResponseEntity
                            .ok((Object) Map.of(MESSAGE, "Estado del usuario actualizado a: " + body.get("active")));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "Usuario no encontrado")));
    }

    @PatchMapping
    public ResponseEntity<Object> updateUsersActiveStatusBatch(@RequestBody List<UserActiveDto> updates) {
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(MESSAGE, "La lista de actualizaciones no puede estar vacía"));
        }

        for (UserActiveDto update : updates) {
            if (update.id() != null && update.active() != null) {
                Optional<User> userOpt = userRepository.findById(update.id());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Validación: No se permite desactivar (active = false) a un usuario ADMIN
                    boolean isAttemptingDeactivation = Boolean.FALSE.equals(update.active());
                    boolean isAdmin = isAdminUser(user);

                    if (isAttemptingDeactivation && isAdmin) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(MESSAGE, "No se puede desactivar a un usuario con rol ADMIN (ID: "
                                        + user.getId() + ")"));
                    }

                    user.setActive(update.active());
                    userRepository.save(user);
                }
            }
        }

        return ResponseEntity.ok(Map.of(MESSAGE, "Usuarios actualizados correctamente"));
    }

    private boolean isAdminUser(User user) {
        return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().toString());
    }
}