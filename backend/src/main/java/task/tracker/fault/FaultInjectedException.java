package task.tracker.fault;

public class FaultInjectedException extends RuntimeException {

    public FaultInjectedException() {
        super("Injected fault");
    }
}
