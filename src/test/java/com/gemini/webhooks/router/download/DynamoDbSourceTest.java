package com.gemini.webhooks.router.download;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class DynamoDbSourceTest {

    // 3.1 — close() on Null variant is tracked by CloseTracker
    @Test
    void close_onNullVariant_isRecordedByTracker() {
        DynamoDbSource source = DynamoDbSource.createNull();
        DynamoDbSource.CloseTracker tracker = source.trackClose();

        source.close();

        assertThat(tracker.wasClosed()).isTrue();
    }

    // 3.2 — close() on Null variant without tracker is a safe no-op
    @Test
    void close_onNullVariantWithNoTracker_doesNotThrow() {
        DynamoDbSource source = DynamoDbSource.createNull();

        assertThatNoException().isThrownBy(source::close);
    }
}
