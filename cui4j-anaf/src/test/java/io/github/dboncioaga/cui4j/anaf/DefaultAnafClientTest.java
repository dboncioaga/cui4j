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

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

@DisplayName("DefaultAnafClient")
class DefaultAnafClientTest {

    private WireMockServer wireMock;
    private AnafClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        String baseUrl = wireMock.baseUrl();
        RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory())
            .build();
        
        client = new DefaultAnafClient(restClient, 500);
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    @DisplayName("should successfully lookup a valid CUI")
    void shouldSuccessfullyLookupValidCui() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "cod": 200,
                      "message": "SUCCESS",
                      "found": [{
                        "date_generale": {
                          "cui": 18547290,
                          "data": "2026-01-19",
                          "denumire": "TEST COMPANY SRL",
                          "adresa": "BUCURESTI, SECTOR 1",
                          "nrRegCom": "J40/1234/2020",
                          "telefon": "0211234567",
                          "codPostal": "010101",
                          "stare_inregistrare": "INREGISTRAT",
                          "data_inregistrare": "2020-01-15",
                          "scpTVA": true,
                          "data_inceput_ScpTVA": "2020-02-01",
                          "statusInactivi": false
                        }
                      }],
                      "notfound": []
                    }
                    """)));

        // When
        CompanyInfo result = client.lookup("18547290");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.foundInAnafRegistry()).isTrue();
        assertThat(result.cui()).isEqualTo(18547290L);
        assertThat(result.companyName()).isEqualTo("TEST COMPANY SRL");
        assertThat(result.address()).isEqualTo("BUCURESTI, SECTOR 1");
        assertThat(result.phoneNumber()).isEqualTo("0211234567");
        assertThat(result.postalCode()).isEqualTo("010101");
        assertThat(result.isVatPayer()).isTrue();
        assertThat(result.isInactive()).isFalse();
        
        wireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/")));
    }

    @Test
    @DisplayName("should handle CUI not found in registry")
    void shouldHandleCuiNotFound() {
        // Given  
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "cod": 200,
                      "message": "SUCCESS",
                      "found": [],
                      "notfound": [{
                        "cui": 10000008,
                        "data": "2026-01-19"
                      }]
                    }
                    """)));

        // When
        CompanyInfo result = client.lookup("10000008");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.foundInAnafRegistry()).isFalse();
        assertThat(result.cui()).isEqualTo(10000008L);
        assertThat(result.companyName()).isNull();
    }

    @Test
    @DisplayName("should successfully lookup batch of CUIs")
    void shouldSuccessfullyLookupBatch() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "cod": 200,
                      "message": "SUCCESS",
                      "found": [{
                        "date_generale": {
                          "cui": 18547290,
                          "data": "2026-01-19",
                          "denumire": "COMPANY ONE SRL",
                          "scpTVA": true,
                          "statusInactivi": false
                        }
                      }],
                      "notfound": [{
                        "cui": 10000008,
                        "data": "2026-01-19"
                      }]
                    }
                    """)));

        // When
        List<CompanyInfo> results = client.lookupBatch(List.of("18547290", "10000008"));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).foundInAnafRegistry()).isTrue();
        assertThat(results.get(0).companyName()).isEqualTo("COMPANY ONE SRL");
        assertThat(results.get(1).foundInAnafRegistry()).isFalse();
    }

    @Test
    @DisplayName("should handle CUI with RO prefix")
    void shouldHandleCuiWithRoPrefix() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "cod": 200,
                      "message": "SUCCESS",
                      "found": [{
                        "date_generale": {
                          "cui": 18547290,
                          "data": "2026-01-19",
                          "denumire": "TEST COMPANY",
                          "scpTVA": true,
                          "statusInactivi": false
                        }
                      }],
                      "notfound": []
                    }
                    """)));

        // When
        CompanyInfo result = client.lookup("RO18547290");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.foundInAnafRegistry()).isTrue();
        assertThat(result.cui()).isEqualTo(18547290L);
    }

    @Test
    @DisplayName("should reject invalid CUI")
    void shouldRejectInvalidCui() {
        // When/Then
        assertThatThrownBy(() -> client.lookup("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid CUI");
    }

    @Test
    @DisplayName("should reject null CUI list")
    void shouldRejectNullCuiList() {
        // When/Then
        assertThatThrownBy(() -> client.lookupBatch(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("should reject empty CUI list")
    void shouldRejectEmptyCuiList() {
        // When/Then
        assertThatThrownBy(() -> client.lookupBatch(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("should reject batch size exceeding maximum")
    void shouldRejectOversizedBatch() {
        // Given
        List<String> largeBatch = java.util.stream.IntStream.range(0, 501)
            .mapToObj(i -> "18547290")
            .toList();

        // When/Then
        assertThatThrownBy(() -> client.lookupBatch(largeBatch))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds maximum");
    }

    @Test
    @DisplayName("should handle server error with exception")
    void shouldHandleServerError() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Bad Gateway\"}")));

        // When/Then
        assertThatThrownBy(() -> client.lookup("18547290"))
            .isInstanceOf(AnafClientException.class)
            .hasMessageContaining("Failed to query ANAF API");
    }

    @Test
    @DisplayName("should handle timeout")
    void shouldHandleTimeout() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(15000)
                .withBody("{}")));

        // When/Then
        assertThatThrownBy(() -> client.lookup("18547290"))
            .isInstanceOf(AnafClientException.class);
    }

    @Test
    @DisplayName("should handle invalid JSON response")
    void shouldHandleInvalidJson() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("invalid json")));

        // When/Then
        assertThatThrownBy(() -> client.lookup("18547290"))
            .isInstanceOf(AnafClientException.class);
    }
}
