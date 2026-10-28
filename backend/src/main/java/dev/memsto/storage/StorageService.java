package dev.memsto.storage;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3;
    private final StorageProperties props;

    public void put(String key, InputStream data, long size, String contentType) {
        s3.putObject(b -> b.bucket(props.bucket()).key(key).contentType(contentType),
                RequestBody.fromInputStream(data, size));
    }

    public ResponseInputStream<GetObjectResponse> get(String key) {
        return s3.getObject(b -> b.bucket(props.bucket()).key(key));
    }

    public void delete(String key) {
        s3.deleteObject(b -> b.bucket(props.bucket()).key(key));
    }

    /** Throws if the bucket is unreachable or missing. */
    public void checkBucket() {
        s3.headBucket(b -> b.bucket(props.bucket()));
    }
}
