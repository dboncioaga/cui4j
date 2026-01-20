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

/**
 * Internal DTO for general company data from ANAF API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnafGeneralData(
    @JsonProperty("cui") @Nullable Long cui,
    @JsonProperty("data") @Nullable String referenceDate,
    @JsonProperty("denumire") @Nullable String companyName,
    @JsonProperty("adresa") @Nullable String address,
    @JsonProperty("nrRegCom") @Nullable String tradeRegisterNumber,
    @JsonProperty("telefon") @Nullable String phoneNumber,
    @JsonProperty("fax") @Nullable String fax,
    @JsonProperty("codPostal") @Nullable String postalCode,
    @JsonProperty("act") @Nullable String act,
    @JsonProperty("stare_inregistrare") @Nullable String registrationState,
    @JsonProperty("data_inregistrare") @Nullable String registrationDate,
    @JsonProperty("cod_CAEN") @Nullable String caenCode,
    @JsonProperty("iban") @Nullable String iban,
    @JsonProperty("statusRO_e_Factura") @Nullable Boolean eInvoiceStatus,
    @JsonProperty("organFiscalCompetent") @Nullable String competentFiscalAuthority,
    @JsonProperty("forma_de_proprietate") @Nullable String ownershipForm,
    @JsonProperty("forma_organizare") @Nullable String organizationForm,
    @JsonProperty("forma_juridica") @Nullable String legalForm,
    @JsonProperty("scpTVA") @Nullable Boolean isVatPayer,
    @JsonProperty("data_inceput_ScpTVA") @Nullable String vatRegistrationDate,
    @JsonProperty("data_sfarsit_ScpTVA") @Nullable String vatEndDate,
    @JsonProperty("data_anul_imp_ScpTVA") @Nullable String vatAnnulDate,
    @JsonProperty("mesaj_ScpTVA") @Nullable String vatMessage,
    @JsonProperty("dataInceputTvaInc") @Nullable String splitVatStartDate,
    @JsonProperty("dataSfarsitTvaInc") @Nullable String splitVatEndDate,
    @JsonProperty("dataActualizare") @Nullable String updateDate,
    @JsonProperty("dataPublicare") @Nullable String publicationDate,
    @JsonProperty("dataRadiere") @Nullable String deregistrationDate,
    @JsonProperty("statusInactivi") @Nullable Boolean isInactive
) {
}
