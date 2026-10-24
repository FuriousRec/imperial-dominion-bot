import java.util.List;

public final class RocketReadinessTest {
    private int assertions;

    public static void main(String[] args) {
        RocketReadinessTest test = new RocketReadinessTest();
        test.acceptsExactRequirementBoundaries();
        test.rejectsEveryMissingRequirement();
        test.requiresAvailableWorkersRatherThanWorkerHeadcount();
        test.reportsAllBlockersInStableOrder();
        System.out.println(
                "RocketReadinessTest: "
                        + test.assertions
                        + " assertions passed");
    }

    private void acceptsExactRequirementBoundaries() {
        RocketReadiness.Snapshot ready = ready();

        check(RocketReadiness.isReady(ready), "exact thresholds must be ready");
        equal(List.of(), RocketReadiness.blockers(ready), "ready blockers");
    }

    private void rejectsEveryMissingRequirement() {
        List<RocketReadiness.Snapshot> snapshots = List.of(
                new RocketReadiness.Snapshot(0, 0, 1, 8000, 300, 200, 100, 30),
                new RocketReadiness.Snapshot(1, 1, 1, 8000, 300, 200, 100, 30),
                new RocketReadiness.Snapshot(1, 0, 0, 8000, 300, 200, 100, 30),
                new RocketReadiness.Snapshot(1, 0, 1, 7999, 300, 200, 100, 30),
                new RocketReadiness.Snapshot(1, 0, 1, 8000, 299, 200, 100, 30),
                new RocketReadiness.Snapshot(1, 0, 1, 8000, 300, 199, 100, 30),
                new RocketReadiness.Snapshot(1, 0, 1, 8000, 300, 200, 99, 30),
                new RocketReadiness.Snapshot(1, 0, 1, 8000, 300, 200, 100, 29));
        List<RocketReadiness.Blocker> expected = List.of(
                RocketReadiness.Blocker.LAB_REQUIRED,
                RocketReadiness.Blocker.ROCKET_ALREADY_BUILT,
                RocketReadiness.Blocker.BUILDING_SLOT_REQUIRED,
                RocketReadiness.Blocker.CURRENCY_BELOW_8000,
                RocketReadiness.Blocker.ENERGY_BELOW_300,
                RocketReadiness.Blocker.IRON_BELOW_200,
                RocketReadiness.Blocker.WOOD_BELOW_100,
                RocketReadiness.Blocker.AVAILABLE_WORKERS_BELOW_30);

        for (int index = 0; index < snapshots.size(); index++) {
            check(!RocketReadiness.isReady(snapshots.get(index)),
                    "missing requirement " + expected.get(index));
            equal(List.of(expected.get(index)),
                    RocketReadiness.blockers(snapshots.get(index)),
                    "single blocker " + expected.get(index));
        }
    }

    private void requiresAvailableWorkersRatherThanWorkerHeadcount() {
        RocketReadiness.Snapshot onlyTwentyNineAvailable =
                new RocketReadiness.Snapshot(
                        1, 0, 1, 8000, 300, 200, 100, 29);

        check(!RocketReadiness.isReady(onlyTwentyNineAvailable),
                "busy employed workers cannot pay the 30-worker recipe cost");
        check(RocketReadiness.blockers(onlyTwentyNineAvailable).contains(
                        RocketReadiness.Blocker.AVAILABLE_WORKERS_BELOW_30),
                "available-worker blocker");
    }

    private void reportsAllBlockersInStableOrder() {
        RocketReadiness.Snapshot blocked =
                new RocketReadiness.Snapshot(
                        0, 0, 1, 7999, 300, 200, 100, 29);

        equal(List.of(
                        RocketReadiness.Blocker.LAB_REQUIRED,
                        RocketReadiness.Blocker.CURRENCY_BELOW_8000,
                        RocketReadiness.Blocker.AVAILABLE_WORKERS_BELOW_30),
                RocketReadiness.blockers(blocked),
                "multiple blockers");
    }

    private RocketReadiness.Snapshot ready() {
        return new RocketReadiness.Snapshot(
                1, 0, 1, 8000, 300, 200, 100, 30);
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
