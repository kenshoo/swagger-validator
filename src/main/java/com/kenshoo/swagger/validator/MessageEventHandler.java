package com.kenshoo.swagger.validator;

import java.text.MessageFormat;

/**
 * Created by michaelp on 3/24/15.
 */
public class MessageEventHandler {
    private final String path;

    public MessageEventHandler(String path) {
        this.path = path;
    }

    public void handleError(String msg, Object... arguments) {
        String formatted = handleWarning(msg, arguments);
        throw new ValidationException(formatted);
    }

    public String handleWarning(String msg, Object... arguments) {
        String formatted = MessageFormat.format("Path: {0}: {1}", path, MessageFormat.format(msg, arguments));
        System.out.println(formatted);
        return formatted;
    }
}
