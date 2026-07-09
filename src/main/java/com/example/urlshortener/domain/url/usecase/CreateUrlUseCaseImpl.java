package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.exception.DomainException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreateUrlUseCaseImpl implements CreateUrlUseCase {
    private final UrlRepository urlRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public UrlData execute(CreateUrlCommand command) {
        String shortCode;
        if (command.customCode() != null && !command.customCode().isBlank()) {
            shortCode = command.customCode().trim();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new DomainException("SHORT_CODE_ALREADY_EXISTS", "Short code " + shortCode + " already exists", 409) {};
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        UrlData urlData = new UrlData(
            null,
            command.originalUrl(),
            shortCode,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        return urlRepository.save(urlData);
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
