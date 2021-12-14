package com.magg.files.mapper;

import com.magg.files.domain.FileDomain;
import com.magg.api.dto.FileDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@SuppressWarnings("unused")
public interface FileMapper extends ObjectConverter<FileDomain, FileDTO>
{

}
