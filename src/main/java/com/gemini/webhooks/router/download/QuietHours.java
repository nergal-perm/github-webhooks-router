package com.gemini.webhooks.router.download;

import java.time.LocalTime;

public class QuietHours {

    private final LocalTime start;
    private final LocalTime end;
    private final boolean enabled;

    private QuietHours(LocalTime start, LocalTime end, boolean enabled) {
        this.start = start;
        this.end = end;
        this.enabled = enabled;
    }

    public static QuietHours none() {
        return new QuietHours(null, null, false);
    }

    public static QuietHours of(LocalTime start, LocalTime end) {
        return new QuietHours(start, end, true);
    }

    public boolean isActive(LocalTime now) {
        if (!enabled) {
            return false;
        }
        if (start.isBefore(end)) {
            // Normal window: e.g. 09:00–17:00
            return !now.isBefore(start) && now.isBefore(end);
        } else {
            // Midnight-spanning window: e.g. 22:00–07:00
            return !now.isBefore(start) || now.isBefore(end);
        }
    }
}
