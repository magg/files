package com.example.file.mapper;

import com.example.file.domain.FileDomain;
import com.example.file.dto.FileDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@SuppressWarnings("unused")
public interface FileMapper extends ObjectConverter<FileDomain, FileDTO>
{

}
