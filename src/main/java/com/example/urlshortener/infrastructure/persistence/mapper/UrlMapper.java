package com.example.urlshortener.infrastructure.persistence.mapper;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    UrlData toDomain(UrlEntity entity);
    UrlEntity toEntity(UrlData domain);
}
