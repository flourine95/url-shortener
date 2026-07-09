package com.example.urlshortener.infrastructure.persistence.mapper;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlMapper INSTANCE = Mappers.getMapper(UrlMapper.class);

    UrlData toDomain(UrlEntity entity);
    UrlEntity toEntity(UrlData domain);
}
