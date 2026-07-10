package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.exception.InvalidExpirationException;
import com.example.urlshortener.domain.url.exception.InvalidOriginalUrlException;
import com.example.urlshortener.domain.url.exception.ShortCodeAlreadyExistsException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreateUrlUseCaseImpl implements CreateUrlUseCase {
    private final UrlRepository urlRepository;
    private final Random random = new SecureRandom();

    @Override
    @Transactional
    public UrlData execute(CreateUrlCommand command) {
        String originalUrl = command.originalUrl().trim();
        validateOriginalUrl(originalUrl);
        validateExpiration(command.expiresAt());

        String shortCode;
        if (command.customCode() != null && !command.customCode().isBlank()) {
            shortCode = command.customCode().trim();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(shortCode);
            }
        } else {
            var existing = urlRepository.findByOriginalUrl(originalUrl);
            if (existing.isPresent() && !isExpired(existing.get())) {
                return existing.get();
            }
            shortCode = generateUniqueShortCode();
        }

        UrlData urlData = new UrlData(
            null,
            originalUrl,
            shortCode,
            null,
            null,
            command.expiresAt()
        );

        return urlRepository.save(urlData);
    }

    private void validateOriginalUrl(String originalUrl) {
        try {
            URI uri = URI.create(originalUrl);
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!normalizedScheme.equals("http") && !normalizedScheme.equals("https")) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new InvalidOriginalUrlException();
        }
    }

    private void validateExpiration(LocalDateTime expiresAt) {
        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            throw new InvalidExpirationException();
        }
    }

    private boolean isExpired(UrlData urlData) {
        return urlData.expiresAt() != null && !urlData.expiresAt().isAfter(LocalDateTime.now());
    }

    private String generateUniqueShortCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        if (urlRepository.existsByShortCode(code)) {
            return generateUniqueShortCode();
        }
        return code;
    }
}
