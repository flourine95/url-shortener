package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlManagementUseCaseTest {
    @Test
    void passesListFiltersAndSortToRepository() {
        UrlRepository repository = mock(UrlRepository.class);
        when(repository.findList(eq("wiki"), eq("active"), ArgumentMatchers.any()))
            .thenReturn(Page.empty());

        new UrlManagementUseCase(repository).list("wiki", "active", "createdAt,desc", 0, 25);

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findList(eq("wiki"), eq("active"), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(25);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection().isDescending()).isTrue();
    }
}
