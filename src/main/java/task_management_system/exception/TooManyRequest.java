package task_management_system.exception;

public class TooManyRequest extends RuntimeException {

    public TooManyRequest(String message) {
        super(message);
    }
}
