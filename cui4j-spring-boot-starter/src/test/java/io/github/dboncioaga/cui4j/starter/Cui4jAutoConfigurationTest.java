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
import io.github.dboncioaga.cui4j.core.CuiValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cui4jAutoConfiguration")
class Cui4jAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(Cui4jAutoConfiguration.class));

    @Test
    @DisplayName("should auto-configure CuiValidator bean")
    void shouldAutoConfigureCuiValidator() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CuiValidator.class);
            CuiValidator validator = context.getBean(CuiValidator.class);
            assertThat(validator).isNotNull();
        });
    }

    @Test
    @DisplayName("should auto-configure AnafClient bean when enabled")
    void shouldAutoConfigureAnafClientWhenEnabled() {
        contextRunner
            .withPropertyValues("cui4j.anaf.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(AnafClient.class);
                AnafClient client = context.getBean(AnafClient.class);
                assertThat(client).isNotNull();
            });
    }

    @Test
    @DisplayName("should not auto-configure AnafClient when disabled")
    void shouldNotAutoConfigureAnafClientWhenDisabled() {
        contextRunner
            .withPropertyValues("cui4j.anaf.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AnafClient.class);
            });
    }

    @Test
    @DisplayName("should auto-configure AnafClient by default")
    void shouldAutoConfigureAnafClientByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AnafClient.class);
        });
    }

    @Test
    @DisplayName("should use custom properties")
    void shouldUseCustomProperties() {
        contextRunner
            .withPropertyValues(
                "cui4j.anaf.timeout=5s",
                "cui4j.anaf.max-retries=3",
                "cui4j.anaf.max-batch-size=100"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(Cui4jProperties.class);
                Cui4jProperties properties = context.getBean(Cui4jProperties.class);
                assertThat(properties.getAnaf().getTimeout().getSeconds()).isEqualTo(5);
                assertThat(properties.getAnaf().getMaxRetries()).isEqualTo(3);
                assertThat(properties.getAnaf().getMaxBatchSize()).isEqualTo(100);
            });
    }

    @Test
    @DisplayName("should configure all ANAF properties")
    void shouldConfigureAllAnafProperties() {
        contextRunner
            .withPropertyValues(
                "cui4j.anaf.enabled=true",
                "cui4j.anaf.timeout=5s",
                "cui4j.anaf.max-retries=3",
                "cui4j.anaf.max-batch-size=100",
                "cui4j.anaf.cache-ttl=12h",
                "cui4j.anaf.rate-limit=5",
                "cui4j.anaf.base-url=http://test.example.com"
            )
            .run(context -> {
                Cui4jProperties properties = context.getBean(Cui4jProperties.class);
                Cui4jProperties.Anaf anaf = properties.getAnaf();
                
                assertThat(anaf.isEnabled()).isTrue();
                assertThat(anaf.getTimeout().getSeconds()).isEqualTo(5);
                assertThat(anaf.getMaxRetries()).isEqualTo(3);
                assertThat(anaf.getMaxBatchSize()).isEqualTo(100);
                assertThat(anaf.getCacheTtl().toHours()).isEqualTo(12);
                assertThat(anaf.getRateLimit()).isEqualTo(5);
                assertThat(anaf.getBaseUrl()).isEqualTo("http://test.example.com");
            });
    }

    @Test
    @DisplayName("should use default ANAF properties when not specified")
    void shouldUseDefaultAnafProperties() {
        contextRunner.run(context -> {
            Cui4jProperties properties = context.getBean(Cui4jProperties.class);
            Cui4jProperties.Anaf anaf = properties.getAnaf();
            
            assertThat(anaf.isEnabled()).isTrue();
            assertThat(anaf.getTimeout().getSeconds()).isEqualTo(10);
            assertThat(anaf.getMaxRetries()).isEqualTo(2);
            assertThat(anaf.getMaxBatchSize()).isEqualTo(500);
            assertThat(anaf.getCacheTtl().toHours()).isEqualTo(24);
            assertThat(anaf.getRateLimit()).isEqualTo(2);
            assertThat(anaf.getBaseUrl()).isEqualTo("https://webservicesp.anaf.ro/PlatitorTvaRest/api/v9/ws/tva");
        });
    }
}
