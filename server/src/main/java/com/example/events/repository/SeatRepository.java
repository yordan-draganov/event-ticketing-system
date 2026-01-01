package com.example.events.repository;

import com.example.events.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(UUID eventId);

    List<Seat> findBySectionIdOrderByRowLabelAscSeatNumberAsc(UUID sectionId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.event.id = :eventId AND s.isAvailable = true")
    long countAvailableByEventId(UUID eventId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.section.id = :sectionId AND s.isAvailable = true")
    long countAvailableBySectionId(UUID sectionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.event.id = :eventId")
    List<Seat> findByIdInAndEventId(List<UUID> seatIds, UUID eventId);

    List<Seat> findByTicketId(UUID ticketId);
}