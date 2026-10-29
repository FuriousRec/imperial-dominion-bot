#!/usr/bin/env bash
set -euo pipefail

build_dir="${TMPDIR:-/tmp}/imperial-dominion-bot-tests"
rm -rf "$build_dir"
mkdir -p "$build_dir"

javac -Xlint:all -Werror -d "$build_dir" \
    ImperialBot.java \
    ActionCooldown.java \
    RocketReadiness.java \
    test/ActionCooldownTest.java \
    test/ImperialBotRecoveryTest.java \
    test/RocketReadinessTest.java \
    test-stubs/de/athron/gitlab/imperial_dominion/Bot.java \
    test-stubs/de/athron/gitlab/imperial_dominion/BotInterface.java \
    test-stubs/de/athron/gitlab/imperial_dominion/StateInterface.java \
    test-stubs/de/athron/gitlab/imperial_dominion/jni/libgame/RecipeName.java \
    test-stubs/de/athron/gitlab/imperial_dominion/jni/libgame/ResourceName.java

java -ea -cp "$build_dir" ActionCooldownTest
java -ea -cp "$build_dir" ImperialBotRecoveryTest
java -ea -cp "$build_dir" RocketReadinessTest
