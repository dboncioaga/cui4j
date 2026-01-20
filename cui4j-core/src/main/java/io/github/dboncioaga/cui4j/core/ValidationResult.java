/*
 * Copyright 2026 Daniel Boncioaga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dboncioaga.cui4j.core;

import org.jspecify.annotations.Nullable;

/**
 * Result of CUI/CIF validation.
 *
 * @param valid whether the CUI is valid according to the Romanian checksum algorithm
 * @param normalizedCui the normalized CUI without prefix or separators (null if invalid)
 * @param vatPrefixPresent whether the "RO" VAT prefix was present in the input
 * @param errorMessage description of validation failure (null if valid)
 */
public record ValidationResult(
    boolean valid,
    @Nullable String normalizedCui,
    boolean vatPrefixPresent,
    @Nullable String errorMessage
) {
    /**
     * Creates a successful validation result.
     */
    public static ValidationResult success(String normalizedCui, boolean vatPrefixPresent) {
        return new ValidationResult(true, normalizedCui, vatPrefixPresent, null);
    }

    /**
     * Creates a failed validation result.
     */
    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, null, false, errorMessage);
    }
}
