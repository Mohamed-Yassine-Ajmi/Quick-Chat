package chat.app.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {

        super("A user with this email exits already!");
    }
}
