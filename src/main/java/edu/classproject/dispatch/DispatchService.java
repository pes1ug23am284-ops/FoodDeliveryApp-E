package edu.classproject.dispatch;

/**
 * Contract for the Dispatch Assignment module.
 *
 * Design patterns applied:
 *  - ISP / SRP (SOLID): single, focused method — no unrelated concerns.
 *  - DIP (SOLID): all callers depend on this abstraction; the concrete
 *    implementation lives in DispatchAssignment.DefaultDispatchService.
 *  - GRASP Information Expert: the interface defines what the module
 *    knows how to do (assign a partner) without exposing how.
 */
public interface DispatchService {

    /**
     * Assigns an available delivery partner to the given order.
     *
     * @param orderId      non-null; must correspond to an existing order
     * @param restaurantId non-null; must be valid for the order
     * @param customerId   non-null; must be associated with a valid user
     * @return {@link DispatchAssignment} with partnerId set on success,
     *         or partnerId=null with a failure reason (synopsis §4)
     */
    DispatchAssignment assignPartner(String orderId, String restaurantId, String customerId);
}
