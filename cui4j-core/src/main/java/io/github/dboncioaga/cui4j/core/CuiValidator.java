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
 * Validator for Romanian CUI (Cod Unic de Identificare) and CIF (Cod de Identificare Fiscală) numbers.
 * <p>
 * A CUI is the unique identification code used by Romanian companies and organizations.
 * It can optionally be prefixed with "RO" for VAT purposes (making it a VAT identification number).
 */
public interface CuiValidator {

    /**
     * Validates a Romanian CUI/CIF number.
     * <p>
     * The input can contain:
     * <ul>
     *   <li>Optional "RO" prefix (case-insensitive)</li>
     *   <li>2 to 10 digits</li>
     *   <li>Spaces and common separators (which will be ignored)</li>
     * </ul>
     *
     * @param input the CUI to validate (may be null)
     * @return the validation result containing success/failure and normalized value
     */
    ValidationResult validate(@Nullable String input);
}
