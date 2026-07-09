package com.example.urlshortener.application.url.controller;

import com.example.urlshortener.application.url.request.CreateUrlRequest;
import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.usecase.CreateUrlUseCase;
import com.example.urlshortener.domain.url.usecase.RedirectUrlUseCase;
import com.example.urlshortener.shared.response.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UrlController {
    private final CreateUrlUseCase createUrlUseCase;
    private final RedirectUrlUseCase redirectUrlUseCase;

    @PostMapping("/api/urls")
    public ResponseEntity<ApiResult<UrlData>> create(@Valid @RequestBody CreateUrlRequest request) {
        CreateUrlCommand command = new CreateUrlCommand(request.originalUrl(), request.customCode());
        UrlData urlData = createUrlUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.ok(urlData, "URL shortened successfully"));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
            HttpServletRequest request) {
        
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        UrlData urlData = redirectUrlUseCase.execute(shortCode, ipAddress, userAgent);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, urlData.originalUrl())
                .build();
    }
}
