package com.magg.files.service;

import com.magg.files.domain.FileDomain;
import com.magg.api.dto.FileDTO;
import com.magg.api.exception.EntityNotFoundException;
import com.magg.api.exception.FileNotFoundException;
import com.magg.files.mapper.ObjectConverter;
import com.magg.files.repository.FileRepository;
import com.magg.storage.AssetType;
import com.magg.storage.FilePointer;
import com.magg.storage.StorageService;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static com.magg.files.domain.FileDomain.FILE_COLLECTION_NAME;

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

        String name = fileDTO.getExternalId() + "/" +filename;

        storageService.upload3(stream, AssetType.TEST, contentType, name, fileDTO.getExternalId() );

        FileDTO fileDto = insertObject(fileDTO);

        try {
            updateSize(name, fileDto);
        } catch (EntityNotFoundException | MethodArgumentNotValidException e) {
            e.printStackTrace();
        }

        return fileDto;
    }

    public void delete(String id)
    {
        delete(id);
    }

    public FileDTO update(String id, FileDTO fileDTO) throws EntityNotFoundException, MethodArgumentNotValidException
    {
        return patchObject(id, fileDTO);
    }


    public ResponseEntity<Resource>  download(String id) throws EntityNotFoundException
    {

        Optional<FileDTO> file = find(id);

        if (file.isEmpty()) {
            throw new EntityNotFoundException(id);
        }

        Optional<FilePointer> filePointer = file
            .map(fileDTO -> storageService
            .findFile(fileDTO))
            .orElse(null);


        if (filePointer.isEmpty()) {
            throw new FileNotFoundException();
        }

        Resource resource = filePointer.map(storageService::prepareResponse)
            .orElseGet(storageService::notFound);
        return response(filePointer.get(), HttpStatus.OK, resource, file.get());

    }


    private ResponseEntity<Resource> response(FilePointer filePointer, HttpStatus status, Resource body, FileDTO fileDTO) {

        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
            .filename(filePointer.getOriginalName())
            .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        final ResponseEntity.BodyBuilder responseBuilder = ResponseEntity
            .status(status)
            .contentLength(fileDTO.getSize())
            .headers(headers)
            .lastModified(fileDTO.getLastModifiedDate().toInstant().toEpochMilli());

        filePointer.getMediaType().map(this::toMediaType)
            .ifPresent(responseBuilder::contentType);
        return responseBuilder.body(body);
    }

    @Async
    public void updateSize(String name, FileDTO dto) throws EntityNotFoundException, MethodArgumentNotValidException
    {
        Long size = storageService.retrieveContentLength(name);
        dto.setSize(size);
        update(dto.getId(), dto);
    }

    private MediaType toMediaType(String input) {
        return MediaType.valueOf(input);
    }


}
