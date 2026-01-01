package com.example.events.mapper;

import com.example.events.DTO.SectionResponse;
import com.example.events.model.Section;
import com.example.events.repository.SeatRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SectionMapper {

    @Autowired
    protected SeatRepository seatRepository;

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "totalSeats", expression = "java(section.getRowsCount() * section.getColsCount())")
    @Mapping(target = "availableSeats", source = "section", qualifiedByName = "countAvailable")
    public abstract SectionResponse toResponse(Section section);

    @Named("countAvailable")
    protected int countAvailable(Section section) {
        return (int) seatRepository.countAvailableBySectionId(section.getId());
    }
}