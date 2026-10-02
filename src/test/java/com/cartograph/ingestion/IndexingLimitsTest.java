package com.cartograph.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexingLimitsTest {
    @Test
    void allowsValuesAtEveryLimit() {
        IndexingLimits limits = new IndexingLimits(2, 100, 60);
        assertDoesNotThrow(() -> limits.validateFetchedTree(List.of(40L, 60L)));
    }

    @Test
    void rejectsTooManyFilesWithTypedDetails() {
        RepositoryLimitException error = assertThrows(RepositoryLimitException.class,
                () -> new IndexingLimits(2, 100, 100).validateFetchedTree(List.of(1L, 2L, 3L)));
        assertEquals(IndexingLimits.Limit.FILE_COUNT, error.limit());
        assertEquals(3L, error.observedValue());
    }

    @Test
    void rejectsTotalBytesBeforeContentDownload() {
        RepositoryLimitException error = assertThrows(RepositoryLimitException.class,
                () -> new IndexingLimits(3, 100, 100).validateFetchedTree(List.of(60L, 41L)));
        assertEquals(IndexingLimits.Limit.TOTAL_BYTES, error.limit());
        assertEquals(101L, error.observedValue());
    }

    @Test
    void rejectsIndividualFileLimit() {
        RepositoryLimitException error = assertThrows(RepositoryLimitException.class,
                () -> new IndexingLimits(3, 1000, 50).validateFetchedTree(List.of(51L)));
        assertEquals(IndexingLimits.Limit.FILE_BYTES, error.limit());
        assertEquals(51L, error.observedValue());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new IndexingLimits(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new IndexingLimits(1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new IndexingLimits(1, 1, 0));
    }
}
