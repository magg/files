package com.magg.client.service;

import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.filedownload.FileDownloadGrpc;
import io.grpc.examples.filedownload.FileDownloadRequest;
import io.grpc.examples.filedownload.FileDownloadResponse;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrcpDownloadClient
{
    private final ManagedChannel channel;
    private final FileDownloadGrpc.FileDownloadBlockingStub blockingStub;
    final CountDownLatch finishLatch = new CountDownLatch(1);
    private final String downloadPath;

    /** Construct client connecting to FileDownload server at {@code host:port}. */
    public GrcpDownloadClient(String host, int port, String downloadPath) {
        this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build(), downloadPath);
    }

    /** Construct client for accessing FileDownload server using the existing channel. */
    GrcpDownloadClient(ManagedChannel channel, String downloadPath) {
        this.channel = channel;
        this.downloadPath = downloadPath;
        blockingStub = FileDownloadGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(15, TimeUnit.MINUTES);
    }

    /** Download file from the server. */
    public String downloadFile(String url) throws Exception {
        log.info("Will try to downloadFile " + url + " ...");
        FileDownloadRequest request = FileDownloadRequest.newBuilder().setId(url).build();
        File localTmpFile =  new File(downloadPath+url);

        String newName = null;

        ByteSink byteSink = Files.asByteSink(localTmpFile, FileWriteMode.APPEND);
        try {
            Iterator<FileDownloadResponse> response;

            try {
                response = blockingStub.download(request);

                while (response.hasNext()) {

                    FileDownloadResponse next = response.next();

                    if (next.hasMetadata()) {
                        newName = next.getMetadata().getName();
                    } else {
                        byteSink.write(next.getFile().getData().toByteArray());
                    }
                }
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {}", e.getStatus());
                finishLatch.countDown();
                return null;
            }

            finishLatch.countDown();

        } finally {
            //localTmpFile.delete();
            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                log.warn("bidirectionalStreaming can not finish within 1 minute");
            }
        }

        if (newName != null) {
            File renamedFile =  new File(downloadPath+newName);
            // Rename file (or directory)
            boolean success = localTmpFile.renameTo(renamedFile);
            if (!success) {
                log.error("File was not successfully renamed");
            } else {
                return downloadPath+newName;
            }
        }

        return downloadPath+url;
    }

}
