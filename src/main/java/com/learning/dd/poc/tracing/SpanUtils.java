package com.learning.dd.poc.tracing;

import datadog.trace.api.interceptor.MutableSpan;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

public final class SpanUtils {

    private SpanUtils() {}

    public static void setTag(String key, String value) {
        Span span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag(key, value);
        }
    }

    public static void setTag(String key, long value) {
        setTag(key, String.valueOf(value));
    }

    public static void setRootTag(String key, String value) {
        Span span = GlobalTracer.get().activeSpan();
        if (span instanceof MutableSpan) {
            ((MutableSpan) span).getLocalRootSpan().setTag(key, value);
        } else if (span != null) {
            span.setTag(key, value);
        }
    }

    public static void setRootTag(String key, long value) {
        setRootTag(key, String.valueOf(value));
    }
}
