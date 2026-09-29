package com.reservas.usuarioservice.service;

import com.reservas.usuarioservice.exception.EmailYaRegistradoException;
import com.reservas.usuarioservice.exception.UsuarioNoEncontradoException;
import com.reservas.usuarioservice.mapper.UsuarioMapper;
import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import com.reservas.usuarioservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailYaRegistradoException(usuario.getEmail());
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return obtenerUsuarioOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> obtenerTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = obtenerUsuarioOrThrow(id);
        usuarioMapper.actualizarEntity(dto, usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario patchUsuario(Long id, UsuarioPatchDTO dto) {
        Usuario usuario = obtenerUsuarioOrThrow(id);
        usuarioMapper.patchEntity(dto, usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNoEncontradoException(id);
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario obtenerUsuarioOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }
}