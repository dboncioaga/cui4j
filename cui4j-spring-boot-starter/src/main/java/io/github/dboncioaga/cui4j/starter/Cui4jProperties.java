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
package io.github.dboncioaga.cui4j.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for CUI4J.
 */
@ConfigurationProperties(prefix = "cui4j")
public class Cui4jProperties {

    private final Anaf anaf = new Anaf();

    public Anaf getAnaf() {
        return anaf;
    }

    /**
     * ANAF-specific configuration properties.
     */
    public static class Anaf {
        
        /**
         * Whether ANAF integration is enabled.
         */
        private boolean enabled = true;

        /**
         * Request timeout for ANAF API calls.
         */
        private Duration timeout = Duration.ofSeconds(10);

        /**
         * Maximum number of retries for failed requests.
         */
        private int maxRetries = 2;

        /**
         * Maximum batch size for CUI lookups.
         */
        private int maxBatchSize = 500;

        /**
         * Cache TTL for ANAF responses.
         */
        private Duration cacheTtl = Duration.ofHours(24);

        /**
         * Rate limit (requests per second).
         */
        private int rateLimit = 2;

        /**
         * Base URL for ANAF API (for testing purposes).
         */
        private String baseUrl = "https://webservicesp.anaf.ro/PlatitorTvaRest/api/v9/ws/tva";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        public void setMaxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
        }

        public Duration getCacheTtl() {
            return cacheTtl;
        }

        public void setCacheTtl(Duration cacheTtl) {
            this.cacheTtl = cacheTtl;
        }

        public int getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(int rateLimit) {
            this.rateLimit = rateLimit;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
