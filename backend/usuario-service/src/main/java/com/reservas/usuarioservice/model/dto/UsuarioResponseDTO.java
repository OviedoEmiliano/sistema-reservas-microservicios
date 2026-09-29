package com.reservas.usuarioservice.model.dto;

import com.reservas.usuarioservice.model.enums.Rol;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String nroCelular;
    private Rol rol;
    private LocalDateTime fechaCreacion;
}