package com.reservas.usuarioservice.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(Long id) {
        super("Usuario no encontrado con ID: " + id);
    }
}