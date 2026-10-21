package com.reservas.usuarioservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para actualización completa (PUT)")
public class UsuarioUpdateDTO {
    @Schema(description = "Nombre del usuario", example = "Juan Carlos")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Perez Gomez")
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String apellido;

    @Schema(description = "Número de celular", example = "1199887766")
    @NotBlank(message = "El número de celular es obligatorio")
    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;
}