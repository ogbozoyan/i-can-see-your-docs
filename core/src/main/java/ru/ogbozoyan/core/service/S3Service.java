package ru.ogbozoyan.core.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ogbozoyan.core.configuration.s3.S3Properties;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;
    private final S3Presigner minioS3Presigner;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties, S3Presigner minioS3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
        this.minioS3Presigner = minioS3Presigner;
    }

    @SneakyThrows
    public S3CustomResponse uploadFile(UUID uuid, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String keyAbsolute = uuid + "/" + originalFilename;
        String key = uuid + "/";

        ensureBucket();

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(properties.getS3Bucket())
            .key(keyAbsolute)
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        return new S3CustomResponse(uuid.toString(), originalFilename, key);
    }

    @SneakyThrows
    public S3CustomResponse uploadFile(UUID uuid, ByteArrayResource baos, String originalFilename) {

        String keyAbsolute = uuid + "/" + originalFilename;
        String key = uuid + "/";

        ensureBucket();

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(properties.getS3Bucket())
            .key(keyAbsolute)
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(baos.getContentAsByteArray()));

        return new S3CustomResponse(uuid.toString(), originalFilename, key);
    }


    public byte[] downloadFile(UUID uuid, String filename) {
        String keyAbsolute = uuid + "/" + filename;
        return downloadFile(keyAbsolute);
    }

    @SneakyThrows
    public byte[] downloadFile(String absolutePath) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(properties.getS3Bucket())
            .key(absolutePath)
            .build();

        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getRequest)) {
            return inputStream.readAllBytes();
        }
    }

    public String getPresignedUrl(UUID uuid, String originalFilename) {
        String key = uuid + "/" + originalFilename;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(properties.getS3Bucket())
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30))
            .getObjectRequest(getObjectRequest)
            .build();

        return minioS3Presigner.presignGetObject(presignRequest).url().toString();
    }
    public String getPresignedUrl(String filePath) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(properties.getS3Bucket())
            .key(filePath)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(30))
            .getObjectRequest(getObjectRequest)
            .build();

        return minioS3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public String resolveFilenameFromS3(UUID uuid) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(properties.getS3Bucket())
            .prefix(uuid.toString() + "/")
            .maxKeys(1)
            .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

        return response.contents().stream()
            .map(S3Object::key)
            .findFirst()
            .map(key -> key.substring(key.lastIndexOf('/') + 1))
            .orElseThrow(() -> new RuntimeException("File not found for UUID: " + uuid));
    }

    private void ensureBucket() {
        try {
            s3Client.headBucket(
                HeadBucketRequest
                    .builder()
                    .bucket(properties.getS3Bucket())
                    .build()
            );
        } catch (NoSuchBucketException e) {
            log.info("Bucket does not exist: {}", properties.getS3Bucket());
            s3Client.createBucket(
                CreateBucketRequest
                    .builder()
                    .bucket(properties.getS3Bucket())
                    .build()
            );
        }
    }

}
