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
package io.github.dboncioaga.cui4j.anaf.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Internal DTO for ANAF API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnafResponse(
    @JsonProperty("cod") @Nullable Integer statusCode,
    @JsonProperty("message") @Nullable String message,
    @JsonProperty("found") @Nullable List<AnafCompanyData> found,
    @JsonProperty("notfound") @Nullable List<AnafNotFoundData> notFound
) {
}
