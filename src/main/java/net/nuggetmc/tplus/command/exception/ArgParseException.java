package net.nuggetmc.tplus.command.exception;

import java.lang.reflect.Parameter;

public class ArgParseException extends Exception {

    private final Parameter parameter;

    public ArgParseException(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
