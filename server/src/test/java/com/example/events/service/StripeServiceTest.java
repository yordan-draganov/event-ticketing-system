package com.example.events.service;

import com.example.events.exception.UnauthorizedException;
import com.example.events.exception.ValidationException;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.model.Section;
import com.example.events.repository.EventRepository;
import com.example.events.repository.SeatRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @InjectMocks
    private StripeService stripeService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketService ticketService;

    @Test
    void createPaymentIntentShouldRejectUnavailableSeats() {
        UUID eventId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);

        Section section = Section.builder()
                .price(BigDecimal.valueOf(25))
                .build();

        Seat seat = Seat.builder()
                .id(seatId)
                .event(event)
                .section(section)
                .isAvailable(false)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatRepository.findAllById(List.of(seatId))).thenReturn(List.of(seat));

        assertThrows(ValidationException.class,
                () -> stripeService.createPaymentIntent(eventId, List.of(seatId), UUID.randomUUID()));
    }

    @Test
    void getPaymentStatusShouldRejectForeignPaymentIntent() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        PaymentIntent intent = mock(PaymentIntent.class);

        when(intent.getMetadata()).thenReturn(Map.of("userId", ownerId.toString()));

        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.retrieve("pi_test")).thenReturn(intent);

            assertThrows(UnauthorizedException.class,
                    () -> stripeService.getPaymentStatus("pi_test", requesterId));
        }
    }

    @Test
    void cancelPaymentIntentShouldRejectForeignPaymentIntent() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        PaymentIntent intent = mock(PaymentIntent.class);

        when(intent.getMetadata()).thenReturn(Map.of("userId", ownerId.toString()));

        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.retrieve("pi_test")).thenReturn(intent);

            assertThrows(UnauthorizedException.class,
                    () -> stripeService.cancelPaymentIntent("pi_test", requesterId));

            verify(intent, never()).cancel(any(PaymentIntentCancelParams.class));
        }
    }
}
