package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlManagementUseCaseImplTest {
    @Test
    void passesListFiltersAndSortToRepository() {
        UrlRepository repository = mock(UrlRepository.class);
        when(repository.findList(eq("wiki"), eq("active"), org.mockito.ArgumentMatchers.any()))
            .thenReturn(Page.empty());

        new UrlManagementUseCaseImpl(repository).list("wiki", "active", "createdAt,desc", 0, 25);

        var pageable = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findList(eq("wiki"), eq("active"), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(25);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection().isDescending()).isTrue();
    }
}
