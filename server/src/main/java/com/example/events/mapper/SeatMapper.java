package com.example.events.mapper;

import com.example.events.DTO.SeatResponse;
import com.example.events.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionName", source = "section.name")
    @Mapping(target = "sectionPrice", source = "section.price")
    @Mapping(target = "isAvailable", expression = "java(seat.isAvailableForPurchase())")
    @Mapping(target = "displayLabel", expression = "java(seat.getRowLabel() + \"-\" + seat.getSeatNumber())")
    SeatResponse toResponse(Seat seat);
}