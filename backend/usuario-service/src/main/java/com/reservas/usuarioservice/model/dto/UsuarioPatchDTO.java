package com.reservas.usuarioservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para modificación parcial (PATCH)")
public class UsuarioPatchDTO {
    @Schema(description = "Nombre (opcional)", example = "Juan")
    @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
    @Size(max = 50)
    private String nombre;

    @Schema(description = "Apellido (opcional)", example = "Perez")
    @Pattern(regexp = ".*\\S.*", message = "El apellido no puede estar vacío")
    @Size(max = 50)
    private String apellido;

    @Schema(description = "Número de celular (opcional)", example = "1155667788")
    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;
}