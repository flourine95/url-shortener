package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.repository.VisitDatabasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveVisitUseCaseImpl implements SaveVisitUseCase {
    private final VisitDatabasePort visitDatabasePort;

    @Override
    @Transactional
    public void execute(VisitData visitData) {
        visitDatabasePort.save(visitData);
    }
}
