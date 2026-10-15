package de.athron.gitlab.imperial_dominion;

import java.util.Collections;
import de.athron.gitlab.imperial_dominion.jni.libgame.RecipeName;
import de.athron.gitlab.imperial_dominion.jni.libgame.ResourceName;

public class Bot {
    public Iterable<RecipeItem> recipeCost(RecipeName recipe) {
        return Collections.emptyList();
    }

    public Iterable<RecipeItem> recipeOutcome(RecipeName recipe) {
        return Collections.emptyList();
    }

    public static final class RecipeItem {
        public ResourceName first() {
            return null;
        }

        public int second() {
            return 0;
        }
    }
}
