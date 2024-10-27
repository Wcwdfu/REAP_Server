package Team_REAP.appserver.common.login.exception;

public class JwtException extends RuntimeException{

    public JwtException(String message){
        super(message);
    }
}
