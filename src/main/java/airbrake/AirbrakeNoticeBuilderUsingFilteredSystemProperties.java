// Modified or written by Luca Marrocco for inclusion with airbrake.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")
package airbrake;

import java.util.*;

import org.apache.logging.log4j.ThreadContext;

public class AirbrakeNoticeBuilderUsingFilteredSystemProperties extends AirbrakeNoticeBuilder {

    public AirbrakeNoticeBuilderUsingFilteredSystemProperties(final String apiKey, final Backtrace backtraceBuilder, final Throwable throwable, final String env) {
        super(apiKey, backtraceBuilder, throwable, env);
        environment(System.getProperties());
        addMDCToSession();
        standardEnvironmentFilters();
        ec2EnvironmentFilters();
    }

    private void addMDCToSession() {
        @SuppressWarnings("unchecked")
        Map<String, String> map = ThreadContext.getContext();

        if (map != null) {
            addSessionKey(":key", Integer.toString(map.hashCode()));
            addSessionKey(":data", map);
        }
    }
}
