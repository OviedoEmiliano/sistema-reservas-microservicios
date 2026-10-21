package com.reservas.usuarioservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---------- Excepciones de dominio ----------

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ProblemDetail handleEmailDuplicado(EmailYaRegistradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Email ya registrado");
        return problema;
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Usuario no encontrado");
        return problema;
    }

    // ---------- Validación (@Valid sobre el body) ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // Agrupa por campo: si un campo falla dos reglas, conserva ambos mensajes
        Map<String, List<String>> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Valor inválido",
                                Collectors.toList())));

        ProblemDetail problema = ex.getBody();
        problema.setTitle("Error de validación");
        problema.setDetail("Uno o más campos no son válidos");
        problema.setProperty("errores", errores);

        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @ExceptionHandler(CampoOrdenInvalidoException.class)
    public ProblemDetail handleCampoOrdenInvalido(CampoOrdenInvalidoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problema.setTitle("Parámetro de orden inválido");
        return problema;
    }

    // ---------- Catch-all: último recurso ----------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intentá nuevamente más tarde.");
        problema.setTitle("Error interno");
        return problema;
    }
}