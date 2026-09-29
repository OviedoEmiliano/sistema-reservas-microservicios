package com.reservas.usuarioservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String apellido;

    @NotBlank(message = "El número de celular es obligatorio")
    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;
}