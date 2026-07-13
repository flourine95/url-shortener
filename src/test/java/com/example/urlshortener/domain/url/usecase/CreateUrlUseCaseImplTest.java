package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.dto.UrlListItem;
import com.example.urlshortener.domain.url.exception.DomainException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateUrlUseCaseImplTest {

    @Test
    void returnsExistingShortCodeForSameOriginalUrl() {
        FakeUrlRepository repository = new FakeUrlRepository();
        UrlData existing = new UrlData(1L, "https://example.com/a", "abc123", Instant.now(), Instant.now());
        repository.save(existing);

        UrlData result = new CreateUrlUseCaseImpl(repository)
            .execute(new CreateUrlCommand(" https://example.com/a ", null));

        assertThat(result.shortCode()).isEqualTo("abc123");
        assertThat(repository.saves).isEqualTo(1);
    }

    @Test
    void rejectsNonHttpUrls() {
        FakeUrlRepository repository = new FakeUrlRepository();

        assertThatThrownBy(() -> new CreateUrlUseCaseImpl(repository)
            .execute(new CreateUrlCommand("javascript:alert(1)", null)))
            .isInstanceOf(DomainException.class)
            .hasMessage("Original URL must be a valid http or https URL");
    }

    @Test
    void rejectsPastExpiration() {
        FakeUrlRepository repository = new FakeUrlRepository();

        assertThatThrownBy(() -> new CreateUrlUseCaseImpl(repository)
            .execute(new CreateUrlCommand(
                "https://example.com",
                null,
                Instant.now().minusSeconds(60))))
            .isInstanceOf(DomainException.class)
            .hasMessage("Expiration must be in the future");
    }

    private static class FakeUrlRepository implements UrlRepository {
        private final Map<String, UrlData> byShortCode = new HashMap<>();
        private final Map<String, UrlData> byOriginalUrl = new HashMap<>();
        private int saves;

        @Override
        public UrlData save(UrlData urlData) {
            saves++;
            byShortCode.put(urlData.shortCode(), urlData);
            byOriginalUrl.put(urlData.originalUrl(), urlData);
            return urlData;
        }

        @Override
        public Optional<UrlData> findByShortCode(String shortCode) {
            return Optional.ofNullable(byShortCode.get(shortCode));
        }

        @Override
        public Optional<UrlData> findByOriginalUrl(String originalUrl) {
            return Optional.ofNullable(byOriginalUrl.get(originalUrl));
        }

        @Override
        public boolean existsByShortCode(String shortCode) {
            return byShortCode.containsKey(shortCode);
        }

        @Override
        public Page<UrlListItem> findList(String q, String status, Pageable pageable) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        @Override
        public void deleteByShortCode(String shortCode) {
            byShortCode.remove(shortCode);
        }
    }
}
