package com.magg.client.service;

import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.filedownload.Chunk;
import io.grpc.examples.filedownload.FileDownloadGrpc;
import io.grpc.examples.filedownload.FileDownloadRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrcpDownloadClient
{
    private final ManagedChannel channel;
    private final FileDownloadGrpc.FileDownloadBlockingStub blockingStub;

    /** Construct client connecting to FileDownload server at {@code host:port}. */
    public GrcpDownloadClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build());
    }

    /** Construct client for accessing FileDownload server using the existing channel. */
    GrcpDownloadClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = FileDownloadGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Download file from the server. */
    public void downloadFile(String url) throws Exception {
        log.info("Will try to downloadFile " + url + " ...");
        FileDownloadRequest request = FileDownloadRequest.newBuilder().setUrl(url).build();
        File localTmpFile = File.createTempFile("localcopy", ".txt");

        ByteSink byteSink = Files.asByteSink(localTmpFile, FileWriteMode.APPEND);
        try {
            Iterator<Chunk> response;
            try {
                response = blockingStub.download(request);
                while (response.hasNext()) {
                    byteSink.write(response.next().getData().toByteArray());
                }
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {}", e.getStatus());
                return;
            }

            try (FileReader fr = new FileReader(localTmpFile);
                 BufferedReader br = new BufferedReader(fr)) {
                String nextLine;
                while ((nextLine = br.readLine()) != null) {
                    log.info("File line: " + nextLine);
                }
            }

        } finally {
            localTmpFile.delete();
        }
    }

}
