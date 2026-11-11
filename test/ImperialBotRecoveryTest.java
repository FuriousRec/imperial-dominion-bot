import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.athron.gitlab.imperial_dominion.Bot;
import de.athron.gitlab.imperial_dominion.StateInterface;
import de.athron.gitlab.imperial_dominion.jni.libgame.RecipeName;
import de.athron.gitlab.imperial_dominion.jni.libgame.ResourceName;

public final class ImperialBotRecoveryTest {
    private int assertions;

    public static void main(String[] args) {
        ImperialBotRecoveryTest test = new ImperialBotRecoveryTest();
        test.doesNotRepeatAcceptedEmergencyActionDuringCooldown();
        test.initClearsAcceptedActionCooldown();
        test.initResetsStrategyStage();
        test.doesNotScheduleActionsAfterCompletion();
        test.doesNotScheduleActionsAfterFailure();
        System.out.println(
                "ImperialBotRecoveryTest: "
                        + test.assertions
                        + " assertions passed");
    }

    private void doesNotRepeatAcceptedEmergencyActionDuringCooldown() {
        ImperialBot bot = new ImperialBot();
        FakeState state = new FakeState();
        state.set(ResourceName.WORKER_WITHOUT_JOB, 0);
        state.set(ResourceName.WORKER_WITH_JOB, 11);

        state.round = 0;
        bot.executeTurn(state, new Bot());

        equal(1, state.actions.size(), "first recovery action count");
        equal(RecipeName.FIRE_WORKER, state.actions.get(0), "recovery recipe");

        for (long round = 1; round < 5; round++) {
            state.round = round;
            bot.executeTurn(state, new Bot());
            equal(1, state.actions.size(), "action count during cooldown at round " + round);
        }

        state.round = 5;
        bot.executeTurn(state, new Bot());
        equal(2, state.actions.size(), "recovery may be retried when cooldown expires");
    }

    private void initClearsAcceptedActionCooldown() {
        ImperialBot bot = new ImperialBot();
        FakeState firstGame = emergencyState();

        bot.executeTurn(firstGame, new Bot());
        equal(1, firstGame.actions.size(), "first game recovery action count");

        bot.init(new Bot());

        FakeState secondGame = emergencyState();
        secondGame.round = 0;
        bot.executeTurn(secondGame, new Bot());

        equal(1, secondGame.actions.size(),
                "new game must not inherit the previous action cooldown");
        equal(RecipeName.FIRE_WORKER, secondGame.actions.get(0),
                "new game recovery recipe");
    }

    private void initResetsStrategyStage() {
        ImperialBot bot = new ImperialBot();
        FakeState progressedGame = new FakeState();
        progressedGame.set(ResourceName.WORKER_WITHOUT_JOB, 1);
        progressedGame.set(ResourceName.WORKER_WITH_JOB, 12);

        bot.executeTurn(progressedGame, new Bot());
        equal(0, progressedGame.actions.size(),
                "progressed game reaches the wind-power stage without an action");

        bot.init(new Bot());

        FakeState secondGame = new FakeState();
        secondGame.set(ResourceName.WORKER_WITHOUT_JOB, 6);
        secondGame.set(ResourceName.WORKER_WITH_JOB, 0);
        secondGame.set(ResourceName.CURRENCY, 50);
        bot.executeTurn(secondGame, new Bot());

        equal(1, secondGame.actions.size(),
                "new game must start from the worker-hiring stage");
        equal(RecipeName.HIRE_WORKER, secondGame.actions.get(0),
                "new game opening recipe");
    }

    private void doesNotScheduleActionsAfterCompletion() {
        FakeState state = emergencyState();
        state.complete = true;

        new ImperialBot().executeTurn(state, new Bot());

        equal(0, state.actions.size(), "completed game must not schedule recovery");
    }

    private void doesNotScheduleActionsAfterFailure() {
        FakeState state = emergencyState();
        state.failed = true;

        new ImperialBot().executeTurn(state, new Bot());

        equal(0, state.actions.size(), "failed game must not schedule recovery");
    }

    private FakeState emergencyState() {
        FakeState state = new FakeState();
        state.set(ResourceName.WORKER_WITHOUT_JOB, 0);
        state.set(ResourceName.WORKER_WITH_JOB, 11);
        state.round = 1;
        return state;
    }

    private void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    message + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class FakeState implements StateInterface {
        private final Map<ResourceName, Integer> resources =
                new EnumMap<>(ResourceName.class);
        private final List<RecipeName> actions = new ArrayList<>();
        private long round;
        private boolean complete;
        private boolean failed;

        void set(ResourceName resource, int value) {
            resources.put(resource, value);
        }

        @Override
        public long getRound() {
            return round;
        }

        @Override
        public int resource(ResourceName resource) {
            return resources.getOrDefault(resource, 0);
        }

        @Override
        public int estimateProduction(ResourceName resource) {
            return 0;
        }

        @Override
        public int estimateConsumption(ResourceName resource) {
            return 0;
        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public boolean isFailed() {
            return failed;
        }

        @Override
        public ActionResult scheduleAction(RecipeName recipe, int amount) {
            if (amount != 1) {
                throw new AssertionError("expected a single scheduled action");
            }
            actions.add(recipe);
            return new ActionResult();
        }
    }
}
