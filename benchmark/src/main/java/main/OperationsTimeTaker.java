package main;

public class OperationsTimeTaker {

    private long opStartTimeNano;

    public void reportOperationStart() {
        opStartTimeNano = System.nanoTime();
    }

    public long reportOperationEnd() {
        long opEndTimeNano = System.nanoTime();
        long opDurationNano = opEndTimeNano - opStartTimeNano;

        return opDurationNano;
    }
}