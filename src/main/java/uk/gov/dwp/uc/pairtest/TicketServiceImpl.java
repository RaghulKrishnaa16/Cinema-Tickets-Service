package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl() {
        this.ticketPaymentService = new TicketPaymentServiceImpl();
        this.seatReservationService = new SeatReservationServiceImpl();
    }
    public TicketServiceImpl(TicketPaymentService paymentService,
                             SeatReservationService seatService) {
        this.ticketPaymentService = paymentService;
        this.seatReservationService = seatService;
    }


    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccount(accountId);
        validateRequests(ticketTypeRequests);

        int adultCount = getTicketCount(ticketTypeRequests, TicketTypeRequest.Type.ADULT);
        int childCount = getTicketCount(ticketTypeRequests, TicketTypeRequest.Type.CHILD);
        int infantCount = getTicketCount(ticketTypeRequests, TicketTypeRequest.Type.INFANT);

        validateBusinessRules(adultCount, childCount, infantCount);

        int totalAmount = calculateTotalAmount(adultCount, childCount);
        int totalSeats = calculateTotalSeats(adultCount, childCount);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    }

    private void validateRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null || request.getTicketType() == null || request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }
        }
    }

    private int getTicketCount(TicketTypeRequest[] ticketTypeRequests, TicketTypeRequest.Type type) {
        int count = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getTicketType() == type) {
                count += request.getNoOfTickets();
            }
        }

        return count;
    }

    private void validateBusinessRules(int adultCount, int childCount, int infantCount) {
        int totalTickets = adultCount + childCount + infantCount;

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }

        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException();
        }

        if (infantCount > adultCount) {
            throw new InvalidPurchaseException();
        }
    }

    private int calculateTotalAmount(int adultCount, int childCount) {
        return (adultCount * ADULT_PRICE) + (childCount * CHILD_PRICE);
    }

    private int calculateTotalSeats(int adultCount, int childCount) {
        return adultCount + childCount;
    }
}