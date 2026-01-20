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

import io.github.dboncioaga.cui4j.anaf.internal.*;
import io.github.dboncioaga.cui4j.core.CuiValidator;
import io.github.dboncioaga.cui4j.core.DefaultCuiValidator;
import io.github.dboncioaga.cui4j.core.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AnafClient} using Spring RestClient.
 * <p>
 * This implementation includes:
 * <ul>
 *   <li>Automatic retries with exponential backoff</li>
 *   <li>Configurable timeout</li>
 *   <li>Request batching support</li>
 *   <li>CUI validation before making requests</li>
 * </ul>
 */
public final class DefaultAnafClient implements AnafClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultAnafClient.class);
    
    private static final String DEFAULT_ANAF_URL = "https://webservicesp.anaf.ro/PlatitorTvaRest/api/v9/ws/tva";
    private static final int DEFAULT_MAX_BATCH_SIZE = 500;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final int DEFAULT_MAX_RETRIES = 2;

    private final RestClient restClient;
    private final CuiValidator cuiValidator;
    private final int maxBatchSize;
    private final int maxRetries;

    /**
     * Creates a new ANAF client with default configuration.
     */
    public DefaultAnafClient() {
        this(DEFAULT_ANAF_URL, DEFAULT_TIMEOUT, DEFAULT_MAX_RETRIES, DEFAULT_MAX_BATCH_SIZE);
    }

    /**
     * Creates a new ANAF client with custom configuration.
     *
     * @param baseUrl the ANAF API base URL
     * @param timeout the request timeout
     * @param maxRetries the maximum number of retries
     * @param maxBatchSize the maximum batch size
     */
    public DefaultAnafClient(String baseUrl, Duration timeout, int maxRetries, int maxBatchSize) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.cuiValidator = new DefaultCuiValidator();
        this.maxBatchSize = maxBatchSize;
        this.maxRetries = maxRetries;
    }

    /**
     * Creates a new ANAF client with a custom RestClient.
     * Package-private for testing purposes.
     *
     * @param restClient the RestClient to use
     * @param maxBatchSize the maximum batch size
     */
    DefaultAnafClient(RestClient restClient, int maxBatchSize) {
        this.restClient = restClient;
        this.cuiValidator = new DefaultCuiValidator();
        this.maxBatchSize = maxBatchSize;
        this.maxRetries = DEFAULT_MAX_RETRIES;
    }

    @Override
    public CompanyInfo lookup(String cui) {
        log.debug("Looking up CUI: {}", cui);
        
        List<CompanyInfo> results = lookupBatch(List.of(cui));
        return results.getFirst();
    }

    @Override
    public List<CompanyInfo> lookupBatch(List<String> cuis) {
        if (cuis == null || cuis.isEmpty()) {
            throw new IllegalArgumentException("CUI list cannot be null or empty");
        }
        
        if (cuis.size() > maxBatchSize) {
            throw new IllegalArgumentException(
                "Batch size " + cuis.size() + " exceeds maximum allowed " + maxBatchSize
            );
        }

        log.debug("Looking up {} CUIs in batch", cuis.size());
        
        // Validate and normalize CUIs
        Map<Long, String> normalizedCuis = cuis.stream()
            .collect(Collectors.toMap(
                this::validateAndNormalizeCui,
                cui -> cui,
                (a, b) -> a
            ));

        // Build request
        LocalDate referenceDate = LocalDate.now();
        List<AnafRequest> requests = normalizedCuis.keySet().stream()
            .map(cui -> AnafRequest.of(cui, referenceDate))
            .toList();

        // Make API call with retries
        return executeWithRetry(requests, normalizedCuis, referenceDate);
    }

    private List<CompanyInfo> executeWithRetry(
        List<AnafRequest> requests,
        Map<Long, String> normalizedCuis,
        LocalDate referenceDate
    ) {
        Exception lastException = null;
        long backoffMillis = 500;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.warn("Retrying ANAF request, attempt {}/{}", attempt, maxRetries);
                    Thread.sleep(backoffMillis);
                    backoffMillis = Math.min(backoffMillis * 2, 2000); // Exponential backoff, max 2s
                }

                AnafResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requests)
                    .retrieve()
                    .body(AnafResponse.class);

                if (response == null) {
                    throw new AnafClientException("Received null response from ANAF");
                }

                return mapResponse(response, normalizedCuis, referenceDate);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AnafClientException("Request interrupted", e);
            } catch (Exception e) {
                lastException = e;
                if (attempt == maxRetries) {
                    log.error("Failed to query ANAF API after {} attempts", maxRetries + 1, e);
                }
            }
        }

        throw new AnafClientException("Failed to query ANAF API after " + (maxRetries + 1) + " attempts", lastException);
    }

    private long validateAndNormalizeCui(String cui) {
        ValidationResult result = cuiValidator.validate(cui);
        
        if (!result.valid()) {
            throw new IllegalArgumentException(
                "Invalid CUI: " + cui + " - " + result.errorMessage()
            );
        }
        
        return Long.parseLong(result.normalizedCui());
    }

    private List<CompanyInfo> mapResponse(
        AnafResponse response,
        Map<Long, String> requestedCuis,
        LocalDate referenceDate
    ) {
        List<CompanyInfo> results = new ArrayList<>();
        
        // Process found companies
        if (response.found() != null) {
            for (AnafCompanyData companyData : response.found()) {
                if (companyData.generalData() != null) {
                    results.add(mapCompanyData(companyData.generalData()));
                }
            }
        }
        
        // Process not found companies
        if (response.notFound() != null) {
            for (AnafNotFoundData notFoundData : response.notFound()) {
                if (notFoundData.cui() != null) {
                    results.add(CompanyInfo.notFound(notFoundData.cui(), referenceDate));
                }
            }
        }
        
        return results;
    }

    private CompanyInfo mapCompanyData(AnafGeneralData data) {
        return new CompanyInfo(
            data.cui(),
            parseDate(data.referenceDate()),
            data.companyName(),
            parseDate(data.registrationDate()),
            data.address(),
            data.phoneNumber(),
            data.postalCode(),
            Boolean.TRUE.equals(data.isVatPayer()),
            parseDate(data.vatRegistrationDate()),
            data.splitVatStartDate() != null,
            Boolean.TRUE.equals(data.isInactive()),
            true
        );
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }
}
