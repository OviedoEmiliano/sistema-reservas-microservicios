package com.reservas.usuarioservice.controller;

import com.reservas.usuarioservice.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.reservas.usuarioservice.model.entity.Usuario;

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

    private static final String USUARIO_VALIDO = """
            {
              "nombre": "Juan",
              "apellido": "Perez",
              "email": "juan.perez@test.com",
              "password": "password123",
              "nroCelular": "1122334455"
            }
            """;

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
    private Usuario guardarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan.perez@test.com");
        usuario.setPassword("password123");
        usuario.setNroCelular("1122334455");
        return usuarioRepository.save(usuario);
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
}