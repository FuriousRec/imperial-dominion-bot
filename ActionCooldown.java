import java.util.Objects;

/**
 * Tracks the cooldown after an action is accepted by the game engine.
 *
 * <p>A rejected action must not start a cooldown: the strategy should be able
 * to try another recovery path on the next round.</p>
 */
final class ActionCooldown {
    private long waitUntilRound = -1;
    private String waitingFor = "";

    boolean isWaiting(long currentRound) {
        return currentRound < waitUntilRound;
    }

    String waitingFor() {
        return waitingFor;
    }

    long waitUntilRound() {
        return waitUntilRound;
    }

    void recordAttempt(
            boolean scheduled,
            long currentRound,
            long delayRounds,
            String label) {

        if (!scheduled) {
            clear();
            return;
        }

        if (delayRounds < 0) {
            throw new IllegalArgumentException(
                    "delayRounds must not be negative");
        }

        waitingFor = Objects.requireNonNull(
                label,
                "label must not be null");
        waitUntilRound = Math.addExact(
                currentRound,
                delayRounds);
    }

    void clear() {
        waitingFor = "";
        waitUntilRound = -1;
    }
}
