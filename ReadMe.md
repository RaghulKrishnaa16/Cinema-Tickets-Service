# 🎬 Cinema Tickets Service

## 📌 Overview

This project is a solution to the DWP Cinema Tickets coding exercise.  
It implements a ticket purchasing service that validates business rules, calculates payments, and reserves seats using external services.

The focus of this solution is on:
- Clean and maintainable Java code
- Strong validation of business rules
- Proper unit testing
- Clear separation of concerns

---

## 🚀 Features

- Supports purchasing:
    - Adult tickets (£25)
    - Child tickets (£15)
    - Infant tickets (£0)
- Validates all business rules before processing
- Calculates:
    - Total payment amount
    - Total seats to reserve
- Integrates with:
    - `TicketPaymentService`
    - `SeatReservationService`
- Comprehensive unit test coverage

---

## 📊 Business Rules Implemented

- Maximum of **25 tickets per purchase**
- **Infants do not require seats**
- **Child and Infant tickets must be accompanied by at least one Adult**
- **Each Infant must have a corresponding Adult (lap rule)**
- Ticket quantities must be **greater than zero**
- Account ID must be **valid (> 0)**

---

## 💰 Pricing

| Ticket Type | Price |
|------------|-------|
| ADULT      | £25   |
| CHILD      | £15   |
| INFANT     | £0    |

---

## 🧠 Assumptions

- Infants sit on an Adult's lap → enforced by ensuring:
- infantCount <= adultCount
- - External services (`TicketPaymentService`, `SeatReservationService`) are assumed to always succeed
- No persistence or API layer is required (as per exercise scope)

---

## 🧩 Design Approach

- **Single Responsibility Principle** applied
- Business logic broken into private helper methods:
- validation
- counting tickets
- calculating totals
- Dependency Injection used for testability
- Immutable domain object (`TicketTypeRequest`)

---

## 🧪 Testing

- Unit tests implemented using:
- **JUnit 5**
- **Mockito**
- Covers:
- Valid scenarios
- Edge cases
- Invalid inputs
- Business rule violations

To run tests:

```bash
mvn test