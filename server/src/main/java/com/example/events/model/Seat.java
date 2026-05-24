package com.example.events.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seats")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "row_label", nullable = false, length = 5)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "reserved_by")
    private UUID reservedBy;

    @Column(name = "reservation_expires_at")
    private LocalDateTime reservationExpiresAt;

    @Column(name = "reservation_payment_intent_id")
    private String reservationPaymentIntentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean hasActiveReservation() {
        if (reservation != null) {
            return reservation.isActive();
        }

        return reservationExpiresAt != null && reservationExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isAvailableForPurchase() {
        return ticket == null
                && !hasActiveReservation()
                && (Boolean.TRUE.equals(isAvailable) || reservationExpiresAt != null);
    }

    public boolean hasReservationFor(UUID userId, UUID reservationId) {
        return reservation != null
                && reservationId != null
                && reservationId.equals(reservation.getId())
                && reservation.belongsTo(userId)
                && reservation.isActive();
    }

    public void reserve(Reservation reservation) {
        this.reservation = reservation;
        this.reservedBy = reservation.getUser().getId();
        this.reservationExpiresAt = reservation.getExpiresAt();
        this.reservationPaymentIntentId = reservation.getPaymentIntentId();
        this.isAvailable = false;
    }

    public void clearReservation() {
        this.reservedBy = null;
        this.reservationExpiresAt = null;
        this.reservationPaymentIntentId = null;
        this.reservation = null;
    }

    public void releaseReservation() {
        clearReservation();
        if (ticket == null) {
            this.isAvailable = true;
        }
    }
}