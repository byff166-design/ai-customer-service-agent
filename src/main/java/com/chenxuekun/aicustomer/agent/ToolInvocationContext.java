package com.chenxuekun.aicustomer.agent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToolInvocationContext {
    private final ThreadLocal<State> state = new ThreadLocal<>();

    public void begin(String sessionId) {
        state.set(new State(sessionId, new ArrayList<>()));
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

    public List<String> toolNames() {
        State current = state.get();
        return current == null ? List.of() : List.copyOf(current.toolNames());
    }

    public void clear() {
        state.remove();
    }

    private record State(String sessionId, List<String> toolNames) {
    }
}
