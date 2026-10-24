import java.util.ArrayList;
import java.util.List;

final class RocketReadiness {

    enum Blocker {
        LAB_REQUIRED,
        ROCKET_ALREADY_BUILT,
        BUILDING_SLOT_REQUIRED,
        CURRENCY_BELOW_8000,
        ENERGY_BELOW_300,
        IRON_BELOW_200,
        WOOD_BELOW_100,
        AVAILABLE_WORKERS_BELOW_30
    }

    record Snapshot(
            int labs,
            int rocketScience,
            int buildingSlots,
            int currency,
            int energy,
            int iron,
            int wood,
            int availableWorkers) {
    }

    private RocketReadiness() {
    }

    static boolean isReady(Snapshot resources) {
        return blockers(resources).isEmpty();
    }

    static List<Blocker> blockers(Snapshot resources) {
        List<Blocker> blockers = new ArrayList<>();

        if (resources.labs() < 1) {
            blockers.add(Blocker.LAB_REQUIRED);
        }
        if (resources.rocketScience() >= 1) {
            blockers.add(Blocker.ROCKET_ALREADY_BUILT);
        }
        if (resources.buildingSlots() < 1) {
            blockers.add(Blocker.BUILDING_SLOT_REQUIRED);
        }
        if (resources.currency() < 8000) {
            blockers.add(Blocker.CURRENCY_BELOW_8000);
        }
        if (resources.energy() < 300) {
            blockers.add(Blocker.ENERGY_BELOW_300);
        }
        if (resources.iron() < 200) {
            blockers.add(Blocker.IRON_BELOW_200);
        }
        if (resources.wood() < 100) {
            blockers.add(Blocker.WOOD_BELOW_100);
        }
        if (resources.availableWorkers() < 30) {
            blockers.add(Blocker.AVAILABLE_WORKERS_BELOW_30);
        }

        return List.copyOf(blockers);
    }
}
