package com.example.urlshortener.application.url.controller;

import com.example.urlshortener.application.url.request.CreateUrlRequest;
import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.dto.UrlListItem;
import com.example.urlshortener.domain.url.dto.UrlStats;
import com.example.urlshortener.domain.url.usecase.CreateUrlUseCase;
import com.example.urlshortener.domain.url.usecase.RedirectUrlUseCase;
import com.example.urlshortener.domain.url.usecase.UrlManagementUseCase;
import com.example.urlshortener.domain.url.usecase.UrlStatsUseCase;
import com.example.urlshortener.shared.response.ApiResult;
import com.example.urlshortener.shared.response.PageMeta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController {
    private final CreateUrlUseCase createUrlUseCase;
    private final RedirectUrlUseCase redirectUrlUseCase;
    private final UrlStatsUseCase urlStatsUseCase;
    private final UrlManagementUseCase urlManagementUseCase;

    @PostMapping("/api/urls")
    public ResponseEntity<ApiResult<UrlData>> create(@Valid @RequestBody CreateUrlRequest request) {
        CreateUrlCommand command = new CreateUrlCommand(request.originalUrl(), request.customCode(), request.expiresAt());
        UrlData urlData = createUrlUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.ok(urlData, "URL shortened successfully"));
    }

    @GetMapping("/api/urls")
    public ApiResult<List<UrlListItem>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UrlListItem> result = urlManagementUseCase.list(q, status, sort, page, size);
        return ApiResult.page(result.getContent(), PageMeta.from(result), "URLs fetched successfully");
    }

    @GetMapping("/api/urls/{shortCode}/stats")
    public ApiResult<UrlStats> stats(@PathVariable String shortCode) {
        return ApiResult.ok(urlStatsUseCase.execute(shortCode), "Stats fetched successfully");
    }

    @DeleteMapping("/api/urls/{shortCode}")
    public ApiResult<Void> delete(@PathVariable String shortCode) {
        urlManagementUseCase.delete(shortCode);
        return ApiResult.ok(null, "URL deleted successfully");
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
