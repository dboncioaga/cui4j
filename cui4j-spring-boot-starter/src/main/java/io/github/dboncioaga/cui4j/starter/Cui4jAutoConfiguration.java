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

import io.github.dboncioaga.cui4j.anaf.AnafClient;
import io.github.dboncioaga.cui4j.anaf.DefaultAnafClient;
import io.github.dboncioaga.cui4j.core.CuiValidator;
import io.github.dboncioaga.cui4j.core.DefaultCuiValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for CUI4J.
 * <p>
 * This configuration provides:
 * <ul>
 *   <li>{@link CuiValidator} - always available for CUI/CIF validation</li>
 *   <li>{@link AnafClient} - conditionally available when ANAF integration is enabled</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(Cui4jProperties.class)
public class Cui4jAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(Cui4jAutoConfiguration.class);

    /**
     * Provides a CUI validator bean.
     * <p>
     * This bean is always available and can be used for validating Romanian CUI/CIF numbers.
     *
     * @return the CUI validator
     */
    @Bean
    @ConditionalOnMissingBean
    public CuiValidator cuiValidator() {
        LOG.info("Configuring CUI4J validator");
        return new DefaultCuiValidator();
    }

    /**
     * Provides an ANAF client bean when ANAF integration is enabled.
     * <p>
     * This bean is conditionally created only when:
     * <ul>
     *   <li>The {@link AnafClient} class is on the classpath</li>
     *   <li>The property {@code cui4j.anaf.enabled} is true (default)</li>
     *   <li>No custom {@link AnafClient} bean is already defined</li>
     * </ul>
     *
     * @param properties the CUI4J configuration properties
     * @return the ANAF client
     */
    @Bean
    @ConditionalOnClass(AnafClient.class)
    @ConditionalOnProperty(prefix = "cui4j.anaf", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AnafClient anafClient(Cui4jProperties properties) {
        Cui4jProperties.Anaf anafConfig = properties.getAnaf();
        
        LOG.info("Configuring CUI4J ANAF client with base URL: {}", anafConfig.getBaseUrl());
        LOG.debug("ANAF configuration: timeout={}ms, maxRetries={}, maxBatchSize={}", 
            anafConfig.getTimeout().toMillis(),
            anafConfig.getMaxRetries(),
            anafConfig.getMaxBatchSize());

        return new DefaultAnafClient(
            anafConfig.getBaseUrl(),
            anafConfig.getTimeout(),
            anafConfig.getMaxRetries(),
            anafConfig.getMaxBatchSize()
        );
    }
}
