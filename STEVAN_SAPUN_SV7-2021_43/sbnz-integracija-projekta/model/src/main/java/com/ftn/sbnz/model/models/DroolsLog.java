package com.ftn.sbnz.model.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DroolsLog {

    private static final List<String> logs = new ArrayList<>();

    public static boolean log(String msg) {
        System.out.println(msg); // still print to console
        logs.add(msg);
        return true;
    }

    public static void clear() {
        logs.clear();
    }

    public static List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    public static void add(String msg) {
        log(msg); // alias for convenience
    }
}
