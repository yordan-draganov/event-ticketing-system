package com.example.events.service;

import com.example.events.exception.UnauthorizedException;
import com.example.events.exception.ValidationException;
import com.example.events.model.Event;
import com.example.events.model.Seat;
import com.example.events.model.Section;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.EventRepository;
import com.example.events.repository.ReservationRepository;
import com.example.events.repository.SeatRepository;
import com.example.events.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

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
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    @Test
    void createPaymentIntentShouldRejectUnavailableSeats() {
        UUID eventId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

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
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("testuser")
                .password("encoded")
                .role(UserRole.user)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(seatRepository.findByIdInAndEventId(List.of(seatId), eventId)).thenReturn(List.of(seat));
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);

        assertThrows(ValidationException.class,
                () -> stripeService.createPaymentIntent(eventId, List.of(seatId), userId));
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
