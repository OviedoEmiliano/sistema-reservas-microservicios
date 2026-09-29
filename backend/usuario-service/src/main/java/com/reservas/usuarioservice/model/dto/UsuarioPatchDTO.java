package com.reservas.usuarioservice.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioPatchDTO {

    @Size(max = 50)
    private String nombre;

    @Size(max = 50)
    private String apellido;

    @Pattern(regexp = "\\d{8,15}", message = "El número de celular debe contener solo dígitos")
    private String nroCelular;
}