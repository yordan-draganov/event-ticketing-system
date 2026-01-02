package com.example.events.mapper;

import com.example.events.DTO.SectionRequestDTO;
import com.example.events.DTO.SectionResponse;
import com.example.events.model.Event;
import com.example.events.model.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "totalSeats", expression = "java(section.getRowsCount() * section.getColsCount())")
    @Mapping(target = "availableSeats", ignore = true)
    SectionResponse toResponse(Section section);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "price", source = "dto.price")
    @Mapping(target = "rowsCount", source = "dto.rows")
    @Mapping(target = "colsCount", source = "dto.cols")
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Section toEntity(SectionRequestDTO dto, Event event);
}