package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.exception.InvalidPaginationException;
import com.example.urlshortener.domain.url.exception.UrlNotFoundException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlManagementUseCaseImpl implements UrlManagementUseCase {
    private final UrlRepository urlRepository;

    @Override
    public Page<UrlData> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidPaginationException();
        }
        return urlRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Override
    public void delete(String shortCode) {
        urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
        urlRepository.deleteByShortCode(shortCode);
    }
}
