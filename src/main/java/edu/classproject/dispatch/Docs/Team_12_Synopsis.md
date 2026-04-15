# Team 12 - OVERRIDERS
## OOAD Mini Project: Food Delivery App
### Dispatch Assignment System – Project Synopsis

**Team Details:**
- Shashank S Shetty - PES1UG23AM284
- Shashank H S - PES1UG23AM282
- Shivakumar S Kokatanur - PES1UG23AM286
- Sachith Kanwa - PES1UG23AM253

---

## 1. Executive Summary

This document outlines the design and implementation strategy for the Dispatch Assignment module.
The primary goal of this module is to assign a delivery partner to an order based on availability while
ensuring clear failure handling when no partner is available.

The system enforces strict dispatch rules, guarantees only available partners are selected, and
provides explicit reasons for assignment failure. The module is designed to integrate seamlessly
with order and delivery partner services.

---

## 2. Requirements to Satisfy

To ensure proper functionality and adherence to system architecture, the following requirements must be met:

- **Availability Constraint:** Only delivery partners marked as available can be assigned.
- **Failure Handling:** If no partner is available, dispatch must fail with a clear reason.
- **Deterministic Assignment:** The assignment rule must be consistent and testable.
- **Integration:** Must work with Order and Delivery modules.
- **Compliance:** Must pass all unit tests and expose clear service contracts.

---

## 3. Proposed Implementation

The implementation follows a clean architecture pattern within the package:
`edu.classproject.dispatch`

### 3.1 Components

- **DispatchService (Interface):** Defines the contract for assigning delivery partners.
- **DefaultDispatchService (Implementation):** Contains the core business logic for partner assignment.
- **DispatchAssignment (Record):** Represents the result of the dispatch process.
- **Dependencies:**
  - `DeliveryPartnerService` → to fetch available partners
  - `OrderService` → to validate order existence

### 3.2 Contract Types (DTOs)

**DispatchAssignment fields:**
- `assignmentId`
- `orderId`
- `partnerId`
- `reason`

### 3.3 Utilities

- **PartnerSelector:** Handles selection logic (e.g., first available / nearest partner).

---

## 4. Functional Behavior

| Feature         | Description |
|---              |---|
| `assignPartner` | Assigns an available partner to an order |
| success case    | Returns assignment with `partnerId` |
| failure case    | Returns `null` partnerId with failure reason |

---

## 5. Validation Rules

| Field | Validation Requirement |
|---|---|
| Order ID | Must be non-null and correspond to an existing order in the system. |
| Restaurant ID | Must be non-null and valid for the given order. |
| Customer ID | Must be non-null and associated with a valid user. |
| Available Partners List | Must be fetched from `DeliveryPartnerService` and should not be null. |
| Partner Availability | Only partners with `available = true` can be considered for assignment. |
| Partner Selection | At least one available partner must exist to proceed with assignment. |
| Assignment ID | Must be generated uniquely for every successful dispatch. |
| Failure Condition | If no partner is available, the system must return a failure with reason: `"No delivery partner available"`. |
| Consistency Rule | Given the same set of available partners, the selection logic must produce consistent results. |

---

## 6. Test and Demo Plan

### 6.1 Unit Tests

- **Assignment Success:** Verify partner is assigned when available.
- **Assignment Failure:** Verify failure when no partners are available.
- **Correct Partner Selection:** Ensure only available partner is chosen.
- **Edge Case:** Empty partner list should return failure.

### 6.2 Demo Scenario

1. Create an order.
2. Add delivery partners:
   - P1 (Available)
   - P2 (Busy)
3. Call dispatch service.
4. System assigns P1.
5. If all partners are busy → failure message returned.


---
