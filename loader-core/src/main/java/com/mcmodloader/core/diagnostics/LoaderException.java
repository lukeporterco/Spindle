package com.mcmodloader.core.diagnostics;

public final class LoaderException extends Exception {
    public LoaderException(String message) {
        super(message);
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
