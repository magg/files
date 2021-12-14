package com.magg.files;

import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinioIntegrationTest {

    private static final DockerComposeContainer minioContainer = new DockerComposeContainer<>(new File("src/test/resources/docker-compose.yml"))
        .withExposedService("minio-service", 9000);
    private static final String MINIO_ENDPOINT = "http://localhost:9000";
    private static final String ACCESS_KEY = "minio";
    private static final String SECRET_KEY = "minio123";
    private S3Client s3Client;

    @BeforeAll
    void setupMinio() {
        minioContainer.start();
        initializeS3Client();
    }

    @AfterAll
    void closeMinio() {
        minioContainer.close();
    }

    private void initializeS3Client() {
        String name = Region.US_EAST_1.toString();
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        s3Client = S3Client.builder().region(Region.of(name))
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .endpointOverride(URI.create(MINIO_ENDPOINT))
            .build();
    }

    @Test
    void shouldReturnActualContentBasedOnBucketName() throws Exception{
        String bucketName = "test-bucket";
        String key = "s3-test";
        String content = "Minio Integration test";
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
            .bucket(bucketName)
            .build();

        createBucket(bucketName);


        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(),RequestBody.fromString(content));

        ResponseInputStream<GetObjectResponse> fullObject = null;
        byte[] actualContent = new byte[22];

        fullObject = s3Client.getObject(GetObjectRequest
            .builder()
            .bucket(bucketName)
            .key(key)
            .build());

        fullObject.read(actualContent);

        Assertions.assertEquals(content, new String(actualContent));
    }


    private void createBucket(String bucketName) {

            try {
                S3Waiter s3Waiter = s3Client.waiter();
                CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

                s3Client.createBucket(bucketRequest);
                HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();


                // Wait until the bucket is created and print out the response
                WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
                waiterResponse.matched().response().ifPresent(System.out::println);
                System.out.println(bucketName +" is ready");

            } catch (S3Exception e) {
                System.err.println(e.awsErrorDetails().errorMessage());
                System.exit(1);
            }
        }

}