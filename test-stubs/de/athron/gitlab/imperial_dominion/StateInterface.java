package de.athron.gitlab.imperial_dominion;

import de.athron.gitlab.imperial_dominion.jni.libgame.RecipeName;
import de.athron.gitlab.imperial_dominion.jni.libgame.ResourceName;

public interface StateInterface {
    long getRound();
    int resource(ResourceName resource);
    int estimateProduction(ResourceName resource);
    int estimateConsumption(ResourceName resource);
    boolean isComplete();
    boolean isFailed();
    ActionResult scheduleAction(RecipeName recipe, int amount);

    final class ActionResult {
        private final boolean accepted;

        public ActionResult() {
            this(true);
        }

        public ActionResult(boolean accepted) {
            this.accepted = accepted;
        }

        public boolean get() {
            return accepted;
        }
    }
}
