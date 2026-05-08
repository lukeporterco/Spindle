package com.mcmodloader.core.process;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ProcessOutputCapture {
    private final int maxLinesPerStream;
    private final Deque<String> stdoutLines = new ArrayDeque<>();
    private final Deque<String> stderrLines = new ArrayDeque<>();

    public ProcessOutputCapture() {
        this(200);
    }

    public ProcessOutputCapture(int maxLinesPerStream) {
        this.maxLinesPerStream = Math.max(1, maxLinesPerStream);
    }

    public void appendStdout(String line) {
        append(stdoutLines, line);
    }

    public void appendStderr(String line) {
        append(stderrLines, line);
    }

    public synchronized String stdoutTail() {
        return join(stdoutLines);
    }

    public synchronized String stderrTail() {
        return join(stderrLines);
    }

    private synchronized void append(Deque<String> target, String line) {
        if (target.size() >= maxLinesPerStream) {
            target.removeFirst();
        }
        target.addLast(line == null ? "" : line);
    }

    private String join(Deque<String> lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
