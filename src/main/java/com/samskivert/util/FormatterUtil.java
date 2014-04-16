//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Helper bits for {@link OneLineLogFormatter} and {@link TerseLogFormatter}.
 */
public class FormatterUtil
{
    /** The line separator to use between log messages. */
    public static String LINE_SEPARATOR = "\n";
    static {
        try {
            LINE_SEPARATOR = System.getProperty("line.separator");
        } catch (Exception e) {
        }
    }

    /**
     * Configures the default logging handler to use an instance of the specified formatter when
     * formatting messages.
     */
    public static void configureDefaultHandler (Formatter formatter)
    {
        // work around for bug - http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8017174
        if (LogManager.getLogManager() != null) {
            LogManager.getLogManager().getLogger("fake_logger");

            Logger logger = LogManager.getLogManager().getLogger("");
            if (logger != null) {
                for (Handler handler : logger.getHandlers()) {
                    handler.setFormatter(formatter);
                }
            }
        }
    }
}
