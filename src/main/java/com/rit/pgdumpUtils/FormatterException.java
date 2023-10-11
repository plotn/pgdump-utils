package com.rit.pgdumpUtils;

public class FormatterException extends Exception {

    public FormatterException(String message) {
        super(message);
    }

    public FormatterException(String messageFmt, Object... args) {
        super(String.format(messageFmt, args));
    }

}
