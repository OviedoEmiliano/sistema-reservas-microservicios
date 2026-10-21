package com.reservas.usuarioservice.model.dto;

import com.reservas.usuarioservice.model.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Representación pública de un usuario registrado")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Perez")
    private String apellido;

    @Schema(description = "Correo electrónico registrado", example = "juan.perez@test.com")
    private String email;

    @Schema(description = "Número de celular", example = "1122334455")
    private String nroCelular;

    @Schema(description = "Rol asignado dentro de la plataforma", example = "ROLE_CLIENTE")
    private Rol rol;

    @Schema(description = "Fecha y hora de creación del registro", example = "2026-09-27T10:15:30")
    private LocalDateTime fechaCreacion;
}