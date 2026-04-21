package edu.classproject.dispatch;

import edu.classproject.common.IdGenerator;

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
 *  - failure case → partnerId null, reason explains why
 */
public record DispatchAssignment(
        String assignmentId,
        String orderId,
        String partnerId,
        String reason) {

    /**
     * Returns a successful assignment with a unique ID.
     */
    public static DispatchAssignment success(String orderId, String partnerId) {
        return new DispatchAssignment(
                IdGenerator.nextId("ASGN"),
                orderId,
                partnerId,
                "Partner assigned successfully");
    }

    /**
     * Returns a failed assignment — partnerId is null, reason explains why.
     */
    public static DispatchAssignment failure(String orderId, String reason) {
        return new DispatchAssignment(null, orderId, null, reason);
    }

    /**
     * Convenience predicate used by callers and unit tests.
     */
    public boolean isSuccess() {
        return partnerId != null;
    }
}