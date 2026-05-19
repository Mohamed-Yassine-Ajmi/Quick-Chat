package chat.app.exception;

public class InvalidMessageException extends RuntimeException{
    public InvalidMessageException(String msg) {
        super(msg);
    }
}
