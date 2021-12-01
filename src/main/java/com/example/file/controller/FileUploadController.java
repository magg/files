package com.example.file.controller;

import com.example.file.model.Response;
import com.example.file.service.AssetType;
import com.example.file.service.StorageService;
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

    private final StorageService storageService;


    public FileUploadController(StorageService storageService)
    {
        this.storageService = storageService;
    }


    @RequestMapping(value="/upload", method= RequestMethod.POST)
    public @ResponseBody Response<String> upload(HttpServletRequest request) {
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                // Inform user about invalid request
                return new Response<>(false, "Not a multipart request.", "");
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
                    storageService.upload(stream, AssetType.TEST, item.getContentType(), filename );
                    stream.close();
                   // out.close();
                }
            }
        } catch (FileUploadException e) {
            return new Response<>(false, "File upload error", e.toString());
        } catch (IOException e) {
            return new Response<>(false, "Internal server IO error", e.toString());
        }

        return new Response<>(true, "Success", "");
    }
}
