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
 * Romanian CUI/CIF validation core module.
 * <p>
 * This module provides validation for Romanian CUI (Cod Unic de Identificare)
 * and CIF (Cod de Identificare Fiscală) numbers.
 */
module io.github.dboncioaga.cui4j.core {
    requires static transitive org.jspecify;

    exports io.github.dboncioaga.cui4j.core;
}
