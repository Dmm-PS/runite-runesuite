package io.ruin.utility;

import io.ruin.Server;

public class TickDelay {

    private long end;

    public void reset() {
        end = 0;
    }

    public void delay(int ticks) {
        end = Server.getEnd(ticks);
    }

    public void delaySeconds(int seconds) {
        delay(Server.toTicks(seconds));
    }

    public boolean isDelayed() {
        return !Server.isPast(end);
    }

    public boolean isDelayed(int extra) {
        return !Server.isPast(end + extra);
    }

    public int remaining() {
        return (int) (end - Server.currentTick());
    }

}
