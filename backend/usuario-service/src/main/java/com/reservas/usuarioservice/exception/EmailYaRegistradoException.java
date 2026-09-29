package com.reservas.usuarioservice.exception;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException(String email) {
        super("El email ya se encuentra registrado en el sistema: " + email);
    }
}