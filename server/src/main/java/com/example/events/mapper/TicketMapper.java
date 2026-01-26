package com.example.events.mapper;

import com.example.events.DTO.TicketDetailResponse;
import com.example.events.DTO.TicketResponse;
import com.example.events.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "event.date", target = "eventDate")
    @Mapping(source = "event.location", target = "eventLocation")
    @Mapping(source = "event.startTime", target = "startTime")
    @Mapping(source = "event.endTime", target = "endTime")
    @Mapping(source = "event.image", target = "eventImage")
    @Mapping(source = "section.id", target = "sectionId")
    @Mapping(source = "section.name", target = "sectionName")
    @Mapping(target = "seatCount", ignore = true)
    TicketResponse toResponse(Ticket ticket);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "event.date", target = "eventDate")
    @Mapping(source = "event.location", target = "eventLocation")
    @Mapping(source = "event.description", target = "eventDescription")
    @Mapping(source = "event.longDescription", target = "eventLongDescription")
    @Mapping(source = "event.category", target = "eventCategory")
    @Mapping(source = "event.image", target = "eventImage")
    @Mapping(source = "event.organizer", target = "eventOrganizer")
    @Mapping(source = "event.startTime", target = "startTime")
    @Mapping(source = "event.endTime", target = "endTime")
    @Mapping(source = "section.id", target = "sectionId")
    @Mapping(source = "section.name", target = "sectionName")
    @Mapping(target = "seatCount", ignore = true)
    @Mapping(target = "seats", ignore = true)
    TicketDetailResponse toDetailResponse(Ticket ticket);
}