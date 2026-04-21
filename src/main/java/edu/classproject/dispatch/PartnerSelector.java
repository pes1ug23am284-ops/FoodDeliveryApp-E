package edu.classproject.dispatch;

import edu.classproject.delivery.DeliveryPartner;

import java.util.List;
import java.util.Optional;

/**
 * Utility responsible solely for partner selection logic.
 * 
 * Design patterns applied:
 *  - SRP (SOLID): selection logic lives here, not in the service.
 *  - Consistency Rule: findFirst() is deterministic — same input, same result.
 */
public class PartnerSelector {

    /**
     * Selects the first available partner from the list.
     * Only partners with {@code available == true} are considered.
     *
     * @param partners list from DeliveryPartnerService (must not be null)
     * @return Optional with chosen partner, or empty if none available
     */
    public Optional<DeliveryPartner> selectFirst(List<DeliveryPartner> partners) {
        return partners.stream()
                .filter(DeliveryPartner::available)
                .findFirst();
    }
}