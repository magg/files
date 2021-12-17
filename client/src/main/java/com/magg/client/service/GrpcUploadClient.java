package com.magg.client.service;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.filedownload.Chunk;
import io.grpc.examples.filedownload.FileUploadRequest;
import io.grpc.examples.filedownload.FileUploadServiceGrpc;
import io.grpc.examples.filedownload.MetaData;
import io.grpc.examples.filedownload.UploadStatus;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrpcUploadClient
{

    final CountDownLatch finishLatch = new CountDownLatch(1);
    final AtomicBoolean completed = new AtomicBoolean(false);

    private final ManagedChannel channel;
    private FileUploadServiceGrpc.FileUploadServiceStub fileServiceStub;

    private final FileUploadServiceGrpc.FileUploadServiceBlockingStub blockingStub;

    public GrpcUploadClient(String hostname, int port){
        this(ManagedChannelBuilder.forAddress(hostname, port)
            .usePlaintext()
            .keepAliveTime(150, TimeUnit.SECONDS)
            .build());
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(20, TimeUnit.SECONDS);
    }

    /** Construct client for accessing FileDownload server using the existing channel. */
    GrpcUploadClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = FileUploadServiceGrpc.newBlockingStub(channel);
        fileServiceStub = FileUploadServiceGrpc.newStub(channel);
    }


    public void upload(String filePath) {

        // request observer
        StreamObserver<UploadStatus> responseObserver = new FileUploadObserver();


        StreamObserver<FileUploadRequest> requestObserver = this.fileServiceStub.upload(responseObserver);

        // input file for testing
        Path path = Paths.get(filePath);
        try
        {

            String mimeType = Files.probeContentType(path);
            // build metadata
            FileUploadRequest metadata = FileUploadRequest.newBuilder()
                .setMetadata(MetaData.newBuilder()
                    .setName(path.getFileName().toString())
                    .setType(mimeType).build())
                .build();
            requestObserver.onNext(metadata);

            int PART_SIZE = 10*1024*1024;
            // upload file as chunk
            //InputStream inputStream = Files.newInputStream(path);
            byte[] bytes = new byte[PART_SIZE];

            /*
            int size;
            while ((size = inputStream.read(bytes)) > 0)
            {
                FileUploadRequest uploadRequest = FileUploadRequest.newBuilder()
                    .setFile(Chunk.newBuilder().setData(ByteString.copyFrom(bytes, 0, size)).build())
                    .build();
                streamObserver.onNext(uploadRequest);
            }

             */

            int bytesRead;
            byte[] toWrite;
            int partNumber = 0;

                final InputStream inputStream = Files.newInputStream(path);
                while ((bytesRead = inputStream.read(bytes)) != -1) {
                    System.out.println("Processing part : "+ partNumber);
                    //part = file.resolveSibling(filenameBase + ".part" + partNumber);
                    toWrite = bytesRead == PART_SIZE ? bytes : Arrays.copyOf(bytes, bytesRead);
                    FileUploadRequest uploadRequest = FileUploadRequest.newBuilder()
                        .setFile(Chunk.newBuilder().setData(ByteString.copyFrom(toWrite)).build())
                        .build();
                    requestObserver.onNext(uploadRequest);
                    partNumber++;
                }



            // close the stream
                inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            requestObserver.onCompleted();
        }

    }

}
