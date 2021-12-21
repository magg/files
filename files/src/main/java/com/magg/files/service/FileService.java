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
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static com.magg.files.domain.FileDomain.FILE_COLLECTION_NAME;

@Slf4j
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

    public FileDTO create(InputStream stream, String contentType, String filename, String localPath)
    {

        FileDTO fileDTO = new FileDTO();
        fileDTO.setFilename(filename);
        fileDTO.setContentType(contentType);
        fileDTO.setExternalId(UUID.randomUUID().toString());

        String name = fileDTO.getExternalId() + "/" +filename;

        FileDTO fileDto = insertObject(fileDTO);

        storageService.upload(stream, AssetType.TEST, name, fileDto);

        if (localPath != null) {
            Path deletePath = Path.of(localPath);
            try
            {
                Files.deleteIfExists(deletePath);
                Files.deleteIfExists(deletePath.getParent());
            }
            catch (IOException e)
            {
                log.error(e.getMessage());
            }
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


    public Pair<FileDTO, FilePointer> fetch(String id) throws EntityNotFoundException
    {

        Optional<FileDTO> file = find(id);

        if (file.isEmpty()) {
            throw new EntityNotFoundException(id);
        } else {
            try {
                FileDTO fileDTO =file.get();
                String name = fileDTO.getExternalId() + "/" +fileDTO.getFilename();
                updateSize(name, fileDTO);
            } catch (EntityNotFoundException | MethodArgumentNotValidException | NoSuchKeyException e) {
                log.error(e.getMessage());
            }
        }

        Optional<FilePointer> filePointer = file
            .map(storageService::findFile)
            .orElse(null);

        if (filePointer.isEmpty()) {
            throw new FileNotFoundException();
        }
        return Pair.of(file.get(), filePointer.get());
    }


    public ResponseEntity<Resource> download(String id) throws EntityNotFoundException
    {

        Pair<FileDTO, FilePointer> pair = fetch(id);
        Optional<FilePointer> filePointer = Optional.of(pair.getSecond());

        Resource resource = filePointer.map(storageService::prepareResponse)
            .orElseGet(storageService::notFound);
        return response(filePointer.get(), HttpStatus.OK, resource, pair.getFirst());
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

    public void updateSize(String name, FileDTO dto) throws EntityNotFoundException, MethodArgumentNotValidException, NoSuchKeyException
    {
        Long size = storageService.retrieveContentLength(name);
        dto.setSize(size);
        update(dto.getId(), dto);
    }

    private MediaType toMediaType(String input) {
        return MediaType.valueOf(input);
    }


}
