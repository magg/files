package com.magg.files.service;

import com.google.protobuf.ByteString;
import io.grpc.examples.filedownload.Chunk;
import io.grpc.examples.filedownload.FileDownloadGrpc;
import io.grpc.examples.filedownload.FileDownloadRequest;
import io.grpc.stub.StreamObserver;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GrpcFileDownload extends FileDownloadGrpc.FileDownloadImplBase
{
    @Override
    public void download(FileDownloadRequest request, StreamObserver<Chunk> responseObserver) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("tmp_file_for_demo_purposes", ".txt");
            try (FileWriter fw = new FileWriter(tmpFile);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                int numBytes = 0;
                while (numBytes < 256 * 1024 * 4) { // 1024k
                    bw.write(request.getUrl());
                    bw.write("\n");
                    numBytes += request.getUrl().length();
                }
            }
            try (FileInputStream fis = new FileInputStream(tmpFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                int bufferSize = 256 * 1024;// 256k
                byte[] buffer = new byte[bufferSize];
                int length;
                while ((length = bis.read(buffer, 0, bufferSize)) != -1) {
                    responseObserver.onNext(
                        Chunk.newBuilder().setData(ByteString.copyFrom(buffer, 0, length)).build()
                    );
                }
                responseObserver.onCompleted();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
}
