package edu.classproject.dispatch;

import edu.classproject.common.IdGenerator;
import edu.classproject.delivery.DeliveryPartner;
import edu.classproject.delivery.DeliveryPartnerService;
import edu.classproject.order.OrderService;

import java.util.List;
import java.util.Optional;

/**
 * Immutable record representing the result of a dispatch attempt.
 *
 * Design patterns applied:
 *  - Creational / Factory Method: static factory methods (success/failure)
 *    enforce clear, named construction semantics instead of raw constructors.
 *  - GRASP Creator: this record owns its own creation logic.
 *  - SRP (SOLID): only models the dispatch result + its creation.
 *
 * Functional behaviour (per synopsis §4):
 *  - success case → assignmentId + partnerId populated
 *  - failure case → partnerId null, reason = "No delivery partner available"
 */
public record DispatchAssignment(
        String assignmentId,
        String orderId,
        String partnerId,
        String reason) {

    // ------------------------------------------------------------------ //
    //  Factory Methods (Creational Pattern)
    // ------------------------------------------------------------------ //

    /** Returns a successful assignment with a unique ID. */
    public static DispatchAssignment success(String orderId, String partnerId) {
        return new DispatchAssignment(
                IdGenerator.nextId("ASGN"),
                orderId,
                partnerId,
                "Partner assigned successfully");
    }

    /** Returns a failed assignment — partnerId is null, reason explains why. */
    public static DispatchAssignment failure(String orderId, String reason) {
        return new DispatchAssignment(null, orderId, null, reason);
    }

    /** Convenience predicate used by callers and unit tests. */
    public boolean isSuccess() {
        return partnerId != null;
    }

    // ------------------------------------------------------------------ //
    //  DefaultDispatchService — inner implementation
    //
    //  Design patterns applied:
    //   - DIP (SOLID): depends on DeliveryPartnerService & OrderService
    //     abstractions, never on concrete classes.
    //   - Low Coupling (GRASP): wired via constructor injection.
    //   - SRP (SOLID): only responsible for orchestrating dispatch.
    //   - PartnerSelector logic (from synopsis §3.3) is a private static
    //     helper here to avoid creating an extra file.
    // ------------------------------------------------------------------ //

    /**
     * Default implementation of {@link DispatchService}.
     * Inject via the {@link DispatchService} interface (DIP).
     */
    public static final class DefaultDispatchService implements DispatchService {

        private final DeliveryPartnerService deliveryPartnerService;
        private final OrderService orderService;

        /** Constructor injection — satisfies DIP and Low Coupling (GRASP). */
        public DefaultDispatchService(DeliveryPartnerService deliveryPartnerService,
                                      OrderService orderService) {
            this.deliveryPartnerService = deliveryPartnerService;
            this.orderService = orderService;
        }

        /**
         * Assigns the first available delivery partner to the order.
         *
         * Validation rules (synopsis §5):
         *  - orderId must be non-null and exist in the system
         *  - restaurantId / customerId must be non-null
         *  - available partners list must not be null
         *  - only partners with available=true are considered
         *  - if none available → failure with "No delivery partner available"
         *  - assignment ID generated uniquely on success (IdGenerator)
         *  - deterministic: same partner list always yields same result (findFirst)
         */
        @Override
        public DispatchAssignment assignPartner(String orderId,
                                                String restaurantId,
                                                String customerId) {
            // Validate inputs (synopsis §5)
            if (orderId == null || restaurantId == null || customerId == null) {
                return DispatchAssignment.failure(orderId, "Invalid request: null fields");
            }

            // Validate order existence via OrderService (synopsis §3.1 dependency)
            if (orderService.getOrder(orderId) == null) {
                return DispatchAssignment.failure(orderId, "Order not found: " + orderId);
            }

            // Fetch available partners from DeliveryPartnerService (synopsis §3.1)
            List<DeliveryPartner> available = deliveryPartnerService.getAvailablePartners();
            if (available == null) {
                return DispatchAssignment.failure(orderId, "No delivery partner available");
            }

            // Delegate to PartnerSelector utility (synopsis §3.3)
            Optional<DeliveryPartner> selected = PartnerSelector.selectFirst(available);

            // Failure condition (synopsis §5 & §4)
            if (selected.isEmpty()) {
                return DispatchAssignment.failure(orderId, "No delivery partner available");
            }

            // Success — unique assignmentId generated via IdGenerator
            return DispatchAssignment.success(orderId, selected.get().partnerId());
        }
    }

    // ------------------------------------------------------------------ //
    //  PartnerSelector — synopsis §3.3 utility
    //
    //  SRP (SOLID): selection logic lives here, not in the service.
    //  Consistency Rule: findFirst() is deterministic — same input, same result.
    // ------------------------------------------------------------------ //

    /**
     * Utility responsible solely for partner selection logic.
     */
    static final class PartnerSelector {

        private PartnerSelector() {}

        /**
         * Selects the first available partner from the list.
         * Only partners with {@code available == true} are considered.
         *
         * @param partners list from DeliveryPartnerService (must not be null)
         * @return Optional with chosen partner, or empty if none available
         */
        static Optional<DeliveryPartner> selectFirst(List<DeliveryPartner> partners) {
            return partners.stream()
                    .filter(DeliveryPartner::available)
                    .findFirst();
        }
    }
}
