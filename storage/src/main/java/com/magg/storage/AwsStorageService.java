package com.magg.storage;

import com.magg.api.dto.FileDTO;
import com.magg.api.exception.StorageException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.SizeConstant;
import software.amazon.awssdk.transfer.s3.Upload;

import static software.amazon.awssdk.transfer.s3.SizeConstant.MB;

/**
 * AWS S3 Abstraction to upload, get, delete files from an specific bucket.
 */

@Slf4j
@Component
public class AwsStorageService implements StorageService
{

    private S3Client s3Client;
    private S3TransferManager transferManager;
    private final ExecutorService executor;

    @Value("${aws.bucket.name}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;


    public AwsStorageService(ExecutorService executor)
    {
        this.executor = executor;
    }


    /**
     * Init service client.
     */
    @PostConstruct
    private void initialize()
    {
        log.debug("Initializing Amazon S3 client.");
        s3Client = getAmazonS3Client();
        transferManager = s3TransferManager();

    }


    private S3Client getAmazonS3Client()
    {
        return S3Client
            .builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();
    }

    private S3TransferManager s3TransferManager() {
        return S3TransferManager.builder()
                .s3ClientConfiguration(cfg -> cfg.credentialsProvider(ProfileCredentialsProvider.create())
                    .region(Region.of(region))
                    .targetThroughputInGbps(20.0)
                    .minimumPartSizeInBytes(10 * MB))
                .build();
    }


    /**
     * {@inheritDoc}
     */
    public String createAsset(File file, AssetType assetType) {

        try {
            PutObjectResponse putObjectResult = s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(assetType.getPrefix() + file.getName())
                    .contentType(Files.probeContentType(file.toPath()))
                    .contentLength(file.length())
                    .build(),
                RequestBody.fromFile(file));
            final URL reportUrl = s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(file.getName()).build());
            log.debug("Object {} put to S3 and accessible by URL {}", putObjectResult, reportUrl);
            return reportUrl.toString();
        } catch (SdkException ex) {
            log.error("Unexpected error putting file in S3 StorageService", ex);
            throw new StorageException("Unexpected error putting file in S3 StorageService", ex);
        } catch (IOException ex) {
            log.error("Unexpected error getting content-type from file", ex);
            throw new StorageException("Unexpected error getting content-type from file", ex);
        }
    }

    @Override
    public ResponseInputStream<GetObjectResponse> downloadAsset(String name, AssetType assetType) {
        ResponseInputStream<GetObjectResponse> fullObject = null;
        try {
            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(assetType.getPrefix() + name)
                .build());

        } catch (SdkServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            log.error("server error {}", e.getMessage());
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            log.error("client error {}", e.getMessage());
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
        }
        return fullObject;
    }


    @Override
    public Optional<FilePointer> findFile(FileDTO fileDTO) {
        String uuid = fileDTO.getExternalId() + "/" + fileDTO.getFilename();

        log.debug("Downloading {}", uuid);
        ResponseInputStream<GetObjectResponse> file = downloadAsset(uuid, AssetType.TEST);
        final S3FilePointer pointer = new S3FilePointer(file, fileDTO.getFilename(), fileDTO.getContentType());
        return Optional.of(pointer);
    }


    public void upload(InputStream in, AssetType assetType, String contentType, String name) {
        try {

            Long contentLength = (long) in.available();

            byte[] fileByteArray = in.readAllBytes();

            Upload upload = transferManager.upload(b -> b.requestBody(AsyncRequestBody.fromBytes(fileByteArray))
                .putObjectRequest(req -> req
                    .bucket(bucketName)
                    .key(assetType.getPrefix() + name)
                    .contentType(contentType)
                    .contentLength(contentLength)
                )
            );

            upload.completionFuture().join();

       } catch (SdkClientException | IOException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }
    @Override
    public Long retrieveContentLength(String name) {

        try {

            return s3Client.headObject(HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(AssetType.TEST.getPrefix()+name)
            .build()).contentLength();

        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void upload2(InputStream in, AssetType assetType, String contentType, String name)
    {

        multiPartUpload(AssetType.TEST.getPrefix()+name, in, contentType);

    }


    @Override
    public void upload3(InputStream in, AssetType assetType, String contentType, String name, String id)
    {
        multiPartUpload3(AssetType.TEST.getPrefix()+name, in, contentType, id);

    }


    public void uploadPartAsync(ByteArrayInputStream inputStream, String filename, String uploadId, int partNumber) {
        submitTaskForUploading(inputStream, false, filename, uploadId, partNumber);
    }

    public void uploadFinalPartAsync(ByteArrayInputStream inputStream,
        String filename,
        String uploadId,
        int partNumber)
    {
        try
        {
            submitTaskForUploading(inputStream, true,filename, uploadId, partNumber);

            // wait and get all PartETags from ExecutorService and submit it in CompleteMultipartUploadRequest
            List<CompletedPart> partETags = new ArrayList<>();

            /*
            for (Future<CompletedPart> partETagFuture : futuresPartETags)
            {
                partETags.add(partETagFuture.get());
            }

             */

            // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
            // parts and finish the multipart operation.
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(partETags)
                .build();

            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();


            s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    private void submitTaskForUploading(ByteArrayInputStream inputStream,
        boolean isFinalPart,
        String filename,
        String uploadId,
        int partNumber) {


        submitTaskToExecutorService(() -> {
            UploadPartRequest uploadRequest = UploadPartRequest
                .builder()
                .bucket(bucketName)
                .key(filename)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();


            log.info(String.format("Submitting uploadPartId: %d of partSize: %d", partNumber, inputStream.available()));

            String etag1 = s3Client.uploadPart(uploadRequest, RequestBody.fromInputStream(inputStream, inputStream.available())).eTag();

            CompletedPart part1 = CompletedPart.builder().partNumber(1).eTag(etag1).build();

            log.info(String.format("Successfully submitted uploadPartId: %d", part1.partNumber()));
            return part1;
        });
    }

    private void submitTaskToExecutorService(Callable<CompletedPart> callable) {

        final List<Future<CompletedPart>> futuresPartETags = new ArrayList<>();

        // we are submitting each part in executor service and it does not matter which part gets upload first
        // because in each part we have assigned PartNumber from "uploadPartId.incrementAndGet()"
        // and S3 will accumulate file by using PartNumber order after CompleteMultipartUploadRequest
        Future<CompletedPart> partETagFuture = this.executor.submit(callable);
        futuresPartETags.add(partETagFuture);
    }



    public void multiPartUpload(String key, InputStream inputStream, String contentType) {
        // First create a multipart upload and get the upload id
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();
        System.out.println(uploadId);

        List<CompletableFuture<CompletedPart>> futures = new ArrayList<>();

        final int UPLOAD_PART_SIZE = 10 * (int) SizeConstant.MB; // Part Size should not be less than 5 MB while using MultipartUpload

        try {

            int bytesRead, bytesAdded = 0;
            byte[] data = new byte[UPLOAD_PART_SIZE];
            //ByteArrayOutputStream bufferOutputStream = new ByteArrayOutputStream();
            int part = 1;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {


                //bufferOutputStream.write(data, 0, bytesRead);
                if (bytesAdded < UPLOAD_PART_SIZE) {
                    // continue writing to same output stream unless it's size gets more than UPLOAD_PART_SIZE
                    bytesAdded += bytesRead;
                    continue;
                }

                log.info(String.format("Submitting uploadPartId: %s", printMb(bytesAdded)));

                //multipartUpload.uploadPartAsync(new ByteArrayInputStream(bufferOutputStream.toByteArray()));

                int finalPart = part;
                int finalBytesAdded = bytesAdded;
                CompletableFuture<CompletedPart> partCompletableFuture = CompletableFuture.supplyAsync(() ->
                        uploadRequest(
                            new ByteArrayInputStream(data, 0, finalBytesAdded),
                            false,
                            key,
                            uploadId,
                            finalPart
                        )
                    );
                futures.add(partCompletableFuture);

                part++;
                //bufferOutputStream.reset(); // flush the bufferOutputStream
                bytesAdded = 0; // reset the bytes added to 0
            }

            // upload remaining part of output stream as final part
            // bufferOutputStream size can be less than 5 MB as it is the last part of upload
            //multipartUpload.uploadFinalPartAsync(new ByteArrayInputStream(bufferOutputStream.toByteArray()));

            int finalPart = part;
            int finalBytesAdded1 = bytesAdded;
            CompletableFuture<CompletedPart> partCompletableFuture = CompletableFuture.supplyAsync(() ->
                uploadRequest(
                    new ByteArrayInputStream(data, 0, finalBytesAdded1),
                    false,
                    key,
                    uploadId,
                    finalPart)
            );
            futures.add(partCompletableFuture);

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<CompletedPart> allParts = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        allParts.sort(Comparator.comparing(CompletedPart::partNumber));

        // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
        // parts and finish the multipart operation.
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(allParts)
            .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
            CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        s3Client.completeMultipartUpload(completeMultipartUploadRequest);

    }

    private CompletedPart uploadRequest(ByteArrayInputStream inputStream,
        boolean isFinalPart,
        String filename,
        String uploadId,
        int partNumber) {


        UploadPartRequest uploadRequest = UploadPartRequest
            .builder()
            .bucket(bucketName)
            .key(filename)
            .uploadId(uploadId)
            .partNumber(partNumber)
            .build();

        String etag1 = s3Client.uploadPart(uploadRequest, RequestBody.fromInputStream(inputStream, inputStream.available())).eTag();

        CompletedPart part = CompletedPart.builder()
            .partNumber(partNumber)
            .eTag(etag1)
            .build();

        log.info(String.format("Successfully submitted uploadPartId, partnumber: %d", part.partNumber()));
        return part;
    }

    private CompletedPart uploadRequest(FileInputStream inputStream,
        boolean isFinalPart,
        String filename,
        String uploadId,
        int partNumber) {


        UploadPartRequest uploadRequest = UploadPartRequest
            .builder()
            .bucket(bucketName)
            .key(filename)
            .uploadId(uploadId)
            .partNumber(partNumber)
            .build();

        String etag1 = null;
        try
        {
            etag1 = s3Client.uploadPart(uploadRequest, RequestBody.fromInputStream(inputStream, inputStream.available())).eTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        CompletedPart part = CompletedPart.builder()
            .partNumber(partNumber)
            .eTag(etag1)
            .build();

        log.info(String.format("Successfully submitted uploadPartId, partnumber: %d", part.partNumber()));
        return part;
    }


    private String printMb(int size) {
        String cnt_size;

        double size_kb = (double) size / 1024;
        double size_mb = size_kb / 1024;


        int result = size / (int) SizeConstant.MB;

      if (size_mb > 0){
            cnt_size = result + " MB";
        }else{
            cnt_size = size_kb + " KB";
        }
        return cnt_size;
    }

    public void multiPartUpload2(String key, InputStream inputStream, String contentType) {
        // First create a multipart upload and get the upload id
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();
        System.out.println(uploadId);

        List<CompletableFuture<CompletedPart>> futures = new ArrayList<>();

        final int UPLOAD_PART_SIZE = 10 * (int) SizeConstant.MB; // Part Size should not be less than 5 MB while using MultipartUpload

        try {

            int bytesRead, bytesAdded = 0;
            byte[] data = new byte[UPLOAD_PART_SIZE];
            ByteArrayOutputStream bufferOutputStream = new ByteArrayOutputStream();
            int part = 1;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {

                bufferOutputStream.write(data, 0, bytesRead);

                if (bytesAdded < UPLOAD_PART_SIZE) {
                    // continue writing to same output stream unless it's size gets more than UPLOAD_PART_SIZE
                    bytesAdded += bytesRead;
                    continue;
                }

                log.info(String.format("Submitting uploadPartId: %s", printMb(bytesAdded)));

                //multipartUpload.uploadPartAsync(new ByteArrayInputStream(bufferOutputStream.toByteArray()));

                int finalPart = part;
                int finalBytesAdded = bytesAdded;
                CompletableFuture<CompletedPart> partCompletableFuture = CompletableFuture.supplyAsync(() ->
                    uploadRequest(
                        new ByteArrayInputStream(data, 0, finalBytesAdded),
                        false,
                        key,
                        uploadId,
                        finalPart
                    )
                );
                futures.add(partCompletableFuture);

                part++;
                bufferOutputStream.reset(); // flush the bufferOutputStream
                bytesAdded = 0; // reset the bytes added to 0
            }

            // upload remaining part of output stream as final part
            // bufferOutputStream size can be less than 5 MB as it is the last part of upload
            //multipartUpload.uploadFinalPartAsync(new ByteArrayInputStream(bufferOutputStream.toByteArray()));

            int finalPart = part;
            int finalBytesAdded = bytesAdded;
            CompletableFuture<CompletedPart> partCompletableFuture = CompletableFuture.supplyAsync(() ->
                uploadRequest(
                    new ByteArrayInputStream(data, 0, finalBytesAdded),
                    false,
                    key,
                    uploadId,
                    finalPart
                )
            );
            futures.add(partCompletableFuture);

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<CompletedPart> allParts = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        allParts.sort(Comparator.comparing(CompletedPart::partNumber));

        // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
        // parts and finish the multipart operation.
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(allParts)
            .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
            CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        s3Client.completeMultipartUpload(completeMultipartUploadRequest);

    }


    public void multiPartUpload3(String key, InputStream inputStream, String contentType, String id)
    {
        // First create a multipart upload and get the upload id
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();
        System.out.println(uploadId);

        List<CompletableFuture<CompletedPart>> futures = new ArrayList<>();

        final int UPLOAD_PART_SIZE = 10 * (int) SizeConstant.MB; // Part Size should not be less than 5 MB while using MultipartUpload

        try
        {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long written = IOUtils.copyLarge(inputStream, baos, 0, UPLOAD_PART_SIZE);

            byte[] data = baos.toByteArray();

            int partNumber = 1;
            int firstByte = 0;
            Boolean isFirstChunck = true;

            InputStream firstChunck = new ByteArrayInputStream(data);
            PushbackInputStream chunckableInputStream = new PushbackInputStream(inputStream, 1);

            while (-1 != (firstByte = chunckableInputStream.read()))
            {
                long partSize = 0;
                chunckableInputStream.unread(firstByte);
                File tempFile = File.createTempFile(id.concat("-part").concat(String.valueOf(partNumber)), "tmp");
                tempFile.deleteOnExit();
                OutputStream os = null;
                try
                {
                    os = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));

                    if (isFirstChunck)
                    {
                        partSize = IOUtils.copyLarge(firstChunck, os, 0, (UPLOAD_PART_SIZE));
                        isFirstChunck = false;
                    }
                    else
                    {
                        partSize = IOUtils.copyLarge(chunckableInputStream, os, 0, (UPLOAD_PART_SIZE));
                    }
                    written += partSize;

                    /*
                    if(written> maxSizeBytes){
                        overSizeLimit = true;
                        logger.error( "OVERSIZED FILE ({}). STARTING ABORT", written );
                        break;
                        //set flag here and break out of loop to run abort
                    }

                     */
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    IOUtils.closeQuietly(os);
                }

                FileInputStream chunk = new FileInputStream(tempFile);

                Boolean isLastPart = -1 == (firstByte = chunckableInputStream.read());
                if(!isLastPart)
                    chunckableInputStream.unread(firstByte);


                int finalPart = partNumber;;
                CompletableFuture<CompletedPart> partCompletableFuture = CompletableFuture.supplyAsync(() ->
                    uploadRequest(
                        chunk,
                        false,
                        key,
                        uploadId,
                        finalPart
                    )
                );
                futures.add(partCompletableFuture);


                partNumber++;
            }

            List<CompletedPart> allParts = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            allParts.sort(Comparator.comparing(CompletedPart::partNumber));

            // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
            // parts and finish the multipart operation.
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(allParts)
                .build();

            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();

            s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }
    }

}
