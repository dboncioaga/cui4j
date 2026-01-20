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
 * Default implementation of {@link CuiValidator} using the Romanian checksum algorithm.
 * <p>
 * This implementation is thread-safe and can be used as a singleton.
 */
public final class DefaultCuiValidator implements CuiValidator {

    private static final int MIN_CUI_LENGTH = 2;
    private static final int MAX_CUI_LENGTH = 10;
    private static final String VAT_PREFIX = "RO";
    
    /**
     * Checksum control key as defined by Romanian legislation.
     * The key has 10 digits, indexed from position 0 to 9.
     */
    private static final int[] CHECKSUM_KEY = {7, 5, 3, 2, 1, 7, 5, 3, 2};

    /**
     * Creates a new CUI validator.
     */
    public DefaultCuiValidator() {
    }

    @Override
    public ValidationResult validate(@Nullable String input) {
        if (input == null) {
            return ValidationResult.failure("CUI cannot be null");
        }

        if (input.isBlank()) {
            return ValidationResult.failure("CUI cannot be empty");
        }

        // Normalize: remove whitespace and separators
        String normalized = input.replaceAll("[\\s\\-_./]", "");
        
        // Check for RO prefix
        boolean hasVatPrefix = normalized.toUpperCase(java.util.Locale.ROOT).startsWith(VAT_PREFIX);
        if (hasVatPrefix) {
            normalized = normalized.substring(VAT_PREFIX.length());
        }

        // Validate it contains only digits
        if (!normalized.matches("\\d+")) {
            return ValidationResult.failure("CUI must contain only digits (optionally prefixed with 'RO')");
        }

        // Validate length
        int length = normalized.length();
        if (length < MIN_CUI_LENGTH) {
            return ValidationResult.failure("CUI must have at least " + MIN_CUI_LENGTH + " digits");
        }
        if (length > MAX_CUI_LENGTH) {
            return ValidationResult.failure("CUI must have at most " + MAX_CUI_LENGTH + " digits");
        }

        // Validate checksum
        if (!isChecksumValid(normalized)) {
            return ValidationResult.failure("CUI checksum is invalid");
        }

        return ValidationResult.success(normalized, hasVatPrefix);
    }

    /**
     * Validates the CUI checksum according to Romanian algorithm.
     * <p>
     * Algorithm based on ANAF specifications:
     * <ol>
     *   <li>The last digit is the control digit</li>
     *   <li>Pad the CUI (without control digit) to the left with zeros to make it 9 digits</li>
     *   <li>Multiply each of the 9 digits by the corresponding key value</li>
     *   <li>Sum all the products</li>
     *   <li>Multiply the sum by 10</li>
     *   <li>Take the remainder when dividing by 11</li>
     *   <li>If the remainder is 10, the control digit should be 0, otherwise it should equal the remainder</li>
     * </ol>
     */
    private boolean isChecksumValid(String cui) {
        int length = cui.length();
        
        // The last digit is the control digit
        int controlDigit = Character.getNumericValue(cui.charAt(length - 1));
        
        // Get digits without control digit, pad to 9 digits from the left
        String cuiWithoutControl = cui.substring(0, length - 1);
        String paddedCui = String.format("%9s", cuiWithoutControl).replace(' ', '0');
        
        // Calculate sum of products
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(paddedCui.charAt(i));
            sum += digit * CHECKSUM_KEY[i];
        }
        
        // Calculate expected control digit
        int product = sum * 10;
        int remainder = product % 11;
        int expectedControl = remainder;
        
        // Special case: if result is 10, the control digit should be 0
        final int specialCase = 10;
        if (remainder == specialCase) {
            expectedControl = 0;
        }
        
        return controlDigit == expectedControl;
    }
}
