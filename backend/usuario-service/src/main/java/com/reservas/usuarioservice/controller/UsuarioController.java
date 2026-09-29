package com.reservas.usuarioservice.controller;

import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioRequestDTO;
import com.reservas.usuarioservice.model.dto.UsuarioResponseDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import com.reservas.usuarioservice.mapper.UsuarioMapper;
import com.reservas.usuarioservice.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping
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
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuario));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodos(pageable)
                .map(usuarioMapper::toResponseDTO);

        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuarioActualizado));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> patchUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioPatchDTO dto) {

        Usuario usuarioActualizado = usuarioService.patchUsuario(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuarioActualizado));
    }
}