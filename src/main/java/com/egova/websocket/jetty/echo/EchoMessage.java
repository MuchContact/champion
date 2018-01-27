package com.egova.websocket.jetty.echo;

public class EchoMessage {
    private final int round;
    private final String winner;

    public EchoMessage(int round, String winner) {

        this.round = round;
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }

    public int getRound() {
        return round;
    }

    @Override
    public String toString() {
        return String.format("%d,%s", round, winner);
    }
}
