package com.magg.files.controller;

import com.magg.api.dto.FileDTO;
import com.magg.files.model.Response;
import com.magg.files.service.FileService;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileUploadController
{


    private final FileService fileService;


    public FileUploadController(FileService fileService)
    {
        this.fileService = fileService;
    }


    @RequestMapping(value="/upload", method= RequestMethod.POST)
    public @ResponseBody Response<FileDTO> upload(HttpServletRequest request) {
        FileDTO fileDTO = null;
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                // Inform user about invalid request
                return new Response<>(false, "Not a multipart request.", fileDTO);
            }

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload();

            // Parse the request
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (!item.isFormField()) {
                    String filename = item.getName();
                    // Process the input stream
                    //OutputStream out = new FileOutputStream(filename);
                   // IOUtils.copy(stream, out);
                    String contentType = item.getContentType();
                    fileDTO = fileService.create(stream, contentType, filename, null);
                    stream.close();
                   // out.close();
                }
            }
        } catch (FileUploadException e) {
            return new Response<>(false, "File upload error", fileDTO);
        } catch (IOException e) {
            return new Response<>(false, "Internal server IO error", fileDTO);
        }

        return new Response<>(true, "Success", fileDTO);
    }
}
