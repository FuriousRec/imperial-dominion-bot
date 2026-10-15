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
        public boolean get() {
            return true;
        }
    }
}
