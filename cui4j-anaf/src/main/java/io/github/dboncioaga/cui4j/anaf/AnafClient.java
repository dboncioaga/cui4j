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

import java.util.List;

/**
 * Client for interacting with the ANAF REST API.
 * <p>
 * This client provides methods to query company information from the Romanian
 * National Agency for Fiscal Administration (ANAF) public API.
 */
public interface AnafClient {

    /**
     * Looks up company information for a single CUI.
     *
     * @param cui the CUI to look up (with or without RO prefix)
     * @return company information, or a not-found record if the CUI doesn't exist
     * @throws AnafClientException if the request fails
     */
    CompanyInfo lookup(String cui);

    /**
     * Looks up company information for multiple CUIs in a batch.
     * <p>
     * This is more efficient than making individual requests for each CUI.
     *
     * @param cuis the list of CUIs to look up (with or without RO prefix)
     * @return list of company information in the same order as the input
     * @throws AnafClientException if the request fails
     * @throws IllegalArgumentException if the list is empty or exceeds maximum batch size
     */
    List<CompanyInfo> lookupBatch(List<String> cuis);
}
