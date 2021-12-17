package com.magg.files.service;

import com.google.protobuf.ByteString;
import io.grpc.examples.filedownload.FileUploadRequest;
import io.grpc.examples.filedownload.FileUploadServiceGrpc;
import io.grpc.examples.filedownload.UploadStatus;
import io.grpc.examples.filedownload.UploadStatusCode;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GrpcUploadFileService extends FileUploadServiceGrpc.FileUploadServiceImplBase
{
    private static final Path SERVER_BASE_PATH = Paths.get("/tmp/output");


    @Override
    public StreamObserver<FileUploadRequest> upload(StreamObserver<UploadStatus> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            // upload context variables
            OutputStream writer;
            UploadStatusCode status = UploadStatusCode.In_Progress;
            String message = "";
            int part=0;

            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                System.out.println(part);

                try{
                    if(fileUploadRequest.hasMetadata()){
                        writer = getFilePath(fileUploadRequest);
                    } else{
                        writeFile(writer, fileUploadRequest.getFile().getData());
                    }
                } catch (IOException e){
                    this.onError(e);
                }


                part++;
            }

            @Override
            public void onError(Throwable throwable) {
                status = UploadStatusCode.Failed;
                System.out.println(throwable.getMessage());
                this.onCompleted();
            }

            @Override
            public void onCompleted() {
                //closeFile(writer);
                status = UploadStatusCode.In_Progress.equals(status) ? UploadStatusCode.Ok : status;
                if (status.equals(UploadStatusCode.Failed)) {
                    message = "failed to send";
                }

                if (status.equals(UploadStatusCode.Ok)) {
                    message = "Upload received with success";
                }

                UploadStatus response = UploadStatus.newBuilder()
                    .setCode(status)
                    .setMessage(message)
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    private OutputStream getFilePath(FileUploadRequest request) throws IOException {

        Files.createDirectories(SERVER_BASE_PATH);  // No need for your null check, so I removed it; based on `fileName`, it will always have a parent
        var fileName = request.getMetadata().getName() ;
        return Files.newOutputStream(SERVER_BASE_PATH.resolve(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void writeFile(OutputStream writer, ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    private void closeFile(OutputStream writer){
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
