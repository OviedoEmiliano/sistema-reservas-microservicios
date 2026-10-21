package com.reservas.usuarioservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Schema(description = "Datos requeridos para dar de alta un nuevo usuario")
public class UsuarioRequestDTO {

    @Schema(description = "Nombre del usuario", example = "Juan")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Perez")
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String apellido;

    @Schema(description = "Correo electrónico (debe ser único en el sistema)", example = "juan.perez@test.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100)
    private String email;

    @Schema(description = "Contraseña en texto plano (entre 8 y 72 caracteres/bytes)", example = "password123")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    @Schema(description = "Número telefónico de contacto (solo dígitos, de 8 a 15 caracteres)", example = "1122334455")
    @NotBlank(message = "El número de celular es obligatorio")
    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;

    @JsonIgnore
    @AssertTrue(message = "La contraseña no puede superar los 72 bytes")
    public boolean isPasswordDentroDelLimite() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}