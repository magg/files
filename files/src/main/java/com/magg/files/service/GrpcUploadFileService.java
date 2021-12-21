package com.magg.files.service;

import com.google.protobuf.ByteString;
import com.magg.api.dto.FileDTO;
import io.grpc.examples.filedownload.FileUploadRequest;
import io.grpc.examples.filedownload.FileUploadServiceGrpc;
import io.grpc.examples.filedownload.UploadStatus;
import io.grpc.examples.filedownload.UploadStatusCode;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.io.FileUtils;

@GrpcService
@Slf4j
public class GrpcUploadFileService extends FileUploadServiceGrpc.FileUploadServiceImplBase
{

    private final FileService fileService;
    private static final String BASE_PATH = "/tmp/output";
    private static final Path SERVER_BASE_PATH = Paths.get(BASE_PATH);
    private static final String SLASH = "/";


    public GrpcUploadFileService(FileService fileService)
    {
        this.fileService = fileService;
    }


    @Override
    public StreamObserver<FileUploadRequest> upload(StreamObserver<UploadStatus> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            // upload context variables
            OutputStream writer;
            UploadStatusCode status = UploadStatusCode.In_Progress;
            String message = "";
            StringBuilder filePath = new StringBuilder();
            String contentType;
            String fileName;
            FileDTO fileDTO;

            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {

                try{
                    if(fileUploadRequest.hasMetadata()){
                        String id = UUID.randomUUID().toString();
                        filePath.append(SLASH);
                        filePath.append(id);
                        filePath.append(SLASH);
                        writer = getFilePath(fileUploadRequest,filePath);
                        contentType = fileUploadRequest.getMetadata().getType();
                        fileName = fileUploadRequest.getMetadata().getName();
                    } else{
                        writeFile(writer, fileUploadRequest.getFile().getData());
                    }
                } catch (IOException e){
                    this.onError(e);
                }

            }

            @Override
            public void onError(Throwable throwable) {
                status = UploadStatusCode.Failed;
                log.error(throwable.getMessage());
                this.onCompleted();
            }

            @SneakyThrows
            @Override
            public void onCompleted() {
                closeFile(writer);
                status = UploadStatusCode.In_Progress.equals(status) ? UploadStatusCode.Ok : status;
                if (status.equals(UploadStatusCode.Failed)) {
                    message = "failed to send";
                }

                if (status.equals(UploadStatusCode.Ok)) {
                    message = "Upload received with success";

                    File initialFile = new File(BASE_PATH+filePath.toString());
                    InputStream targetStream = FileUtils.openInputStream(initialFile);
                    fileDTO = fileService.create(targetStream,contentType, fileName, BASE_PATH+filePath.toString());

                }

                log.info("sending response");
                UploadStatus response = UploadStatus.newBuilder()
                    .setCode(status)
                    .setMessage(message)
                    .setFileId(fileDTO.getId())
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    private OutputStream getFilePath(FileUploadRequest request, StringBuilder path) throws IOException {

        Files.createDirectories(SERVER_BASE_PATH);
        Path newPath = Paths.get(BASE_PATH+path);
        Files.createDirectories(newPath);
        var fileName = request.getMetadata().getName();
        path.append(SLASH);
        path.append(fileName);
        return Files.newOutputStream(newPath.resolve(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void writeFile(OutputStream writer, ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    private void closeFile(OutputStream writer){
        try {
            writer.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
