package com.reservas.usuarioservice.controller;

import com.reservas.usuarioservice.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.reservas.usuarioservice.model.entity.Usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "DB_PASSWORD=test")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class UsuarioControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USUARIO_VALIDO = """
            {
              "nombre": "Juan",
              "apellido": "Perez",
              "email": "juan.perez@test.com",
              "password": "password123",
              "nroCelular": "1122334455"
            }
            """;

    private Usuario guardarUsuario() {
        return guardarUsuario("juan.perez@test.com");
    }

    private Usuario guardarUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail(email);
        usuario.setPassword("password123");
        usuario.setNroCelular("1122334455");
        return usuarioRepository.save(usuario);
    }
    @BeforeEach
    void limpiarBase() {
        usuarioRepository.deleteAll();
    }

    @Test
    void crearUsuario_conDatosValidos_devuelve201YPersiste() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("juan.perez@test.com"))
                .andExpect(jsonPath("$.rol").value("ROLE_CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void crearUsuario_conEmailDuplicado_devuelve409() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO))
                .andExpect(status().isConflict());

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void crearUsuario_conDatosInvalidos_devuelve400YNoPersiste() throws Exception {
        String invalido = """
                {
                  "nombre": "",
                  "apellido": "Perez",
                  "email": "no-es-un-email",
                  "password": "123",
                  "nroCelular": "abc"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void patchUsuario_soloActualizaLosCamposEnviados() throws Exception {
        Usuario guardado = guardarUsuario();

        mockMvc.perform(patch("/api/usuarios/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nroCelular": "1155667788" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nroCelular").value("1155667788"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Perez"));

        Usuario enBase = usuarioRepository.findById(guardado.getId()).orElseThrow();
        assertThat(enBase.getNroCelular()).isEqualTo("1155667788");
        assertThat(enBase.getNombre()).isEqualTo("Juan");
        assertThat(enBase.getApellido()).isEqualTo("Perez");
        assertThat(enBase.getEmail()).isEqualTo("juan.perez@test.com");
    }

    @Test
    void patchUsuario_conIdInexistente_devuelve404() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nroCelular": "1155667788" }
                            """))
                .andExpect(status().isNotFound());
    }
    @Test
    void obtenerUsuario_conIdExistente_devuelve200() throws Exception {
        Usuario guardado = guardarUsuario();

        mockMvc.perform(get("/api/usuarios/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.email").value("juan.perez@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void obtenerUsuario_conIdInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarUsuario_conDatosValidos_reemplazaLosCampos() throws Exception {
        Usuario guardado = guardarUsuario();

        mockMvc.perform(put("/api/usuarios/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Juan Carlos",
                              "apellido": "Perez Gomez",
                              "nroCelular": "1199887766"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Carlos"));

        Usuario enBase = usuarioRepository.findById(guardado.getId()).orElseThrow();
        assertThat(enBase.getNombre()).isEqualTo("Juan Carlos");
        assertThat(enBase.getApellido()).isEqualTo("Perez Gomez");
        assertThat(enBase.getNroCelular()).isEqualTo("1199887766");
        assertThat(enBase.getEmail()).isEqualTo("juan.perez@test.com");
    }

    @Test
    void actualizarUsuario_conNombreVacio_devuelve400() throws Exception {
        Usuario guardado = guardarUsuario();

        mockMvc.perform(put("/api/usuarios/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "",
                              "apellido": "Perez",
                              "nroCelular": "1122334455"
                            }
                            """))
                .andExpect(status().isBadRequest());

        Usuario enBase = usuarioRepository.findById(guardado.getId()).orElseThrow();
        assertThat(enBase.getNombre()).isEqualTo("Juan");
    }

    @Test
    void eliminarUsuario_conIdExistente_devuelve204YLoBorra() throws Exception {
        Usuario guardado = guardarUsuario();

        mockMvc.perform(delete("/api/usuarios/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        assertThat(usuarioRepository.existsById(guardado.getId())).isFalse();
    }

    @Test
    void eliminarUsuario_conIdInexistente_devuelve404() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarUsuarios_respetaElTamanioDePagina() throws Exception {
        guardarUsuario("a@test.com");
        guardarUsuario("b@test.com");
        guardarUsuario("c@test.com");

        mockMvc.perform(get("/api/usuarios").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
    @Test
    void crearUsuario_conEmailEnMayusculas_loGuardaEnMinusculasYDetectaDuplicado() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nombre": "Juan", "apellido": "Perez",
                              "email": "Juan.Perez@Test.com", "password": "password123",
                              "nroCelular": "1122334455" }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan.perez@test.com"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO))
                .andExpect(status().isConflict());

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }


    @Test
    void crearUsuario_guardaElPasswordHasheadoEnLaBase() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_VALIDO))
                .andExpect(status().isCreated());

        Usuario enBase = usuarioRepository.findByEmail("juan.perez@test.com").orElseThrow();
        assertThat(enBase.getPassword()).isNotEqualTo("password123");
        assertThat(enBase.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("password123", enBase.getPassword())).isTrue();
    }
}