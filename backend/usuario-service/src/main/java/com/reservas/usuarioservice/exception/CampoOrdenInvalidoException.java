package com.reservas.usuarioservice.exception;

import java.util.Collection;

public class CampoOrdenInvalidoException extends RuntimeException {
    public CampoOrdenInvalidoException(String campo, Collection<String> permitidos) {
        super("No se puede ordenar por '" + campo + "'. Campos permitidos: " + permitidos);
    }
}