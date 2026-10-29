// SPDX-FileCopyrightText: 2026 Guth, Hartula, Haß, Helke, Kurek, Stope, Thron
// SPDX-License-Identifier: LicenseRef-Proprietary
// All rights reserved. See LICENSE for terms and contact information.

import de.athron.gitlab.imperial_dominion.Bot;
import de.athron.gitlab.imperial_dominion.BotInterface;
import de.athron.gitlab.imperial_dominion.StateInterface;
import de.athron.gitlab.imperial_dominion.jni.libgame.ResourceName;
import de.athron.gitlab.imperial_dominion.jni.libgame.RecipeName;

public class ImperialBot implements BotInterface {
    public int getMatrikelNumber() {
        return 0; // Sanitized for public portfolio version.
    }

    public String getStudentName() {
        return "Francis Mathew";
    }

    public String getBotName() {
        return "ImperialBot";
    }

    // ANCHOR: STAGE CONTROL VARIABLES
    private int stage = 0;

    private final ActionCooldown actionCooldown = new ActionCooldown();

    public void init(Bot bot) {
        printRecipe(bot, RecipeName.HIRE_WORKER);
        printRecipe(bot, RecipeName.BUILD_HOUSE);
        printRecipe(bot, RecipeName.BUILD_WIND_POWER);
        printRecipe(bot, RecipeName.BUILD_WATER_POWER);
        printRecipe(bot, RecipeName.BUILD_SOLAR_POWER);
        printRecipe(bot, RecipeName.BUILD_SAWMILL);
        printRecipe(bot, RecipeName.BUILD_IRON_MINE);
        printRecipe(bot, RecipeName.CONVERT_RAW_IRON);
        printRecipe(bot, RecipeName.BUILD_FARM);
        printRecipe(bot, RecipeName.BUILD_AGRICULTUR);
        printRecipe(bot, RecipeName.BUILD_4FLOOR_BUILDING);
        printRecipe(bot, RecipeName.BUILD_10FLOOR_BUILDING);
        printRecipe(bot, RecipeName.BUILD_UNIVERSITY);
        printRecipe(bot, RecipeName.BUILD_LAB);
        printRecipe(bot, RecipeName.BUILD_ROCKET_SCIENCE);
        printRecipe(bot, RecipeName.DELETE_UNIVERSITY);
        printRecipe(bot, RecipeName.DELETE_WATER_POWER);
        printRecipe(bot, RecipeName.DELETE_WIND_POWER);
        printRecipe(bot, RecipeName.DELETE_FARM);
        printRecipe(bot, RecipeName.FIRE_WORKER);
    }

    public void executeTurn(StateInterface state, Bot bot) {
        // ANCHOR: PRINT BLOCK
        if (state.getRound() % 5 == 0) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("ROUND " + state.getRound());
            System.out.println("========================================");

            System.out.println("STAGE: " + stage);
            System.out.println("WAITING_FOR: " + actionCooldown.waitingFor());

            System.out.println("ENERGY: " + state.resource(ResourceName.ENERGY));
            System.out.println("FOOD: " + state.resource(ResourceName.FOOD));
            System.out.println("CURRENCY: " + state.resource(ResourceName.CURRENCY));

            System.out.println("WOOD: " + state.resource(ResourceName.WOOD));
            System.out.println("SAWMILL: " + state.resource(ResourceName.SAWMILL));
            System.out.println("WOOD production: " + state.estimateProduction(ResourceName.WOOD));
            System.out.println("WOOD consumption: " + state.estimateConsumption(ResourceName.WOOD));

            System.out.println("IRON: " + state.resource(ResourceName.IRON));
            System.out.println("RAW_IRON: " + state.resource(ResourceName.RAW_IRON));
            System.out.println("IRON_MINE: " + state.resource(ResourceName.IRON_MINE));
            System.out.println("MINING_SLOTS: " + state.resource(ResourceName.MINING_SLOTS));

            System.out.println("UNIVERSITY: " + state.resource(ResourceName.UNIVERSITY));
            System.out.println("LAB: " + state.resource(ResourceName.LAB));
            System.out.println("ROCKET_SCIENCE: " + state.resource(ResourceName.ROCKET_SCIENCE));

            System.out.println("FARM: " + state.resource(ResourceName.FARM));
            System.out.println("FOOD production: " + state.estimateProduction(ResourceName.FOOD));
            System.out.println("FOOD consumption: " + state.estimateConsumption(ResourceName.FOOD));

            System.out.println("HOUSE: " + state.resource(ResourceName.HOUSE));
            System.out.println("WIND_POWER_PLANT: " + state.resource(ResourceName.WIND_POWER_PLANT));
            System.out.println("SOLAR_POWER_PLANT: " + state.resource(ResourceName.SOLAR_POWER_PLANT));

            System.out.println("BUILDING_SLOTS: " + state.resource(ResourceName.BUILDING_SLOTS));
            System.out.println("WATER_BUILDING: " + state.resource(ResourceName.WATER_BUILDING));
            System.out.println("WATER_POWER_PLANT: " + state.resource(ResourceName.WATER_POWER_PLANT));
            System.out.println("TEN_STORY_BUILDING: " + state.resource(ResourceName.TEN_STORY_BUILDING));
            System.out.println("APARTMENTS_AVAILABLE: " + state.resource(ResourceName.APARTMENTS_AVAILABLE));

            System.out.println("WORKER_WITH_JOB: " + state.resource(ResourceName.WORKER_WITH_JOB));
            System.out.println("WORKER_WITHOUT_JOB: " + state.resource(ResourceName.WORKER_WITHOUT_JOB));
            System.out.println("FREE_WORKERS_ESTIMATE: " + freeWorkers(state));

            System.out.println("ENERGY production: " + state.estimateProduction(ResourceName.ENERGY));
            System.out.println("ENERGY consumption: " + state.estimateConsumption(ResourceName.ENERGY));

            System.out.println("WORKER production: " + state.estimateProduction(ResourceName.WORKER_WITH_JOB));
            System.out.println("WORKER consumption: " + state.estimateConsumption(ResourceName.WORKER_WITH_JOB));

            System.out.println("COMPLETE: " + state.isComplete());
            System.out.println("FAILED: " + state.isFailed());
        }

        // ANCHOR: ACCEPTED ACTION WAIT BLOCK
        // Apply the cooldown before every recovery or stage action. In particular,
        // an accepted emergency FIRE_WORKER action may take several rounds to settle;
        // resubmitting it early can fire more workers than the strategy intended.
        if (actionCooldown.isWaiting(state.getRound())) {
            return;
        }

        // ANCHOR: SAFETY BLOCK
        if (state.resource(ResourceName.WORKER_WITHOUT_JOB) < 1) {
            // In the science sprint the key recipes spend WORKER_WITH_JOB, not idle
            // workers. Do not keep firing workers once we already have enough free
            // employed workers to start the University/Lab/Rocket chain.
            if (stage >= 21) {
                // Late science recipes do not need idle workers. Let the normal stage
                // logic continue instead of firing workers.
            } else if (state.resource(ResourceName.WORKER_WITH_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.FIRE_WORKER, "EMERGENCY_FIRE_WORKER_RECOVERY");
                return;
            } else {
                System.out.println("Safety stop: WORKER_WITHOUT_JOB below 1 and cannot recover.");
                return;
            }
        }

        // ANCHOR: STAGE UPDATE BLOCK
        updateStage(state);

        // ANCHOR :HIRE WORKER BLOCK
        if (stage == 0) {
            if (state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 12) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_0_HIRE_WORKER");
                return;
            }
            return;
        }

        // ANCHOR: WIND POWER BLOCK
        if (stage == 1) {
            if (state.resource(ResourceName.WIND_POWER_PLANT) < 4 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 120 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 2) {
                tryScheduleBuilding(state, RecipeName.BUILD_WIND_POWER, "STAGE_1_BUILD_WIND_POWER");
                return;
            }
            return;
        }

        // ANCHOR: EXTRA HIRE WORKER BLOCK
        if (stage == 2) {
            if (state.resource(ResourceName.WORKER_WITHOUT_JOB) > 2 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 18) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_2_EXTRA_HIRE_WORKER");
                return;
            }
            return;
        }

        // ANCHOR: HOUSE BLOCK
        if (stage == 3) {
            if (state.resource(ResourceName.HOUSE) < 6 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.ENERGY) >= 20 &&
                    state.resource(ResourceName.WOOD) >= 25 &&
                    freeWorkers(state) >= 3) {
                tryScheduleBuilding(state, RecipeName.BUILD_HOUSE, "STAGE_3_BUILD_HOUSE");
                return;
            }
            return;
        }

        // ANCHOR: SAWMILL BLOCK
        if (stage == 4) {
            if (state.resource(ResourceName.SAWMILL) < 1 &&
                    state.resource(ResourceName.WOOD) >= 10 &&
                    state.resource(ResourceName.CURRENCY) >= 60 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 4) {
                tryScheduleBuilding(state, RecipeName.BUILD_SAWMILL, "STAGE_4_BUILD_SAWMILL");
                return;
            }
            return;
        }

        // ANCHOR: POST SAWMILL HIRE BLOCK
        if (stage == 5) {
            if (state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 22) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_5_POST_SAWMILL_HIRE_WORKER");
                return;
            }
            return;
        }

        // ANCHOR: PRE IRON HOUSE BLOCK
        if (stage == 6) {
            if (state.resource(ResourceName.HOUSE) < 7 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.ENERGY) >= 20 &&
                    state.resource(ResourceName.WOOD) >= 15 &&
                    freeWorkers(state) >= 3) {
                tryScheduleBuilding(state, RecipeName.BUILD_HOUSE, "STAGE_6_PRE_IRON_BUILD_HOUSE");
                return;
            }
            return;
        }

        // ANCHOR: POST HOUSE 7 HIRE BLOCK
        if (stage == 7) {
            if (state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 24) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_7_POST_HOUSE_7_HIRE_WORKER");
                return;
            }
            return;
        }

        // ANCHOR: FARM BLOCK
        if (stage == 8) {
            if (state.estimateProduction(ResourceName.FOOD) < state.estimateConsumption(ResourceName.FOOD) + 40 &&
                    state.resource(ResourceName.FARM) < 4 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_FARM, "STAGE_8_BUILD_FARM");
                return;
            }
            return;
        }

        // ANCHOR: IRON MINE BLOCK
        if (stage == 9) {
            if (state.resource(ResourceName.IRON_MINE) < 1 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 8 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 26 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_9_PRE_IRON_MINE_HIRE_WORKER");
                return;
            }

            if (state.resource(ResourceName.IRON_MINE) < 1 &&
                    state.resource(ResourceName.MINING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 150 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 8 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleBuilding(state, RecipeName.BUILD_IRON_MINE, "STAGE_9_BUILD_IRON_MINE");
                return;
            }
            return;
        }

        // ANCHOR: POST IRON MINE HIRE BLOCK
        if (stage == 10) {
            if (state.resource(ResourceName.IRON_MINE) >= 1 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 8 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 30 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_10_POST_IRON_MINE_HIRE_WORKER");
                return;
            }
            return;
        }

        // ANCHOR: CONVERT RAW IRON BLOCK
        if (stage == 11) {
            if (state.resource(ResourceName.IRON) < 300 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_11_CONVERT_RAW_IRON");
                return;
            }
            return;
        }

        // ANCHOR: ROCKET PREP HIRE BLOCK
        if (stage == 12) {
            if (state.resource(ResourceName.WORKER_WITHOUT_JOB) > 3 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) < 34) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_12_ROCKET_PREP_HIRE");
                return;
            }
            return;
        }

        // ANCHOR: ROCKET ENERGY BLOCK
        if (stage == 13) {
            if (state.resource(ResourceName.WIND_POWER_PLANT) < 5 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 120 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 2) {
                tryScheduleBuilding(state, RecipeName.BUILD_WIND_POWER, "STAGE_13_BUILD_WIND_POWER_5");
                return;
            }
            return;
        }

        // ANCHOR: APARTMENT EXPANSION BLOCK
        if (stage == 14) {
            if (state.resource(ResourceName.APARTMENTS_AVAILABLE) < 70 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 200 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 30 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_4FLOOR_BUILDING, "STAGE_14_BUILD_4FLOOR_BUILDING");
                return;
            }
            return;
        }

        // ANCHOR: POST APARTMENT ENERGY BLOCK
        if (stage == 15) {
            if (state.resource(ResourceName.WIND_POWER_PLANT) < 8 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 120 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 2) {
                tryScheduleBuilding(state, RecipeName.BUILD_WIND_POWER, "STAGE_15_POST_APARTMENT_WIND_POWER");
                return;
            }
            return;
        }

        // ANCHOR: FINAL FOOD BLOCK
        if (stage == 16) {
            if (freeWorkers(state) < 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_16_FREE_WORKER_HIRE");
                return;
            }

            if (state.estimateProduction(ResourceName.FOOD) < state.estimateConsumption(ResourceName.FOOD) + 50 &&
                    state.resource(ResourceName.FARM) < 8 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 3) {
                tryScheduleBuilding(state, RecipeName.BUILD_FARM, "STAGE_16_BUILD_EXTRA_FARM");
                return;
            }
            return;
        }

        // ANCHOR: FINAL IRON TOP UP BLOCK
        if (stage == 17) {
            if (state.resource(ResourceName.IRON) < 500 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 1) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_17_TOP_UP_IRON");
                return;
            }
            return;
        }

        // ANCHOR: SAFE APARTMENT EXPANSION BLOCK
        if (stage == 18) {
            if (state.resource(ResourceName.APARTMENTS_AVAILABLE) < 130 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 1 &&
                    state.resource(ResourceName.CURRENCY) >= 200 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 30 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 3) {
                tryScheduleBuilding(state, RecipeName.BUILD_4FLOOR_BUILDING, "STAGE_18_BUILD_4FLOOR");
                return;
            }

            if (state.resource(ResourceName.APARTMENTS_AVAILABLE) < 130 &&
                    freeWorkers(state) < 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 8 &&
                    state.resource(ResourceName.CURRENCY) >= 50) {
                tryScheduleHire(state, "STAGE_18_PRE_4FLOOR_HIRE");
                return;
            }
            return;
        }

        // ANCHOR: WATER + SAFE ENERGY BLOCK
        // Stage 19: use water power, but do not get stuck trying to overbuild it.
        if (stage == 19) {
            // Force the first water power plant.
            if (state.resource(ResourceName.WATER_BUILDING) > 0 &&
                    state.resource(ResourceName.WATER_POWER_PLANT) < 1 &&
                    freeWorkers(state) < 10 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 15 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 500) {
                tryScheduleHire(state, "STAGE_19_PRE_FIRST_WATER_HIRE");
                return;
            }

            if (state.resource(ResourceName.WATER_BUILDING) > 0 &&
                    state.resource(ResourceName.WATER_POWER_PLANT) < 1 &&
                    state.resource(ResourceName.CURRENCY) >= 300 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 30 &&
                    state.resource(ResourceName.WOOD) >= 30 &&
                    freeWorkers(state) >= 10 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_WATER_POWER, "STAGE_19_BUILD_FIRST_WATER_POWER");
                return;
            }

            // Optional second water plant only if it is naturally easy.
            if (state.resource(ResourceName.WATER_BUILDING) > 0 &&
                    state.resource(ResourceName.WATER_POWER_PLANT) < 2 &&
                    state.resource(ResourceName.CURRENCY) >= 300 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 30 &&
                    state.resource(ResourceName.WOOD) >= 30 &&
                    freeWorkers(state) >= 10 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 30 &&
                    state.resource(ResourceName.FOOD) > 1200 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleBuilding(state, RecipeName.BUILD_WATER_POWER, "STAGE_19_BUILD_OPTIONAL_SECOND_WATER_POWER");
                return;
            }

            if (state.estimateProduction(ResourceName.ENERGY) < state.estimateConsumption(ResourceName.ENERGY) + 30 &&
                    state.resource(ResourceName.WIND_POWER_PLANT) < 10 &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 6 &&
                    state.resource(ResourceName.CURRENCY) >= 120 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 2 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_WIND_POWER, "STAGE_19_BUILD_BACKUP_WIND_POWER");
                return;
            }
            return;
        }

        // ANCHOR: ENDGAME FOOD STABILITY BLOCK
        // Stage 20: prepare just enough farms and free workers for the next stage.
        // Do not force 12 farms here. That caused a deadlock.
        if (stage == 20) {

            // 1) If free workers are too low, hire carefully.
            if (freeWorkers(state) < 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 20 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 250) {
                tryScheduleHire(state, "STAGE_20_PRE_FARM_HIRE");
                return;
            }

            // 2) Get at least 8 farms.
            if (state.resource(ResourceName.FARM) < 8 &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 6 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_FARM, "STAGE_20_BUILD_ENDGAME_FARM");
                return;
            }

            // 3) If food is still weak, build one extra farm.
            if (state.resource(ResourceName.FARM) < 9 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) + 10 &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 6 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_FARM, "STAGE_20_EXTRA_FOOD_FIX");
                return;
            }

            // 4) Prepare enough free workers for 10-floor in Stage 21.
            if (state.resource(ResourceName.FARM) >= 8 &&
                    freeWorkers(state) < 12 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 20 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 250) {
                tryScheduleHire(state, "STAGE_20_PRE_10FLOOR_HIRE");
                return;
            }

            return;
        }

        // ANCHOR: PRE UNIVERSITY APARTMENT BLOCK
        // Stage 21: prepare energy BEFORE the 10-floor building.
        // The 10-floor building is useful for apartments, but it adds a large energy
        // burden.
        // So we build one solar bridge first, then the 10-floor, then a moderate worker
        // buffer.
        if (stage == 21) {

            // 1) If free workers are too low for solar, hire one carefully.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) < 1 &&
                    state.resource(ResourceName.SOLAR_POWER_PLANT) < 1 &&
                    state.estimateProduction(ResourceName.ENERGY) < state.estimateConsumption(ResourceName.ENERGY) + 100
                    &&
                    freeWorkers(state) < 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 20 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 500) {
                tryScheduleHire(state, "STAGE_21_PRE_SOLAR_HIRE");
                return;
            }

            // 2) Build one solar plant BEFORE the 10-floor building.
            // This avoids the old Stage 22 trap where energy became negative after
            // 10-floor.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) < 1 &&
                    state.resource(ResourceName.SOLAR_POWER_PLANT) < 1 &&
                    state.estimateProduction(ResourceName.ENERGY) < state.estimateConsumption(ResourceName.ENERGY) + 100
                    &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 6 &&
                    state.resource(ResourceName.CURRENCY) >= 400 &&
                    state.resource(ResourceName.ENERGY) >= 25 &&
                    state.resource(ResourceName.IRON) >= 370 &&
                    state.resource(ResourceName.WOOD) >= 55 &&
                    freeWorkers(state) >= 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_SOLAR_POWER, "STAGE_21_BUILD_SOLAR_BEFORE_10FLOOR");
                return;
            }

            // 3) If free workers are too low for the 10-floor building, hire one carefully.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) < 1 &&
                    freeWorkers(state) < 12 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 10 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 500) {
                tryScheduleHire(state, "STAGE_21_PRE_10FLOOR_HIRE");
                return;
            }

            // 4) Build the 10-floor building only after the energy bridge is ready.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) < 1 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 5 &&
                    state.resource(ResourceName.CURRENCY) >= 900 &&
                    state.resource(ResourceName.ENERGY) >= 1000 &&
                    state.resource(ResourceName.IRON) >= 50 &&
                    state.resource(ResourceName.WOOD) >= 40 &&
                    state.estimateProduction(ResourceName.ENERGY) >= state.estimateConsumption(ResourceName.ENERGY) + 40
                    &&
                    freeWorkers(state) >= 12 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_10FLOOR_BUILDING, "STAGE_21_BUILD_10FLOOR_FOR_ROCKET");
                return;
            }

            // 5) After the 10-floor, fix food only if it is weak.
            // Keep at least 4 slots: University, Lab, Rocket Science, and one buffer.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) + 20 &&
                    state.resource(ResourceName.FARM) < 12 &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 4 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_FARM, "STAGE_21_POST_10FLOOR_FOOD_FIX");
                return;
            }

            // 6) If endgame hiring would starve the population pool, add compact food.
            // Keep three slots reserved for University, Lab, and Rocket Science.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) < 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) &&
                    state.resource(ResourceName.IRON) < 325 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_21_RESTORE_IRON_FOR_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) < 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 3 &&
                    state.resource(ResourceName.CURRENCY) >= 400 &&
                    state.resource(ResourceName.ENERGY) >= 35 &&
                    state.resource(ResourceName.IRON) >= 325 &&
                    freeWorkers(state) >= 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_AGRICULTUR, "STAGE_21_BUILD_AGRICULTUR_FOR_ROCKET_FOOD");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) == 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) &&
                    state.resource(ResourceName.BUILDING_SLOTS) <= 3 &&
                    state.resource(ResourceName.FARM) > 8 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) >= 1) {
                tryScheduleQuick(state, RecipeName.DELETE_FARM, "STAGE_21_SWAP_FARM_FOR_SECOND_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) == 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) &&
                    state.resource(ResourceName.IRON) < 330 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_21_RESTORE_IRON_FOR_SECOND_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) == 1 &&
                    state.estimateProduction(ResourceName.FOOD) <= state.estimateConsumption(ResourceName.FOOD) &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 3 &&
                    state.resource(ResourceName.CURRENCY) >= 400 &&
                    state.resource(ResourceName.ENERGY) >= 35 &&
                    state.resource(ResourceName.IRON) >= 330 &&
                    freeWorkers(state) >= 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_AGRICULTUR, "STAGE_21_BUILD_SECOND_AGRICULTUR_FOR_ROCKET_FOOD");
                return;
            }

            // 7) Get enough free workers for water/University prep before more energy work.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    freeWorkers(state) < 10 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 100) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_21_PRE_ENERGY_HIRE");
                return;
            }

            // 8) Keep energy positive after compact food so the science chain can start.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.IRON) < 295 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_21_RESTORE_IRON_FOR_SOLAR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.estimateProduction(ResourceName.ENERGY) <= state.estimateConsumption(ResourceName.ENERGY) &&
                    state.resource(ResourceName.WATER_BUILDING) > 0 &&
                    state.resource(ResourceName.WATER_POWER_PLANT) < 2 &&
                    state.resource(ResourceName.CURRENCY) >= 300 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 295 &&
                    state.resource(ResourceName.WOOD) >= 30 &&
                    freeWorkers(state) >= 10) {
                tryScheduleBuilding(state, RecipeName.BUILD_WATER_POWER, "STAGE_21_BUILD_SECOND_WATER_AFTER_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.estimateProduction(ResourceName.ENERGY) <= state.estimateConsumption(ResourceName.ENERGY) &&
                    state.resource(ResourceName.WIND_POWER_PLANT) < 9 &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 3 &&
                    state.resource(ResourceName.CURRENCY) >= 120 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WOOD) >= 20 &&
                    freeWorkers(state) >= 2) {
                tryScheduleBuilding(state, RecipeName.BUILD_WIND_POWER, "STAGE_21_BUILD_WIND_AFTER_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.estimateProduction(ResourceName.ENERGY) <= state.estimateConsumption(ResourceName.ENERGY) &&
                    state.resource(ResourceName.BUILDING_SLOTS) > 3 &&
                    state.resource(ResourceName.CURRENCY) >= 400 &&
                    state.resource(ResourceName.ENERGY) >= 25 &&
                    state.resource(ResourceName.SOLAR_POWER_PLANT) < 1 &&
                    state.resource(ResourceName.IRON) >= 285 &&
                    state.resource(ResourceName.WOOD) >= 15 &&
                    freeWorkers(state) >= 5 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5) {
                tryScheduleBuilding(state, RecipeName.BUILD_SOLAR_POWER, "STAGE_21_BUILD_SOLAR_AFTER_AGRICULTUR");
                return;
            }

            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    state.resource(ResourceName.AGRICULTUR) >= 2 &&
                    state.resource(ResourceName.FOOD) < 10000 &&
                    state.estimateProduction(ResourceName.FOOD) > state.estimateConsumption(ResourceName.FOOD)) {
                return;
            }

            // 9) Build a moderate worker buffer before University.
            // Lab adds a large worker burden, so build the real rocket buffer before
            // University while population capacity is still healthy.
            if (state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                    freeWorkers(state) < 70 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 100) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_21_PRE_UNIVERSITY_HIRE");
                return;
            }

            printScienceBlocker(state, "STAGE_21_READY_FOR_UNIVERSITY");
        }

        // ANCHOR: UNIVERSITY BLOCK
        if (stage == 22) {
            // Stage 22: build University directly.
            // Do not repair energy here; Stage 21 already prepared the solar bridge before
            // 10-floor.
            if (state.resource(ResourceName.UNIVERSITY) < 1 &&
                    state.resource(ResourceName.CURRENCY) >= 450 &&
                    state.resource(ResourceName.ENERGY) >= 30 &&
                    state.resource(ResourceName.IRON) >= 265 &&
                    state.resource(ResourceName.WOOD) >= 40 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 3 &&
                    freeWorkers(state) >= 30) {
                tryScheduleBuilding(state, RecipeName.BUILD_UNIVERSITY, "STAGE_22_BUILD_UNIVERSITY");
                return;
            }

            // If something lowered free workers, recover one worker slowly.
            if (state.resource(ResourceName.UNIVERSITY) < 1 &&
                    freeWorkers(state) < 12 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 20 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 500 &&
                    state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD)) {
                tryScheduleHire(state, "STAGE_22_PRE_UNI_HIRE");
                return;
            }

            printScienceBlocker(state, "STAGE_22_UNIVERSITY_BLOCKED");
            return;
        }

        // ANCHOR: LAB BLOCK
        if (stage == 23) {
            if (state.resource(ResourceName.IRON) < 260 &&
                    state.resource(ResourceName.RAW_IRON) >= 10 &&
                    freeWorkers(state) >= 3 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 1) {
                tryScheduleFast(state, RecipeName.CONVERT_RAW_IRON, "STAGE_23_RESTORE_IRON_FOR_LAB_ROCKET");
                return;
            }

            if (freeWorkers(state) < 35 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 2000) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_23_PRE_LAB_HIRE");
                return;
            }

            if (state.resource(ResourceName.UNIVERSITY) >= 1 &&
                    state.resource(ResourceName.LAB) < 1 &&
                    state.resource(ResourceName.CURRENCY) >= 600 &&
                    state.resource(ResourceName.ENERGY) >= 700 &&
                    state.resource(ResourceName.IRON) >= 260 &&
                    state.resource(ResourceName.WOOD) >= 60 &&
                    state.resource(ResourceName.BUILDING_SLOTS) >= 2 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) >= 8 &&
                    freeWorkers(state) >= 30) {
                tryScheduleBuilding(state, RecipeName.BUILD_LAB, "STAGE_23_BUILD_LAB");
                return;
            }

            printScienceBlocker(state, "STAGE_23_LAB_BLOCKED");
            return;
        }

        // ANCHOR: ROCKET PREP BLOCK
        if (stage == 24) {
            if (state.resource(ResourceName.LAB) < 1) {
                System.out.println("Stage 24 blocked: LAB missing. Returning to Stage 23.");
                stage = 23;
                clearWait();
                return;
            }

            if (rocketReady(state)) {
                stage = 25;
                clearWait();
                return;
            }

            if (state.resource(ResourceName.UNIVERSITY) >= 1 &&
                    freeWorkers(state) < 30 &&
                    state.resource(ResourceName.CURRENCY) >= 450 &&
                    state.resource(ResourceName.ENERGY) >= 330 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) >= 4) {
                tryScheduleFinal(state, RecipeName.DELETE_UNIVERSITY, "STAGE_24_DELETE_UNIVERSITY_AFTER_LAB");
                return;
            }

            // Final sprint cleanup: protect the 300 energy stockpile Rocket Science needs.
            // Delete the least dangerous buildings first and stop once energy gets tight.
            if (freeWorkers(state) < 30 &&
                    state.resource(ResourceName.ENERGY) <= 320) {
                printScienceBlocker(state, "STAGE_24_ROCKET_BLOCKED_ENERGY_RESERVED");
                return;
            }

            if (freeWorkers(state) < 30 &&
                    freeWorkers(state) >= 10 &&
                    state.resource(ResourceName.WATER_POWER_PLANT) > 0 &&
                    state.resource(ResourceName.CURRENCY) >= 300 &&
                    state.resource(ResourceName.ENERGY) >= 330) {
                tryScheduleFinal(state, RecipeName.DELETE_WATER_POWER, "STAGE_24_DELETE_WATER_FOR_ROCKET");
                return;
            }

            if (freeWorkers(state) < 30 &&
                    state.resource(ResourceName.WIND_POWER_PLANT) > 0 &&
                    state.resource(ResourceName.ENERGY) >= 310 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) >= 2) {
                tryScheduleFinal(state, RecipeName.DELETE_WIND_POWER, "STAGE_24_DELETE_WIND_FOR_ROCKET");
                return;
            }

            if (freeWorkers(state) < 30 &&
                    state.resource(ResourceName.FARM) > 8 &&
                    state.resource(ResourceName.FOOD) > 5000 &&
                    state.estimateProduction(ResourceName.FOOD) > state.estimateConsumption(ResourceName.FOOD) + 45 &&
                    state.resource(ResourceName.CURRENCY) >= 70 &&
                    state.resource(ResourceName.ENERGY) >= 10 &&
                    state.resource(ResourceName.WORKER_WITH_JOB) >= 1) {
                tryScheduleFinal(state, RecipeName.DELETE_FARM, "STAGE_24_DELETE_EXTRA_FARM_FOR_ROCKET");
                return;
            }

            if (rocketReady(state)) {
                stage = 25;
                clearWait();
                return;
            }

            // Reduce worker pressure before hiring, otherwise HIRE drains the unemployed
            // pool.
            if (freeWorkers(state) < 30 &&
                    state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                    state.resource(ResourceName.CURRENCY) >= 50 &&
                    state.resource(ResourceName.FOOD) > 100) {
                tryScheduleFast(state, RecipeName.HIRE_WORKER, "STAGE_24_FINAL_HIRE_FOR_ROCKET");
                return;
            }

            printScienceBlocker(state, "STAGE_24_ROCKET_BLOCKED");
            return;
        }

        // ANCHOR: ROCKET SCIENCE BLOCK
        if (stage == 25) {
            if (rocketReady(state)) {
                tryScheduleFinal(state, RecipeName.BUILD_ROCKET_SCIENCE, "STAGE_25_BUILD_ROCKET_SCIENCE");
                return;
            }

            printScienceBlocker(state, "STAGE_25_ROCKET_ACTION_BLOCKED");
            stage = 24;
            clearWait();
            return;
        }

        // ANCHOR: COMPLETE BLOCK
        if (stage == 26) {
            System.out.println("ROCKET SCIENCE BUILT. COMPLETE CHECKPOINT.");
            return;
        }
    }

    // ANCHOR: STAGE UPDATE HELPER
    private void updateStage(StateInterface state) {
        if (stage == 0 && state.resource(ResourceName.WORKER_WITH_JOB) >= 12) {
            stage = 1;
            clearWait();
            return;
        }

        if (stage == 1 && state.resource(ResourceName.WIND_POWER_PLANT) >= 4) {
            stage = 2;
            clearWait();
            return;
        }

        if (stage == 2 && state.resource(ResourceName.WORKER_WITH_JOB) >= 18) {
            stage = 3;
            clearWait();
            return;
        }

        if (stage == 3 && state.resource(ResourceName.HOUSE) >= 6) {
            stage = 4;
            clearWait();
            return;
        }

        if (stage == 4 && state.resource(ResourceName.SAWMILL) >= 1) {
            stage = 5;
            clearWait();
            return;
        }

        if (stage == 5 && state.resource(ResourceName.WORKER_WITH_JOB) >= 22) {
            stage = 6;
            clearWait();
            return;
        }

        if (stage == 6 && state.resource(ResourceName.HOUSE) >= 7) {
            stage = 7;
            clearWait();
            return;
        }

        if (stage == 7 && state.resource(ResourceName.WORKER_WITH_JOB) >= 24) {
            stage = 8;
            clearWait();
            return;
        }

        if (stage == 8 &&
                state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD) + 20) {
            stage = 9;
            clearWait();
            return;
        }

        if (stage == 9 && state.resource(ResourceName.IRON_MINE) >= 1) {
            stage = 10;
            clearWait();
            return;
        }

        if (stage == 10 &&
                state.resource(ResourceName.WORKER_WITH_JOB) >= 30 &&
                state.resource(ResourceName.WORKER_WITHOUT_JOB) > 5 &&
                freeWorkers(state) >= 3) {
            stage = 11;
            clearWait();
            return;
        }

        if (stage == 11 && state.resource(ResourceName.IRON) >= 300) {
            stage = 12;
            clearWait();
            return;
        }

        if (stage == 12 && state.resource(ResourceName.WORKER_WITH_JOB) >= 34) {
            stage = 13;
            clearWait();
            return;
        }

        if (stage == 13 && state.resource(ResourceName.WIND_POWER_PLANT) >= 5) {
            stage = 14;
            clearWait();
            return;
        }

        if (stage == 14 && state.resource(ResourceName.APARTMENTS_AVAILABLE) >= 70) {
            stage = 15;
            clearWait();
            return;
        }

        if (stage == 15 &&
                state.resource(ResourceName.WIND_POWER_PLANT) >= 8 &&
                state.estimateProduction(ResourceName.ENERGY) >= state.estimateConsumption(ResourceName.ENERGY) + 10) {
            stage = 16;
            clearWait();
            return;
        }

        if (stage == 16 &&
                state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD) + 30 &&
                freeWorkers(state) >= 3) {
            stage = 17;
            clearWait();
            return;
        }

        if (stage == 17 &&
                state.resource(ResourceName.IRON) >= 500 &&
                state.estimateProduction(ResourceName.FOOD) >= state.estimateConsumption(ResourceName.FOOD) &&
                state.resource(ResourceName.WORKER_WITHOUT_JOB) > 1) {
            stage = 18;
            clearWait();
            return;
        }

        if (stage == 18 &&
                state.resource(ResourceName.APARTMENTS_AVAILABLE) >= 130) {
            stage = 19;
            clearWait();
            return;
        }

        if (stage == 19 &&
                state.resource(ResourceName.WATER_POWER_PLANT) >= 1 &&
                state.estimateProduction(ResourceName.ENERGY) >= state.estimateConsumption(ResourceName.ENERGY) + 20) {
            stage = 20;
            clearWait();
            return;
        }

        if (stage == 20 &&
                state.resource(ResourceName.FARM) >= 8 &&
                freeWorkers(state) >= 12 &&
                state.resource(ResourceName.WORKER_WITHOUT_JOB) > 20 &&
                state.resource(ResourceName.FOOD) > 250) {
            stage = 21;
            clearWait();
            return;
        }

        if (stage == 21 &&
                state.resource(ResourceName.TEN_STORY_BUILDING) >= 1 &&
                state.resource(ResourceName.BUILDING_SLOTS) >= 3 &&
                state.resource(ResourceName.IRON) >= 265 &&
                freeWorkers(state) >= 53 &&
                state.resource(ResourceName.ENERGY) >= 1000 &&
                state.resource(ResourceName.FOOD) >= 100) {
            stage = 22;
            clearWait();
            return;
        }

        if (stage == 22 &&
                state.resource(ResourceName.UNIVERSITY) >= 1) {
            stage = 23;
            clearWait();
            return;
        }

        if (stage == 23 &&
                state.resource(ResourceName.LAB) >= 1) {
            stage = 24;
            clearWait();
            return;
        }

        if (stage == 24 && rocketReady(state)) {
            stage = 25;
            clearWait();
            return;
        }

        if (stage == 25 &&
                state.resource(ResourceName.ROCKET_SCIENCE) >= 1) {
            stage = 26;
            clearWait();
            return;
        }
    }

    // ANCHOR: FREE WORKER HELPER
    private int freeWorkers(StateInterface state) {
        return state.resource(ResourceName.WORKER_WITH_JOB)
                - state.estimateConsumption(ResourceName.WORKER_WITH_JOB);
    }

    // ANCHOR: ROCKET READY HELPER
    private boolean rocketReady(StateInterface state) {
        return RocketReadiness.isReady(rocketSnapshot(state));
    }

    private RocketReadiness.Snapshot rocketSnapshot(StateInterface state) {
        return new RocketReadiness.Snapshot(
                state.resource(ResourceName.LAB),
                state.resource(ResourceName.ROCKET_SCIENCE),
                state.resource(ResourceName.BUILDING_SLOTS),
                state.resource(ResourceName.CURRENCY),
                state.resource(ResourceName.ENERGY),
                state.resource(ResourceName.IRON),
                state.resource(ResourceName.WOOD),
                freeWorkers(state));
    }

    // ANCHOR: SCIENCE DIAGNOSTIC HELPER
    private void printScienceBlocker(StateInterface state, String label) {
        if (state.getRound() % 25 != 0) {
            return;
        }

        System.out.println(label
                + " | slots=" + state.resource(ResourceName.BUILDING_SLOTS)
                + " currency=" + state.resource(ResourceName.CURRENCY)
                + " energy=" + state.resource(ResourceName.ENERGY)
                + " iron=" + state.resource(ResourceName.IRON)
                + " wood=" + state.resource(ResourceName.WOOD)
                + " freeWorkers=" + freeWorkers(state)
                + " idleWorkers=" + state.resource(ResourceName.WORKER_WITHOUT_JOB)
                + " university=" + state.resource(ResourceName.UNIVERSITY)
                + " lab=" + state.resource(ResourceName.LAB)
                + " rocket=" + state.resource(ResourceName.ROCKET_SCIENCE)
                + " blockers=" + RocketReadiness.blockers(rocketSnapshot(state)));
    }

    // ANCHOR: BUILDING SCHEDULE HELPER
    private boolean tryScheduleBuilding(StateInterface state, RecipeName recipe, String label) {
        boolean success = state.scheduleAction(recipe, 1).get();
        System.out.println("Tried: " + label + ", success: " + success);

        actionCooldown.recordAttempt(
                success,
                state.getRound(),
                5,
                label);

        return success;
    }

    // ANCHOR: CAREFUL HIRE HELPER
    private boolean tryScheduleHire(StateInterface state, String label) {
        boolean success = state.scheduleAction(RecipeName.HIRE_WORKER, 1).get();
        System.out.println("Tried: " + label + ", success: " + success);

        actionCooldown.recordAttempt(
                success,
                state.getRound(),
                5,
                label);

        return success;
    }

    // ANCHOR: QUICK DELETE HELPER
    private boolean tryScheduleQuick(StateInterface state, RecipeName recipe, String label) {
        boolean success = state.scheduleAction(recipe, 1).get();
        System.out.println("Tried: " + label + ", success: " + success);

        actionCooldown.recordAttempt(
                success,
                state.getRound(),
                2,
                label);

        return success;
    }

    // ANCHOR: FINAL SPRINT SCHEDULE HELPER
    private boolean tryScheduleFinal(StateInterface state, RecipeName recipe, String label) {
        boolean success = state.scheduleAction(recipe, 1).get();
        System.out.println("Tried: " + label + ", success: " + success);

        actionCooldown.recordAttempt(
                success,
                state.getRound(),
                1,
                label);

        return success;
    }

    // ANCHOR: FAST SCHEDULE HELPER
    private boolean tryScheduleFast(StateInterface state, RecipeName recipe, String label) {
        boolean success = state.scheduleAction(recipe, 1).get();
        System.out.println("Tried: " + label + ", success: " + success);
        return success;
    }

    // ANCHOR: CLEAR WAIT HELPER
    private void clearWait() {
        actionCooldown.clear();
    }

    private void printRecipe(Bot bot, RecipeName recipe) {
        System.out.println("=== " + recipe + " cost ===");
        printRecipeCost(bot, recipe);
        System.out.println("=== " + recipe + " outcome ===");
        printRecipeOutcome(bot, recipe);
    }

    private void printRecipeCost(Bot bot, RecipeName recipe) {
        for (var item : bot.recipeCost(recipe)) {
            System.out.println(item.first() + ": " + item.second());
        }
    }

    private void printRecipeOutcome(Bot bot, RecipeName recipe) {
        for (var item : bot.recipeOutcome(recipe)) {
            System.out.println(item.first() + ": " + item.second());
        }
    }
}
