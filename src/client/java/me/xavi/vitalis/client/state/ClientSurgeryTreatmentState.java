package me.xavi.vitalis.client.state;

public final class ClientSurgeryTreatmentState {

    private static boolean active;
    private static int progressTicks;
    private static int totalTicks;

    private ClientSurgeryTreatmentState() {
    }

    public static void update(boolean active, int progressTicks, int totalTicks) {
        ClientSurgeryTreatmentState.active = active;
        ClientSurgeryTreatmentState.progressTicks = progressTicks;
        ClientSurgeryTreatmentState.totalTicks = totalTicks;
    }

    public static boolean isActive() {
        return active;
    }

    public static float getProgress() {
        if (!active || totalTicks <= 0) {
            return 0.0F;
        }

        return Math.min(1.0F, (float) progressTicks / (float) totalTicks);
    }

    public static int getRemainingSeconds() {
        if (!active || totalTicks <= 0) {
            return 0;
        }

        return Math.max(0, (totalTicks - progressTicks) / 20);
    }
}