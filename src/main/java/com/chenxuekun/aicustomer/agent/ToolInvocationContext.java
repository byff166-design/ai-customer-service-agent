package com.chenxuekun.aicustomer.agent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToolInvocationContext {
    private final ThreadLocal<State> state = new ThreadLocal<>();
    private final ThreadLocal<Boolean> failed = new ThreadLocal<>();

    public void begin(String sessionId) {
        begin(sessionId, "");
    }

    public void begin(String sessionId, String mode) {
        begin(sessionId, mode, sessionId);
    }

    public void begin(String sessionId, String mode, String customerId) {
        state.set(new State(sessionId, mode, customerId, new ArrayList<>()));
        failed.set(false);
    }

    public void record(String toolName) {
        State current = state.get();
        if (current != null && !current.toolNames().contains(toolName)) {
            current.toolNames().add(toolName);
        }
    }

    public String sessionId() {
        State current = state.get();
        return current == null ? "unknown" : current.sessionId();
    }

    public String customerId() {
        State current = state.get();
        return current == null ? "unknown" : current.customerId();
    }

    public String conversationKey() {
        State current = state.get();
        return current == null ? "unknown|unknown" : current.customerId() + "|" + current.sessionId();
    }

    public List<String> toolNames() {
        State current = state.get();
        return current == null ? List.of() : List.copyOf(current.toolNames());
    }

    public String mode() {
        State current = state.get();
        return current == null ? "" : current.mode();
    }

    public void markFailed() {
        failed.set(true);
    }

    public boolean hasFailure() {
        return Boolean.TRUE.equals(failed.get());
    }

    public void clear() {
        state.remove();
        failed.remove();
    }

    private record State(String sessionId, String mode, String customerId, List<String> toolNames) {
    }
}
