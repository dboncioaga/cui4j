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
package io.github.dboncioaga.cui4j.anaf;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * Company information retrieved from ANAF.
 * <p>
 * This record contains tax-related information about a Romanian company.
 * All fields may be null if the company is not found or data is unavailable.
 *
 * @param cui the CUI number (without RO prefix)
 * @param referenceDate the reference date for which data was retrieved
 * @param companyName the registered company name
 * @param registrationDate the date when the company was registered
 * @param address the registered company address
 * @param phoneNumber the registered phone number
 * @param postalCode the postal code
 * @param isVatPayer whether the company is registered for VAT
 * @param vatRegistrationDate the date when VAT registration started
 * @param isSplitVat whether the company uses split VAT regime
 * @param isInactive whether the company is inactive
 * @param foundInAnafRegistry whether the CUI was found in ANAF registry
 */
public record CompanyInfo(
    @Nullable Long cui,
    @Nullable LocalDate referenceDate,
    @Nullable String companyName,
    @Nullable LocalDate registrationDate,
    @Nullable String address,
    @Nullable String phoneNumber,
    @Nullable String postalCode,
    boolean isVatPayer,
    @Nullable LocalDate vatRegistrationDate,
    boolean isSplitVat,
    boolean isInactive,
    boolean foundInAnafRegistry
) {
    /**
     * Creates a company info for a CUI not found in the registry.
     */
    public static CompanyInfo notFound(long cui, LocalDate referenceDate) {
        return new CompanyInfo(
            cui,
            referenceDate,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            false,
            false,
            false
        );
    }
}
