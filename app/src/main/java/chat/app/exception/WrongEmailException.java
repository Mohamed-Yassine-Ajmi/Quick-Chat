package chat.app.exception;


public class WrongEmailException extends Exception {
    public WrongEmailException(){
        super("Email not found");
    }
}