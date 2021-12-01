package com.example.file.controller;

import com.example.file.service.StorageService;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController
@RequestMapping("/download")
public class FileDownloadController
{

    private final StorageService storageService;


    public FileDownloadController(StorageService storageService)
    {
        this.storageService = storageService;
    }


    @RequestMapping(method = GET, value = "/{uuid}")
    public Resource download(@PathVariable UUID uuid) {
        return storageService
            .findFile(uuid)
            .map(storageService::prepareResponse)
            .orElseGet(storageService::notFound);
    }
}
