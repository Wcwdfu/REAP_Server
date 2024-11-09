package Team_REAP.appserver.STT.exception;

public class DuplicateFileException extends RuntimeException{
    public DuplicateFileException() {
    }

    public DuplicateFileException(String message) {
        super(message);
    }
}
