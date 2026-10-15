public final class ActionCooldownTest {
    private int assertions;

    public static void main(String[] args) {
        ActionCooldownTest test = new ActionCooldownTest();
        test.startsIdle();
        test.successfulAttemptStartsBoundedCooldown();
        test.failedAttemptClearsStaleCooldown();
        test.clearResetsState();
        test.rejectsNegativeDelay();
        System.out.println(
                "ActionCooldownTest: "
                        + test.assertions
                        + " assertions passed");
    }

    private void startsIdle() {
        ActionCooldown cooldown = new ActionCooldown();

        check(!cooldown.isWaiting(0), "new cooldown must be idle");
        equal("", cooldown.waitingFor(), "new label");
        equal(-1L, cooldown.waitUntilRound(), "new deadline");
    }

    private void successfulAttemptStartsBoundedCooldown() {
        ActionCooldown cooldown = new ActionCooldown();

        cooldown.recordAttempt(true, 40, 5, "BUILD_FARM");

        check(cooldown.isWaiting(40), "wait starts immediately");
        check(cooldown.isWaiting(44), "wait covers intermediate rounds");
        check(!cooldown.isWaiting(45), "wait ends at the deadline");
        equal("BUILD_FARM", cooldown.waitingFor(), "accepted label");
        equal(45L, cooldown.waitUntilRound(), "accepted deadline");
    }

    private void failedAttemptClearsStaleCooldown() {
        ActionCooldown cooldown = new ActionCooldown();
        cooldown.recordAttempt(true, 10, 5, "BUILD_HOUSE");

        cooldown.recordAttempt(false, 15, 5, "BUILD_LAB");

        check(!cooldown.isWaiting(15), "rejected action must not wait");
        equal("", cooldown.waitingFor(), "rejected action label");
        equal(-1L, cooldown.waitUntilRound(), "rejected action deadline");
    }

    private void clearResetsState() {
        ActionCooldown cooldown = new ActionCooldown();
        cooldown.recordAttempt(true, 100, 2, "DELETE_FARM");

        cooldown.clear();

        check(!cooldown.isWaiting(100), "cleared cooldown must be idle");
        equal("", cooldown.waitingFor(), "cleared label");
        equal(-1L, cooldown.waitUntilRound(), "cleared deadline");
    }

    private void rejectsNegativeDelay() {
        ActionCooldown cooldown = new ActionCooldown();

        try {
            cooldown.recordAttempt(true, 0, -1, "INVALID");
            throw new AssertionError("negative delay was accepted");
        } catch (IllegalArgumentException expected) {
            assertions++;
        }
    }

    private void check(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    message
                            + ": expected "
                            + expected
                            + ", got "
                            + actual);
        }
    }
}
