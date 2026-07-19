package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.infrastructure.persistence.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveVisitUseCase {
    private final VisitRepository visitRepository;

    @Transactional
    public void execute(VisitData visitData) {
        visitRepository.save(visitData);
    }
}
