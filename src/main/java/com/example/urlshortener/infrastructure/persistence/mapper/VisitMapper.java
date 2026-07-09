package com.example.urlshortener.infrastructure.persistence.mapper;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    VisitMapper INSTANCE = Mappers.getMapper(VisitMapper.class);

    VisitData toDomain(VisitEntity entity);
    VisitEntity toEntity(VisitData domain);
}
