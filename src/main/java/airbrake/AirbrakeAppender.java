// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")
package airbrake;

import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "AirbrakeAppender", category = "Core", elementType = "appender")
public class AirbrakeAppender extends AbstractAppender {

    private final AirbrakeNotifier airbrakeNotifier = new AirbrakeNotifier();

    private final String name;
    private final String apiKey;
    private final String env;
    private Backtrace backtrace = new Backtrace();

    public AirbrakeAppender(String name, String apiKey, String env, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.name = name;
        this.apiKey = apiKey;
        this.env = env;
    }

    @PluginFactory
    public static AirbrakeAppender createAppender(@PluginAttribute("name") String name,
            @PluginAttribute("apiKey") String apiKey,
            @PluginAttribute("env") String env,
            @PluginElement("Layout") Layout layout,
            @PluginElement("Filters") Filter filter) {
        AirbrakeAppender appender = new AirbrakeAppender(name, apiKey, env, filter, layout, false);
        return appender;
    }

    @Override
    public void append(final LogEvent loggingEvent) {
        if (thereIsThrowableIn(loggingEvent)) {
            notifyThrowableIn(loggingEvent);
        }
    }

    public AirbrakeNotice newNoticeFor(final Throwable throwable) {
        return new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey,
                backtrace, throwable, env).newNotice();
    }

    private int notifyThrowableIn(final LogEvent loggingEvent) {
        return airbrakeNotifier.notify(newNoticeFor(throwable(loggingEvent)));
    }

    public void setUrl(final String url) {
        airbrakeNotifier.setUrl(url);
    }

    /**
     * Checks if the LoggingEvent contains a Throwable
     *
     * @param loggingEvent
     * @return
     */
    private boolean thereIsThrowableIn(final LogEvent loggingEvent) {
        return loggingEvent.getThrown() != null
                || loggingEvent.getMessage() instanceof Throwable;
    }

    /**
     * Get the throwable information contained in a {@link LoggingEvent}.
     * Returns the Throwable passed to the logger or the message if it's a
     * Throwable.
     *
     * @param loggingEvent
     * @return The Throwable contained in the {@link LoggingEvent} or null if
     * there is none.
     */
    private Throwable throwable(final LogEvent loggingEvent) {
        return loggingEvent.getThrown();
    }

    protected String getApiKey() {
        return apiKey;
    }

    public Backtrace getBacktrace() {
        return backtrace;
    }

    protected String getEnv() {
        return env;
    }
}
