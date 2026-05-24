package com.example.events.repository;

import com.example.events.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(UUID eventId);

    List<Seat> findBySectionIdOrderByRowLabelAscSeatNumberAsc(UUID sectionId);

    @Query("SELECT COUNT(s) FROM Seat s LEFT JOIN s.reservation r WHERE s.event.id = :eventId AND s.ticket IS NULL AND (r IS NULL OR r.status <> com.example.events.model.ReservationStatus.pending OR r.expiresAt <= CURRENT_TIMESTAMP)")
    long countAvailableByEventId(UUID eventId);

    @Query("SELECT COUNT(s) FROM Seat s LEFT JOIN s.reservation r WHERE s.section.id = :sectionId AND s.ticket IS NULL AND (r IS NULL OR r.status <> com.example.events.model.ReservationStatus.pending OR r.expiresAt <= CURRENT_TIMESTAMP)")
    long countAvailableBySectionId(UUID sectionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.event.id = :eventId")
    List<Seat> findByIdInAndEventId(List<UUID> seatIds, UUID eventId);

    List<Seat> findByTicketId(UUID ticketId);

    @Query("SELECT s FROM Seat s JOIN FETCH s.section WHERE s.reservation.id = :reservationId ORDER BY s.rowLabel, s.seatNumber")
    List<Seat> findByReservationIdOrderByRowLabelAscSeatNumberAsc(UUID reservationId);

    @Modifying
    @Query("UPDATE Seat s SET s.isAvailable = true, s.reservation = NULL, s.reservedBy = NULL, s.reservationExpiresAt = NULL, s.reservationPaymentIntentId = NULL WHERE s.ticket IS NULL AND s.reservation.paymentIntentId = :paymentIntentId")
    int clearReservationByPaymentIntentId(String paymentIntentId);

    @Modifying
    @Query("UPDATE Seat s SET s.isAvailable = true, s.reservation = NULL, s.reservedBy = NULL, s.reservationExpiresAt = NULL, s.reservationPaymentIntentId = NULL WHERE s.ticket IS NULL AND s.reservation.id = :reservationId")
    int clearReservationByReservationId(UUID reservationId);

    @Modifying
    @Query("UPDATE Seat s SET s.reservationPaymentIntentId = :paymentIntentId WHERE s.reservation.id = :reservationId")
    int attachPaymentIntentToReservationSeats(UUID reservationId, String paymentIntentId);

    @Modifying
    @Query("UPDATE Seat s SET s.isAvailable = true, s.reservation = NULL, s.reservedBy = NULL, s.reservationExpiresAt = NULL, s.reservationPaymentIntentId = NULL WHERE s.ticket IS NULL AND s.reservation.status = com.example.events.model.ReservationStatus.expired")
    int releaseExpiredReservations();
}