package com.reservas.usuarioservice.controller;

import com.reservas.usuarioservice.exception.EmailYaRegistradoException;
import com.reservas.usuarioservice.exception.UsuarioNoEncontradoException;
import com.reservas.usuarioservice.mapper.UsuarioMapper;
import com.reservas.usuarioservice.service.UsuarioService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UsuarioControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

    // ---------- Errores del framework (los arregla ResponseEntityExceptionHandler) ----------

    @Test
    void crearUsuario_conJsonMalformado_devuelve400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void obtenerUsuario_conIdNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void metodoNoSoportado_devuelve405() throws Exception {
        mockMvc.perform(post("/api/usuarios/{id}", 1))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rutaInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearUsuario_sinContentType_devuelve415() throws Exception {
        mockMvc.perform(post("/api/usuarios").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ---------- Validación ----------

    @Test
    void crearUsuario_conDatosInvalidos_devuelveErroresPorCampo() throws Exception {
        String invalido = """
                { "nombre": "", "apellido": "Perez", "email": "no-es-un-email",
                  "password": "123", "nroCelular": "abc" }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.errores.nombre").isArray())
                .andExpect(jsonPath("$.errores.email").isArray())
                .andExpect(jsonPath("$.errores.password").isArray())
                .andExpect(jsonPath("$.errores.nroCelular").isArray())
                .andExpect(jsonPath("$.errores.apellido").doesNotExist());
    }

    // ---------- Excepciones de dominio (el service lanza, el handler traduce) ----------

    @Test
    void obtenerUsuario_inexistente_devuelve404ProblemDetail() throws Exception {
        when(usuarioService.buscarPorId(99L)).thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(get("/api/usuarios/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.detail").value("Usuario no encontrado con ID: 99"));
    }

    @Test
    void crearUsuario_conEmailDuplicado_devuelve409ProblemDetail() throws Exception {
        when(usuarioService.registrarUsuario(any()))
                .thenThrow(new EmailYaRegistradoException("juan.perez@test.com"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Juan", "apellido": "Perez",
                                  "email": "juan.perez@test.com", "password": "password123",
                                  "nroCelular": "1122334455" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email ya registrado"));
    }

    // ---------- Catch-all ----------

    @Test
    void errorInesperado_devuelve500SinFiltrarDetalles() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenThrow(new RuntimeException("detalle secreto de la BD"));

        mockMvc.perform(get("/api/usuarios/{id}", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secreto"))));
    }
    @Test
    void patchUsuario_conNombreVacio_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").isArray());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void patchUsuario_conApellidoSoloEspacios_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"apellido\": \"   \" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.apellido").isArray());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void patchUsuario_conCelularInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nroCelular\": \"abc\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nroCelular").isArray());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void patchUsuario_soloConCelularValido_noFallaLaValidacion() throws Exception {
        mockMvc.perform(patch("/api/usuarios/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nroCelular\": \"1155667788\" }"))
                .andExpect(status().isOk());
    }

    @Test
    void crearUsuario_conPasswordDeMasDe72Caracteres_devuelve400() throws Exception {
        String larga = "a".repeat(73);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"Juan\", \"apellido\": \"Perez\", "
                                + "\"email\": \"juan@test.com\", \"password\": \"" + larga + "\", "
                                + "\"nroCelular\": \"1122334455\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.password").isArray());
    }
    @Test
    void crearUsuario_conPasswordMultibyteQueSuperaLos72Bytes_devuelve400() throws Exception {
        String multibyte = "ñ".repeat(40); // 40 caracteres, 80 bytes
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"Juan\", \"apellido\": \"Perez\", "
                                + "\"email\": \"juan@test.com\", \"password\": \"" + multibyte + "\", "
                                + "\"nroCelular\": \"1122334455\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.passwordDentroDelLimite").isArray());
    }
}