package ru.netology.cloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloud.model.User;
import ru.netology.cloud.service.FileService;
import ru.netology.cloud.service.TokenService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/list")
public class FileListController {
    private final FileService fileService;
    private final TokenService tokenService;

    public FileListController(FileService fileService, TokenService tokenService) {
        this.fileService = fileService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ResponseEntity<?> getFileList(
            @RequestHeader("auth-token") String authToken,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        return tokenService.validateToken(authToken)
                .map(token -> {
                    User user = token.getUser();
                    List<ru.netology.cloud.model.FileInfo> files = fileService.getUserFiles(user);

                    List<Map<String, Object>> fileList = files.stream()
                            .limit(limit)
                            .map(file -> Map.of(
                                    "filename", file.getFilename(),
                                    "size", file.getSize()
                            ))
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(fileList);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Unauthorized")));
    }
}
