import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService seatService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        seatService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatService);
    }

    @Test
    void shouldPurchaseAdultTicketsSuccessfully() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));

        verify(paymentService).makePayment(1L, 50);
        verify(seatService).reserveSeat(1L, 2);
    }

    @Test
    void shouldCalculateCorrectPaymentAndSeatsForMixedTickets() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        verify(paymentService).makePayment(1L, 95);
        verify(seatService).reserveSeat(1L, 5);
    }

    @Test
    void shouldNotAllocateSeatsForInfants() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));

        verify(paymentService).makePayment(1L, 50);
        verify(seatService).reserveSeat(1L, 2);
    }

    @Test
    void shouldRejectNullAccountId() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(null,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));

        verify(paymentService, never()).makePayment(1L, 25);
        verify(seatService, never()).reserveSeat(1L, 1);
    }

    @Test
    void shouldRejectInvalidAccountId() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void shouldRejectEmptyTicketRequests() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L));
    }

    @Test
    void shouldRejectNullTicketRequestsArray() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    @Test
    void shouldRejectNullTicketRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                        null));
    }

    @Test
    void shouldRejectZeroTicketsInRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)));
    }

    @Test
    void shouldRejectNegativeTicketsInRequest() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)));
    }

    @Test
    void shouldRejectChildOnlyPurchase() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)));
    }

    @Test
    void shouldRejectInfantOnlyPurchase() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
    }

    @Test
    void shouldRejectChildAndInfantWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
    }

    @Test
    void shouldRejectMoreThanTwentyFiveTickets() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6)));
    }

    @Test
    void shouldAllowExactlyTwentyFiveTickets() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5));

        verify(paymentService).makePayment(1L, 575);
        verify(seatService).reserveSeat(1L, 25);
    }

    @Test
    void shouldRejectMoreInfantsThanAdults() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)));
    }
}