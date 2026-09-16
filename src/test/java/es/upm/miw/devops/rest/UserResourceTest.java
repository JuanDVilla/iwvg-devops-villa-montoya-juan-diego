package es.upm.miw.devops.rest;

import es.upm.miw.devops.model.Role;
import es.upm.miw.devops.model.User;
import es.upm.miw.devops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserResource.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFirstName("Juan");
        sampleUser.setFamilyName("Pérez");
        sampleUser.setIdentity("12345678Z");
        sampleUser.setEmail("juan@example.com");
        sampleUser.setAddress("Calle 123");
        sampleUser.setCity("Madrid");
        sampleUser.setProvince("Madrid");
        sampleUser.setPostalCode("28001");
        sampleUser.setActive(true);
        sampleUser.setRole(Role.OPERATOR);
    }

    // ==========================================
    // PRUEBAS GET /user/{id}
    // ==========================================

    @Test
    void testGetUserSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.identity").value("12345678Z"))
                .andExpect(jsonPath("$.billable").value(true));
    }

    @Test
    void testGetUserNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    // ==========================================
    // PRUEBAS DELETE /user/{id}
    // ==========================================

    @Test
    void testDeleteUserSuccess() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado"));

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        when(userRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(userRepository, never()).deleteById(anyLong());
    }

    // ==========================================
    // PRUEBAS PUT /user/{id} (Actualización Completa)
    // ==========================================

    @Test
    void testUpdateUserSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String updateJson = """
                {
                    "firstName": "Carlos",
                    "familyName": "Gómez",
                    "identity": "87654321X",
                    "email": "carlos@example.com",
                    "address": "Avenida 456",
                    "city": "Barcelona",
                    "province": "Barcelona",
                    "postalCode": "08001",
                    "active": false,
                    "role": "OPERATOR"
                }
                """;

        mockMvc.perform(put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.familyName").value("Gómez"))
                .andExpect(jsonPath("$.identity").value("87654321X"))
                .andExpect(jsonPath("$.email").value("carlos@example.com"))
                .andExpect(jsonPath("$.city").value("Barcelona"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.billable").value(true));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String updateJson = """
                {
                    "firstName": "Carlos",
                    "familyName": "Gómez",
                    "email": "carlos@example.com"
                }
                """;

        mockMvc.perform(put("/user/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // PRUEBAS PUT /user/{id}/active
    // ==========================================

    @Test
    void testUpdateActiveStatusSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String jsonBody = "{\"active\": false}";

        mockMvc.perform(put("/user/1/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Estado del usuario actualizado a: false"));

        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testUpdateActiveStatusBadRequestMissingKey() throws Exception {
        String invalidJsonBody = "{\"invalidKey\": true}";

        mockMvc.perform(put("/user/1/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Formato incorrecto. Se esperaba {'active': true/false}"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateActiveStatusNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String jsonBody = "{\"active\": true}";

        mockMvc.perform(put("/user/99/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // PRUEBAS PATCH /user (Actualización en Lote + Validaciones de Rol)
    // ==========================================

    @Test
    void testUpdateUsersActiveStatusBatchSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        String batchJson = """
                [
                    {"id": 1, "active": false}
                ]
                """;

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuarios actualizados correctamente"));

        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testUpdateUsersActiveStatusBatchEmptyList() throws Exception {
        String emptyBatchJson = "[]";

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyBatchJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La lista de actualizaciones no puede estar vacía"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUsersActiveStatusBatchAdminDeactivationForbidden() throws Exception {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFirstName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        String batchJson = """
                [
                    {"id": 2, "active": false}
                ]
                """;

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede desactivar a un usuario con rol ADMIN (ID: 2)"));

        verify(userRepository, never()).save(adminUser);
    }

    @Test
    void testUpdateUsersActiveStatusBatchAdminActivationSuccess() throws Exception {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFirstName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        String batchJson = """
                [
                    {"id": 2, "active": true}
                ]
                """;

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuarios actualizados correctamente"));

        verify(userRepository, times(1)).save(adminUser);
    }
}