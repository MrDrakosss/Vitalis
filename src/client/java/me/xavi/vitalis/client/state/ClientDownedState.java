package me.xavi.vitalis.client.state;

public final class ClientDownedState {

    private static boolean active;
    private static int remainingTicks;

    private ClientDownedState() {
    }

    public static void update(boolean active, int remainingTicks) {
        ClientDownedState.active = active;
        ClientDownedState.remainingTicks = remainingTicks;
    }

    public static boolean isActive() {
        return active;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static int getRemainingSeconds() {
        return Math.max(0, remainingTicks / 20);
    }
}
