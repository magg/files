package com.example.file.service;

import com.example.file.domain.FileDomain;
import com.example.file.dto.FileDTO;
import com.example.file.exception.EntityNotFoundException;
import com.example.file.mapper.ObjectConverter;
import com.example.file.repository.FileRepository;
import com.example.file.storage.AssetType;
import com.example.file.storage.StorageService;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import static com.example.file.domain.FileDomain.FILE_COLLECTION_NAME;

@Component
public class FileService extends ObjectService<FileDomain, FileDTO, FileRepository> {

    private final StorageService storageService;

    public FileService(
        FileRepository fileRepository,
        ObjectConverter<FileDomain, FileDTO> converter, StorageService storageService) {

        super(FILE_COLLECTION_NAME,
            fileRepository,
            converter,
            FileDTO.class
        );
        this.storageService = storageService;
    }

    public Optional<FileDTO> find(String id)
    {
        return getObject(id);
    }

    public FileDTO create(InputStream stream, String contentType, String filename)
    {

        FileDTO fileDTO = new FileDTO();
        fileDTO.setFilename(filename);
        fileDTO.setContentType(contentType);
        fileDTO.setExternalId(UUID.randomUUID().toString());

        storageService.upload(stream, AssetType.TEST, contentType, fileDTO.getExternalId() + "/" +filename);

        return insertObject(fileDTO);
    }

    public void delete(String id)
    {
        delete(id);
    }

    public FileDTO update(String id, FileDTO fileDTO) throws EntityNotFoundException
    {
        return doUpdate(id, fileDTO);
    }

}
