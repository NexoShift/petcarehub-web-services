package com.nexoshift.petcarehub.shared.interfaces.rest.controllers;

import com.nexoshift.petcarehub.shared.domain.services.FileStorageService;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.MessageResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("api/v1/test/s3")
public class S3TestController {
    private final FileStorageService fileStorageService;

    public S3TestController(FileStorageService fileStorageService){
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> upload(@RequestParam("file")MultipartFile file) throws IOException{

        if(file.isEmpty()){
            throw new IllegalArgumentException("File cannot be empty");
        }

        String objectKey;

        try(var inputStream = file.getInputStream()){
            objectKey = fileStorageService.upload(inputStream, file.getSize(), file.getContentType(), "profile");
        }
        return ResponseEntity.ok(Map.of("message","File uploaded successfully", "objectKey", objectKey));
    }

    @GetMapping("/url")
    public ResponseEntity<Map<String,String>> getUrl(@RequestParam String objectKey){

        validateTestObjectKey(objectKey);
        String url = fileStorageService.generatePresignedUrl(objectKey, Duration.ofMinutes(10));

        return ResponseEntity.ok(Map.of("objectKey", objectKey, "url", url));
    }

    @DeleteMapping
    public ResponseEntity<MessageResource> delete(@RequestParam String objectKey){

        validateTestObjectKey(objectKey);
        fileStorageService.delete(objectKey);

        return ResponseEntity.ok(new MessageResource("File deleted successfully"));
    }


    private void validateTestObjectKey(String objectKey) {
        if (objectKey == null ||
                !objectKey.matches(
                        "profile/[0-9a-fA-F-]{36}\\.(jpg|png|webp|pdf)"
                )) {
            throw new IllegalArgumentException(
                    "Invalid test object key"
            );
        }
    }
}
