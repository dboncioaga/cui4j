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

/**
 * Romanian ANAF (National Agency for Fiscal Administration) REST API integration module.
 * <p>
 * This module provides integration with the ANAF public TVA (VAT) REST API
 * to query company tax information.
 */
module io.github.dboncioaga.cui4j.anaf {
    requires io.github.dboncioaga.cui4j.core;
    requires static transitive org.jspecify;
    requires spring.web;
    requires spring.beans;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;

    exports io.github.dboncioaga.cui4j.anaf;
}
