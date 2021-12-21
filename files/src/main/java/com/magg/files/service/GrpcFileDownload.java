package com.magg.files.service;

import com.google.protobuf.ByteString;
import com.magg.api.dto.FileDTO;
import com.magg.api.exception.EntityNotFoundException;
import com.magg.storage.FilePointer;
import io.grpc.examples.filedownload.Chunk;
import io.grpc.examples.filedownload.FileDownloadGrpc;
import io.grpc.examples.filedownload.FileDownloadRequest;
import io.grpc.examples.filedownload.FileDownloadResponse;
import io.grpc.examples.filedownload.MetaData;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.util.Pair;

@Slf4j
@GrpcService
public class GrpcFileDownload extends FileDownloadGrpc.FileDownloadImplBase
{

    private final FileService fileService;

    public GrpcFileDownload(FileService fileService)
    {
        this.fileService = fileService;
    }

    @Override
    public void download(FileDownloadRequest request, StreamObserver<FileDownloadResponse> responseObserver) {
        try {
            Pair<FileDTO, FilePointer> pair = fileService.fetch(request.getId());
            log.info("entre");
            MetaData metaData = MetaData
                .newBuilder()
                .setName(pair.getFirst().getFilename())
                .setType(pair.getFirst().getContentType())
                .build();
            responseObserver.onNext(
                FileDownloadResponse.newBuilder().setMetadata(metaData).build()
            );
            try (
                 InputStream bis = pair.getSecond().open()) {
                int bufferSize = 256 * 1024;// 256k
                byte[] buffer = new byte[bufferSize];
                int length;
                while ((length = bis.read(buffer, 0, bufferSize)) != -1) {
                    Chunk chunk = Chunk.newBuilder().setData(ByteString.copyFrom(buffer, 0, length)).build();
                    responseObserver.onNext(
                        FileDownloadResponse.newBuilder().setFile(chunk).build()
                    );
                }
                responseObserver.onCompleted();
            }
        } catch (IOException | EntityNotFoundException e)
        {
            log.error(e.getMessage());
        }
    }
}
