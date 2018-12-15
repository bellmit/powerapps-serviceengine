/*
 * Created on Sep 2, 2003
 */
package com.profitera.rpm;

/**
 * @author jamison
 *
 */
public class IncompleteAccountDataException extends Exception {

    public IncompleteAccountDataException() {
        super();
    }

    public IncompleteAccountDataException(String message) {
        super(message);
    }

    public IncompleteAccountDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteAccountDataException(Throwable cause) {
        super(cause);
    }

}
