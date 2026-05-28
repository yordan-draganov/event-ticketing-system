package com.example.events.service;

import com.example.events.repository.SeatRepository;
import com.example.events.model.ReservationStatus;
import com.example.events.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatReservationCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(SeatReservationCleanupService.class);

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelayString = "${checkout.reservation-cleanup-delay-ms:60000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseExpiredReservations() {
        reservationRepository.expireReservations(ReservationStatus.pending, ReservationStatus.expired);
        int releasedSeats = seatRepository.releaseExpiredReservations();
        if (releasedSeats > 0) {
            logger.info("Released {} expired seat reservations", releasedSeats);
        }
    }
}