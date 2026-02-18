/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.time;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DIY branch coverage instrumentation utility.
 * Temporary. NOT to be merged to main.
 */
public final class BranchCoverage {

    private static final Map<String, boolean[]> COVERAGE = new LinkedHashMap<>();
    private static final int MAX_BRANCHES = 100;

    private BranchCoverage() {
    }

    /**
     * Mark branch {@code branchId} of {@code functionName} as reached.
     * @param functionName the name of the instrumented function.
     * @param branchId     the unique ID of the reached branch.
     */
    public static void hit(final String functionName, final int branchId) {
        COVERAGE.computeIfAbsent(functionName, k -> new boolean[MAX_BRANCHES])[branchId] = true;
    }

    /**
     * Print which branch IDs were covered for {@code functionName}.
     * @param functionName the name of the instrumented function.
     */
    public static void report(final String functionName) {
        final boolean[] hits = COVERAGE.get(functionName);
        if (hits == null) {
            System.out.println("[BranchCoverage] " + functionName + ": no data");
            return;
        }
        final StringBuilder covered = new StringBuilder();
        for (int i = 0; i < MAX_BRANCHES; i++) {
            if (hits[i]) {
                if (covered.length() > 0) {
                    covered.append(", ");
                }
                covered.append(i);
            }
        }
        System.out.println("=== BranchCoverage: " + functionName + " ===");
        System.out.println("  Covered : [" + covered + "]");
    }
}
