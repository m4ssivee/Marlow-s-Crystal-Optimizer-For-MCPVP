package io.github.m4ssivee.mcpvpcrystal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

    private static final Logger LOGGER = LoggerFactory.getLogger("Marlow's Crystal Optimizer for MCPVP");

    private Log() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }
}
