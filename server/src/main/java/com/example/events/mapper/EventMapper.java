package com.example.events.mapper;

import com.example.events.DTO.EventCreateDTO;
import com.example.events.DTO.EventResponse;
import com.example.events.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isFinished", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventCreateDTO dto);

    @Mapping(target = "minPrice", ignore = true)
    @Mapping(target = "maxPrice", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "sectionCount", ignore = true)
    EventResponse toResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isFinished", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(EventCreateDTO dto, @MappingTarget Event event);
}