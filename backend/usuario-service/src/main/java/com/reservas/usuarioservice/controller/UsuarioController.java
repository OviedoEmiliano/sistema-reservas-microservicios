package com.reservas.usuarioservice.controller;

import com.reservas.usuarioservice.exception.CampoOrdenInvalidoException;
import com.reservas.usuarioservice.mapper.UsuarioMapper;
import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioRequestDTO;
import com.reservas.usuarioservice.model.dto.UsuarioResponseDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import com.reservas.usuarioservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springdoc.core.annotations.ParameterObject;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones CRUD para la administración de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    private static final List<String> CAMPOS_ORDENABLES =
            List.of("id", "nombre", "apellido", "email", "fechaCreacion");

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un usuario nuevo en el sistema, encripta su contraseña y genera el header Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (validación @Valid)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario usuarioEntity = usuarioMapper.toEntity(dto);
        Usuario usuarioGuardado = usuarioService.registrarUsuario(usuarioEntity);
        UsuarioResponseDTO responseDTO = usuarioMapper.toResponseDTO(usuarioGuardado);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Recupera la información pública de un usuario según su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(
            @Parameter(description = "ID del usuario a buscar", example = "1") @PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuario));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios paginados",
            description = "Devuelve el listado paginado. Campos permitidos para ordenamiento: id, nombre, apellido, email, fechaCreacion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetro de orden inválido fuera de la lista blanca",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        validarOrden(pageable);

        Page<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodos(pageable)
                .map(usuarioMapper::toResponseDTO);

        return ResponseEntity.ok(usuarios);
    }

    private void validarOrden(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!CAMPOS_ORDENABLES.contains(order.getProperty())) {
                throw new CampoOrdenInvalidoException(order.getProperty(), CAMPOS_ORDENABLES);
            }
        });
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualización completa", description = "Reemplaza los campos nombre, apellido y celular del usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios faltantes o no válidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @Parameter(description = "ID del usuario a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuarioActualizado));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualización parcial", description = "Modifica únicamente los campos enviados en el cuerpo de la solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario modificado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Formato de campo inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> patchUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Valid @RequestBody UsuarioPatchDTO dto) {

        Usuario usuarioActualizado = usuarioService.patchUsuario(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuarioActualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina físicamente el registro de un usuario del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado con éxito"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", example = "1") @PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}