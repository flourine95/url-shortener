package com.example.urlshortener.infrastructure.persistence.mapper;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    VisitData toDomain(VisitEntity entity);
    VisitEntity toEntity(VisitData domain);
}
