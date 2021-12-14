package com.magg.files.controller;

import com.magg.api.exception.EntityNotFoundException;
import com.magg.files.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/download")
public class FileDownloadController
{

    private final FileService fileService;


    public FileDownloadController(FileService fileService)
    {
        this.fileService = fileService;
    }


    @RequestMapping(method = GET, value = "/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws EntityNotFoundException
    {
        return fileService.download(id);
    }
}
