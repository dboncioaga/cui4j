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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultCuiValidator")
class DefaultCuiValidatorTest {

    private CuiValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefaultCuiValidator();
    }

    @Nested
    @DisplayName("Valid CUI numbers")
    class ValidCuiTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "18547290",    // Valid 8-digit CUI (real company)
            "27",          // Valid 2-digit CUI
            "108",         // Valid 3-digit CUI  
            "1006",        // Valid 4-digit CUI
            "10004",       // Valid 5-digit CUI
            "100000",      // Valid 6-digit CUI
            "1000009",     // Valid 7-digit CUI
            "10000008",    // Valid 8-digit CUI
            "100000006",   // Valid 9-digit CUI
            "1000000004"   // Valid 10-digit CUI
        })
        @DisplayName("should validate correct CUI numbers")
        void shouldValidateCorrectCui(String cui) {
            ValidationResult result = validator.validate(cui);

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo(cui);
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate CUI with RO prefix")
        void shouldValidateCuiWithRoPrefix() {
            ValidationResult result = validator.validate("RO18547290");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
            assertThat(result.vatPrefixPresent()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate CUI with lowercase ro prefix")
        void shouldValidateCuiWithLowercaseRoPrefix() {
            ValidationResult result = validator.validate("ro18547290");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
            assertThat(result.vatPrefixPresent()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate CUI with spaces")
        void shouldValidateCuiWithSpaces() {
            ValidationResult result = validator.validate("18 547 290");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate CUI with hyphens")
        void shouldValidateCuiWithHyphens() {
            ValidationResult result = validator.validate("18-547-290");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate CUI with mixed separators")
        void shouldValidateCuiWithMixedSeparators() {
            ValidationResult result = validator.validate("RO 18.547-290");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
            assertThat(result.vatPrefixPresent()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should validate minimum length CUI (2 digits)")
        void shouldValidateMinimumLengthCui() {
            ValidationResult result = validator.validate("27");

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("27");
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Invalid CUI numbers")
    class InvalidCuiTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("should reject null or blank input")
        void shouldRejectNullOrBlank(String input) {
            ValidationResult result = validator.validate(input);

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "18547291",    // Invalid checksum (last digit wrong)
            "18547299",    // Invalid checksum
            "11111111"     // Invalid checksum
        })
        @DisplayName("should reject CUI with invalid checksum")
        void shouldRejectInvalidChecksum(String cui) {
            ValidationResult result = validator.validate(cui);

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).contains("checksum");
        }

        @Test
        @DisplayName("should reject CUI that is too short")
        void shouldRejectTooShortCui() {
            ValidationResult result = validator.validate("1");

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).contains("at least 2");
        }

        @Test
        @DisplayName("should reject CUI that is too long")
        void shouldRejectTooLongCui() {
            ValidationResult result = validator.validate("12345678901");

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).contains("at most 10");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ABC123",
            "18547X90",
            "185472!0",
            "RO123ABC",
            "RO!@#$%"
        })
        @DisplayName("should reject CUI with non-numeric characters")
        void shouldRejectNonNumeric(String cui) {
            ValidationResult result = validator.validate(cui);

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).contains("digits");
        }

        @Test
        @DisplayName("should reject CUI with only RO prefix")
        void shouldRejectOnlyRoPrefix() {
            ValidationResult result = validator.validate("RO");

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).isNotNull();
        }

        @Test
        @DisplayName("should reject RO prefix with spaces only")
        void shouldRejectRoPrefixWithSpacesOnly() {
            ValidationResult result = validator.validate("RO   ");

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.errorMessage()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ValidationResult factory methods")
    class ValidationResultTests {

        @Test
        @DisplayName("should create success result")
        void shouldCreateSuccessResult() {
            ValidationResult result = ValidationResult.success("12345678", true);

            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("12345678");
            assertThat(result.vatPrefixPresent()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            ValidationResult result = ValidationResult.failure("Test error");

            assertThat(result.valid()).isFalse();
            assertThat(result.normalizedCui()).isNull();
            assertThat(result.vatPrefixPresent()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Test error");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle CUI with leading zeros")
        void shouldHandleLeadingZeros() {
            // Leading zeros are valid in CUI numbers
            ValidationResult result = validator.validate("00123456");

            // This should be validated based on checksum
            assertThat(result.valid()).isFalse(); // This specific number has invalid checksum
        }

        @Test
        @DisplayName("should be case-insensitive for RO prefix")
        void shouldBeCaseInsensitiveForRoPrefix() {
            ValidationResult result1 = validator.validate("RO18547290");
            ValidationResult result2 = validator.validate("ro18547290");
            ValidationResult result3 = validator.validate("Ro18547290");
            ValidationResult result4 = validator.validate("rO18547290");

            assertThat(result1.valid()).isTrue();
            assertThat(result2.valid()).isTrue();
            assertThat(result3.valid()).isTrue();
            assertThat(result4.valid()).isTrue();

            assertThat(result1.vatPrefixPresent()).isTrue();
            assertThat(result2.vatPrefixPresent()).isTrue();
            assertThat(result3.vatPrefixPresent()).isTrue();
            assertThat(result4.vatPrefixPresent()).isTrue();
        }

        @Test
        @DisplayName("should handle various separator combinations")
        void shouldHandleVariousSeparatorCombinations() {
            String[] variations = {
                "18 547 290",
                "18-547-290",
                "18.547.290",
                "18_547_290",
                "18/547/290",
                "18 547-290"
            };

            for (String variation : variations) {
                ValidationResult result = validator.validate(variation);
                assertThat(result.normalizedCui())
                    .as("Variation: " + variation)
                    .isEqualTo("18547290");
            }
        }
    }
}
