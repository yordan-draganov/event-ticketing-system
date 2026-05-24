package com.example.events.repository;

import com.example.events.model.Reservation;
import com.example.events.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByPaymentIntentId(String paymentIntentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT r FROM Reservation r WHERE r.paymentIntentId = :paymentIntentId")
    Optional<Reservation> findByPaymentIntentIdForUpdate(String paymentIntentId);

    @Modifying
    @Query("UPDATE Reservation r SET r.status = :expiredStatus WHERE r.status = :pendingStatus AND r.expiresAt <= CURRENT_TIMESTAMP")
    int expireReservations(ReservationStatus pendingStatus, ReservationStatus expiredStatus);
}