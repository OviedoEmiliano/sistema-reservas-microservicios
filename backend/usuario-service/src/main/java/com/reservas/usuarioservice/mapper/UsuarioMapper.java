package com.reservas.usuarioservice.mapper;

import com.reservas.usuarioservice.model.dto.UsuarioPatchDTO;
import com.reservas.usuarioservice.model.dto.UsuarioRequestDTO;
import com.reservas.usuarioservice.model.dto.UsuarioResponseDTO;
import com.reservas.usuarioservice.model.dto.UsuarioUpdateDTO;
import com.reservas.usuarioservice.model.entity.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void actualizarEntity(UsuarioUpdateDTO dto, @MappingTarget Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void patchEntity(UsuarioPatchDTO dto, @MappingTarget Usuario usuario);
}