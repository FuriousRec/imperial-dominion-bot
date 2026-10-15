package de.athron.gitlab.imperial_dominion;

public interface BotInterface {
    int getMatrikelNumber();
    String getStudentName();
    String getBotName();
    void init(Bot bot);
    void executeTurn(StateInterface state, Bot bot);
}
