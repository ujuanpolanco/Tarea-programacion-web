package co.edu.usco.seguridadroles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de la matriz de permisos. Los usuarios se cargan con
 * @WithUserDetails, que llama a nuestro UserDetailsServiceImpl, asi que
 * tambien se comprueba la "lectura desde DB".
 *
 * Corren sobre H2 en memoria con los mismos schema.sql y data.sql
 * (ver src/test/resources/application.properties), no hace falta MySQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadRolesApplicationTests {

    @Autowired
    private MockMvc mvc;

    // ---------- Login contra la base de datos ----------

    @Test
    void loginCorrectoConUsuarioDeLaBase() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("123456"))
                .andExpect(authenticated().withUsername("admin")
                        .withAuthorities(List.of(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginConContrasenaIncorrecta() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("mala"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginConUsuarioDeshabilitado() throws Exception {
        mvc.perform(formLogin("/login").user("inactivo").password("123456"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?deshabilitado"));
    }

    @Test
    void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---------- USER: solo puede ver la lista ----------

    @Test
    @WithUserDetails("usuario")
    void userVeLaLista() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("usuario")
    void userNoPuedeCrearEditarNiEliminar() throws Exception {
        mvc.perform(get("/new")).andExpect(status().isForbidden()).andExpect(forwardedUrl("/403"));
        mvc.perform(get("/edit/1")).andExpect(status().isForbidden());
        // Un POST rechazado tambien debe ir a la pagina personalizada /403
        mvc.perform(post("/delete/1").with(csrf())).andExpect(status().isForbidden()).andExpect(forwardedUrl("/403"));
    }

    // ---------- CREATOR: ver y crear ----------

    @Test
    @WithUserDetails("creador")
    void creatorPuedeCrearPeroNoEditar() throws Exception {
        mvc.perform(get("/new")).andExpect(status().isOk());
        mvc.perform(get("/edit/1")).andExpect(status().isForbidden());
        mvc.perform(post("/delete/1").with(csrf())).andExpect(status().isForbidden());
    }

    // ---------- EDITOR: ver y editar ----------

    @Test
    @WithUserDetails("editor")
    void editorPuedeEditarPeroNoCrearNiEliminar() throws Exception {
        mvc.perform(get("/edit/1")).andExpect(status().isOk());
        mvc.perform(get("/new")).andExpect(status().isForbidden());
        mvc.perform(post("/delete/1").with(csrf())).andExpect(status().isForbidden());
    }

    // ---------- Usuario con dos roles (CREATOR + EDITOR) ----------

    @Test
    @WithUserDetails("juan")
    void usuarioConDosRolesSumaLosPermisos() throws Exception {
        mvc.perform(get("/new")).andExpect(status().isOk());
        mvc.perform(get("/edit/1")).andExpect(status().isOk());
        mvc.perform(post("/delete/1").with(csrf())).andExpect(status().isForbidden());
    }

    // ---------- ADMIN: todo ----------

    @Test
    @WithUserDetails("admin")
    void adminEntraATodo() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/new")).andExpect(status().isOk());
        mvc.perform(get("/edit/1")).andExpect(status().isOk());
        // No se borra de verdad un producto: id inexistente, deleteById no falla
        mvc.perform(post("/delete/999999").with(csrf())).andExpect(redirectedUrl("/"));
    }
}
