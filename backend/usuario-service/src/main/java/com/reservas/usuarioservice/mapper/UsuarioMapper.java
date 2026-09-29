package com.reservas.usuarioservice.mapper;

import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioRequestDTO;
import com.reservas.usuarioservice.model.dto.UsuarioResponseDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toEntity(UsuarioRequestDTO dto);

    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void actualizarEntity(UsuarioUpdateDTO dto, @MappingTarget Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(UsuarioPatchDTO dto, @MappingTarget Usuario usuario);
}