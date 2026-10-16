package com.reservas.usuarioservice.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPatchDTO {

    @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
    @Size(max = 50)
    private String nombre;

    @Pattern(regexp = ".*\\S.*", message = "El apellido no puede estar vacío")
    @Size(max = 50)
    private String apellido;

    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;
}