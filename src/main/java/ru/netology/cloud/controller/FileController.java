package ru.netology.cloud.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.model.User;
import ru.netology.cloud.service.FileService;
import ru.netology.cloud.service.TokenService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;
    private final TokenService tokenService;

    public FileController(FileService fileService, TokenService tokenService) {
        this.fileService = fileService;
        this.tokenService = tokenService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) {

        return tokenService.validateToken(authToken)
                .map(token -> {
                    try {
                        User user = token.getUser();
                        fileService.saveFile(file, filename, user);
                        return ResponseEntity.ok().build();
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Error uploading file"));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Unauthorized")));
    }

    @GetMapping
    public ResponseEntity<?> downloadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        return tokenService.validateToken(authToken)
                .map(token -> {
                    try {
                        User user = token.getUser();
                        Path filePath = fileService.getFilePath(filename, user);
                        Resource resource = new UrlResource(filePath.toUri());

                        if (resource.exists() && resource.isReadable()) {
                            return ResponseEntity.ok()
                                    .header(HttpHeaders.CONTENT_DISPOSITION,
                                            "attachment; filename="" + filename + """)
                                    .body(resource);
                        } else {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("message", "File not found"));
                        }
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Error downloading file"));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Unauthorized")));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        return tokenService.validateToken(authToken)
                .map(token -> {
                    try {
                        User user = token.getUser();
                        fileService.deleteFile(filename, user);
                        return ResponseEntity.ok().build();
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Error deleting file"));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Unauthorized")));
    }

    @PutMapping
    public ResponseEntity<?> renameFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename,
            @RequestBody Map<String, String> newName) {

        return tokenService.validateToken(authToken)
                .map(token -> {
                    try {
                        User user = token.getUser();
                        String newFilename = newName.get("name");
                        fileService.renameFile(filename, newFilename, user);
                        return ResponseEntity.ok().build();
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Error renaming file"));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Unauthorized")));
    }
}
