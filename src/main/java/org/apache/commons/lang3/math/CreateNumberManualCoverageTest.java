package org.apache.commons.lang3.math;

import org.apache.commons.lang3.AbstractLangTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class ManualCoverageReportTest extends AbstractLangTest {

    @Test
    void triggerTestExecution() {
    }

    @AfterAll
    static void printManualCoverage() {
        NumberUtils.printCoverage();
    }
}
