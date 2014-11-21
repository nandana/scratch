package org.ldp4j.apps.o2jc.validation;

/**
 * Created by nandana on 11/20/14.
 */
public class JSONLDContextValidationException extends RuntimeException {

    public JSONLDContextValidationException(String msg) {
        super(msg);
    }

    public JSONLDContextValidationException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
