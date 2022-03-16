package com.github.sshota0809.javaagent;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public final class IsitoB3Propagator implements TextMapPropagator {
  private static final String REQUEST_ID_FIELD = "x-request-id";
  private static final String PARENT_SPAN_ID_FIELD = "x-b3-parentspanid";
  private static final String TRACE_ID_FIELD = "x-b3-traceid";
  private static final String SPAN_ID_FIELD = "x-b3-spanid";
  private static final String SAMPLED_FIELD = "x-b3-sampled";
  private static final String FLAGS_FIELD = "x-b3-flags";

  private final List<String> fields;

  private static final ContextKey<List<String>> EXTRACTED_KEY_VALUES =
      ContextKey.named("istio-b3-keyvalues");

  public IsitoB3Propagator() {
    this.fields = Collections.unmodifiableList(Arrays.asList(REQUEST_ID_FIELD, PARENT_SPAN_ID_FIELD, TRACE_ID_FIELD, SPAN_ID_FIELD, SAMPLED_FIELD, FLAGS_FIELD));
  }

  @Override
  public List<String> fields() {
    return fields;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    List<String> extracted = context.get(EXTRACTED_KEY_VALUES);
    if (extracted != null) {
      for (int i = 0; i < extracted.size(); i += 2) {
        setter.set(carrier, extracted.get(i), extracted.get(i + 1));
      }
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    List<String> extracted = null;
    for (String field : fields) {
      String value = getter.get(carrier, field);
      if (value != null) {
        if (extracted == null) {
          extracted = new ArrayList<>();
        }
        extracted.add(field);
        extracted.add(value);
      }
    }
    return extracted != null ? context.with(EXTRACTED_KEY_VALUES, extracted) : context;
  }
}