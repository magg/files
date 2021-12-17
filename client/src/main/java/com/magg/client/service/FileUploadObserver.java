package com.magg.client.service;

import io.grpc.examples.filedownload.UploadStatus;
import io.grpc.stub.StreamObserver;

public class FileUploadObserver implements StreamObserver<UploadStatus>
{

    @Override
    public void onNext(UploadStatus fileUploadResponse) {
        System.out.println(
            "File upload status :: " + fileUploadResponse.getCode()
        );
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(
            throwable.getMessage()
        );
    }

    @Override
    public void onCompleted() {
        System.out.println("complete");
    }

}
