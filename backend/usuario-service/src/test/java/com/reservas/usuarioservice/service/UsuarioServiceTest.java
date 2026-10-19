package com.reservas.usuarioservice.service;

import com.reservas.usuarioservice.exception.EmailYaRegistradoException;
import com.reservas.usuarioservice.exception.UsuarioNoEncontradoException;
import com.reservas.usuarioservice.mapper.UsuarioMapper;
import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import com.reservas.usuarioservice.model.enums.Rol;
import com.reservas.usuarioservice.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan.perez@test.com");
        usuario.setPassword("password123");
        usuario.setNroCelular("1122334455");
        usuario.setRol(Rol.ROLE_CLIENTE);
    }

    @Test
    void registrarUsuario_conEmailNuevo_debeGuardarYDevolverUsuario() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.registrarUsuario(usuario);

        assertThat(resultado).isEqualTo(usuario);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void registrarUsuario_conEmailDuplicado_debeLanzarExcepcion() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuario))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining(usuario.getEmail());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorId_conIdExistente_debeDevolverUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void actualizarUsuario_conIdExistente_debeAplicarCambiosYGuardar() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Juan Carlos");
        dto.setApellido("Perez Gomez");
        dto.setNroCelular("1199887766");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.actualizarUsuario(1L, dto);

        verify(usuarioMapper).actualizarEntity(dto, usuario);
        verify(usuarioRepository).save(usuario);
        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void actualizarUsuario_conIdInexistente_debeLanzarExcepcion() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizarUsuario(99L, dto))
                .isInstanceOf(UsuarioNoEncontradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void patchUsuario_debeAplicarSoloElMetodoDePatch() {
        UsuarioPatchDTO dto = new UsuarioPatchDTO();
        dto.setNroCelular("1155667788");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        usuarioService.patchUsuario(1L, dto);

        verify(usuarioMapper).patchEntity(dto, usuario);
        verify(usuarioMapper, never()).actualizarEntity(any(), any());
    }

    @Test
    void eliminarUsuario_conIdExistente_debeEliminar() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void eliminarUsuario_conIdInexistente_debeLanzarExcepcionYNoEliminar() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                .isInstanceOf(UsuarioNoEncontradoException.class);

        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    void registrarUsuario_normalizaElEmailAntesDeGuardar() {
        usuario.setEmail("  Juan.Perez@Test.COM ");
        when(usuarioRepository.existsByEmail("juan.perez@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.registrarUsuario(usuario);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("juan.perez@test.com");
    }

    @Test
    void registrarUsuario_conEmailDuplicadoEnOtroCase_lanzaExcepcion() {
        usuario.setEmail("JUAN.PEREZ@TEST.COM");
        when(usuarioRepository.existsByEmail("juan.perez@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuario))
                .isInstanceOf(EmailYaRegistradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarUsuario_siLaBdRechazaElEmailPorConcurrencia_lanzaEmailYaRegistrado() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.save(usuario))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuario))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining(usuario.getEmail());
    }
    @Test
    void registrarUsuario_guardaElPasswordHasheado() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.registrarUsuario(usuario);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hash-simulado");
    }

    @Test
    void registrarUsuario_conEmailDuplicado_noGastaTiempoHasheando() {
        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuario))
                .isInstanceOf(EmailYaRegistradoException.class);

        verifyNoInteractions(passwordEncoder);
    }
}