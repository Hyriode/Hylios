package fr.hyriode.hylios.util;

/**
 * Created by AstFaster
 * on 05/12/2022 at 20:59
 */
public class HyliosException extends RuntimeException {

    public HyliosException(String message) {
        super(message);
    }

    public HyliosException(String message, Throwable cause) {
        super(message, cause);
    }

    public HyliosException(Throwable cause) {
        super(cause);
    }

}
