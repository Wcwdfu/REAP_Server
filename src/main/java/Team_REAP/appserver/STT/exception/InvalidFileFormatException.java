package Team_REAP.appserver.STT.exception;

public class InvalidFileFormatException extends RuntimeException{
    public InvalidFileFormatException() {
    }

    public InvalidFileFormatException(String message) {
        super(message);
    }
}
