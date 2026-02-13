package com.gemini.webhooks.router.download;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuietHoursTest {

    // none() is never active
    @Test
    void none_isNeverActive() {
        QuietHours quietHours = QuietHours.none();

        assertThat(quietHours.isActive(LocalTime.of(0, 0))).isFalse();
        assertThat(quietHours.isActive(LocalTime.of(12, 0))).isFalse();
        assertThat(quietHours.isActive(LocalTime.of(23, 59))).isFalse();
    }

    // Normal window (no midnight crossing): 09:00–17:00
    @Test
    void normalWindow_timeInsideIsActive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(quietHours.isActive(LocalTime.of(12, 0))).isTrue();
    }

    @Test
    void normalWindow_timeOutsideIsNotActive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(quietHours.isActive(LocalTime.of(8, 59))).isFalse();
        assertThat(quietHours.isActive(LocalTime.of(18, 0))).isFalse();
    }

    @Test
    void normalWindow_endBoundaryIsExclusive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(quietHours.isActive(LocalTime.of(17, 0))).isFalse();
    }

    @Test
    void normalWindow_startBoundaryIsInclusive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(quietHours.isActive(LocalTime.of(9, 0))).isTrue();
    }

    // Midnight-spanning window: 22:00–07:00
    @Test
    void midnightSpanning_timeBeforeMidnightIsActive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(22, 0), LocalTime.of(7, 0));

        assertThat(quietHours.isActive(LocalTime.of(23, 0))).isTrue();
    }

    @Test
    void midnightSpanning_timeAfterMidnightIsActive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(22, 0), LocalTime.of(7, 0));

        assertThat(quietHours.isActive(LocalTime.of(3, 0))).isTrue();
    }

    @Test
    void midnightSpanning_timeOutsideIsNotActive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(22, 0), LocalTime.of(7, 0));

        assertThat(quietHours.isActive(LocalTime.of(10, 0))).isFalse();
    }

    @Test
    void midnightSpanning_endBoundaryIsExclusive() {
        QuietHours quietHours = QuietHours.of(LocalTime.of(22, 0), LocalTime.of(7, 0));

        assertThat(quietHours.isActive(LocalTime.of(7, 0))).isFalse();
    }
}
