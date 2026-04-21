package edu.classproject.dispatch;

import edu.classproject.delivery.DeliveryPartner;
import edu.classproject.delivery.DeliveryPartnerService;
import edu.classproject.common.Money;
import edu.classproject.order.Order;
import edu.classproject.order.OrderItem;
import edu.classproject.order.OrderRequest;
import edu.classproject.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {

    private DispatchService dispatchService;
    private StubOrderService orderService;
    private StubDeliveryPartnerService deliveryPartnerService;

    @BeforeEach
    void setUp() {
        orderService = new StubOrderService();
        deliveryPartnerService = new StubDeliveryPartnerService();
        dispatchService = new DefaultDispatchService(
                deliveryPartnerService, orderService);
    }

    @Test
    void assignPartner_success_whenPartnerAvailable() {
        orderService.orderExists = true;
        deliveryPartnerService.partners = List.of(
                new DeliveryPartner("P1", "Alice", true),
                new DeliveryPartner("P2", "Bob",   false)
        );

        DispatchAssignment result = dispatchService.assignPartner("ORD-1", "REST-1", "CUST-1");

        assertTrue(result.isSuccess());
        assertEquals("P1", result.partnerId());
        assertNotNull(result.assignmentId());
        System.out.println("[SUCCESS] " + result);
    }

    @Test
    void assignPartner_failure_whenNoPartnerAvailable() {
        orderService.orderExists = true;
        deliveryPartnerService.partners = List.of(
                new DeliveryPartner("P2", "Bob", false)
        );

        DispatchAssignment result = dispatchService.assignPartner("ORD-1", "REST-1", "CUST-1");

        assertFalse(result.isSuccess());
        assertNull(result.partnerId());
        assertEquals("No delivery partner available", result.reason());
        System.out.println("[FAILURE - no available partner] " + result);
    }

    @Test
    void assignPartner_failure_whenPartnerListEmpty() {
        orderService.orderExists = true;
        deliveryPartnerService.partners = List.of();

        DispatchAssignment result = dispatchService.assignPartner("ORD-1", "REST-1", "CUST-1");

        assertFalse(result.isSuccess());
        assertEquals("No delivery partner available", result.reason());
        System.out.println("[FAILURE - empty list] " + result);
    }

    @Test
    void assignPartner_failure_whenOrderNotFound() {
        orderService.orderExists = false;
        deliveryPartnerService.partners = List.of(
                new DeliveryPartner("P1", "Alice", true)
        );

        DispatchAssignment result = dispatchService.assignPartner("ORD-999", "REST-1", "CUST-1");

        assertFalse(result.isSuccess());
        assertTrue(result.reason().contains("Order not found"));
        System.out.println("[FAILURE - order not found] " + result);
    }

    @Test
    void assignPartner_selectsOnlyAvailablePartner() {
        orderService.orderExists = true;
        deliveryPartnerService.partners = List.of(
                new DeliveryPartner("P1", "Alice", false),
                new DeliveryPartner("P2", "Bob",   true),
                new DeliveryPartner("P3", "Carol",  true)
        );

        DispatchAssignment result = dispatchService.assignPartner("ORD-1", "REST-1", "CUST-1");

        assertTrue(result.isSuccess());
        assertEquals("P2", result.partnerId()); // first available
        System.out.println("[CORRECT SELECTION] " + result);
    }

    // ── Stubs ────────────────────────────────────────────────────────────

    static class StubOrderService implements OrderService {
        boolean orderExists = true;

        @Override
        public Order getOrder(String orderId) {
            if (!orderExists) return null;
            OrderItem stub = new OrderItem("ITEM-1", "Burger", Money.of(5.0), 1);
            return new Order(orderId, "CUST-1", "REST-1", List.of(stub));
        }

        @Override public Order placeOrder(OrderRequest r) { return null; }
        @Override public void updateStatus(String id, String s) {}
    }

    static class StubDeliveryPartnerService implements DeliveryPartnerService {
        List<DeliveryPartner> partners = List.of();

        @Override
        public List<DeliveryPartner> getAvailablePartners() { return partners; }

        @Override
        public DeliveryPartner register(String name) { return null; }

        @Override
        public void setAvailability(String partnerId, boolean available) {}
    }
}