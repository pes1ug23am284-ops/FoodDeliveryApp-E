package edu.classproject.dispatch;

import edu.classproject.delivery.DeliveryPartner;
import edu.classproject.delivery.DeliveryPartnerService;
import edu.classproject.order.OrderService;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link DispatchService}.
 * 
 * Design patterns applied:
 *  - DIP (SOLID): depends on DeliveryPartnerService & OrderService
 *    abstractions, never on concrete classes.
 *  - Low Coupling (GRASP): wired via constructor injection.
 *  - SRP (SOLID): only responsible for orchestrating dispatch.
 */
public class DefaultDispatchService implements DispatchService {

    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderService orderService;
    private final PartnerSelector partnerSelector;

    /**
     * Constructor injection — satisfies DIP and Low Coupling (GRASP).
     */
    public DefaultDispatchService(DeliveryPartnerService deliveryPartnerService,
                                  OrderService orderService) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.orderService = orderService;
        this.partnerSelector = new PartnerSelector();
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
        Optional<DeliveryPartner> selected = partnerSelector.selectFirst(available);

        // Failure condition (synopsis §5 & §4)
        if (selected.isEmpty()) {
            return DispatchAssignment.failure(orderId, "No delivery partner available");
        }

        // Success — unique assignmentId generated via IdGenerator
        return DispatchAssignment.success(orderId, selected.get().partnerId());
    }
}