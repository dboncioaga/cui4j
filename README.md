# cui4j

[![Build](https://github.com/dboncioaga/cui4j/workflows/Build/badge.svg)](https://github.com/dboncioaga/cui4j/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dboncioaga/cui4j.svg)](https://search.maven.org/artifact/io.github.dboncioaga/cui4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage](https://codecov.io/gh/dboncioaga/cui4j/branch/main/graph/badge.svg)](https://codecov.io/gh/dboncioaga/cui4j)

Romanian CUI/CIF validation and ANAF integration SDK for Java.

## Overview

**cui4j** is a production-grade Java SDK that provides:

- ✅ **CUI/CIF Validation**: Validates Romanian CUI (Cod Unic de Identificare) and CIF (Cod de Identificare Fiscală) numbers using the official checksum algorithm
- 🔗 **ANAF Integration**: Integrates with the Romanian National Agency for Fiscal Administration (ANAF) public TVA REST API
- 🚀 **Spring Boot Support**: Auto-configuration for seamless Spring Boot integration
- 📦 **Zero Dependencies Core**: The validation module has no external dependencies
- 🔒 **Null-Safe**: Uses JSpecify annotations for enhanced null safety
- 📘 **Fully Documented**: Complete Javadoc coverage

## Requirements

- Java 25 or later
- Maven 3.9+ (for building from source)
- Spring Boot 4.0+ (optional, for Spring Boot integration)

## Installation

### Maven

Add the appropriate dependency to your `pom.xml`:

#### Core Validation Only

```xml
<dependency>
    <groupId>io.github.dboncioaga</groupId>
    <artifactId>cui4j-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### ANAF Integration

```xml
<dependency>
    <groupId>io.github.dboncioaga</groupId>
    <artifactId>cui4j-anaf</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Spring Boot Starter (includes all modules)

```xml
<dependency>
    <groupId>io.github.dboncioaga</groupId>
    <artifactId>cui4j-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```gradle
// Core validation only
implementation 'io.github.dboncioaga:cui4j-core:0.1.0'

// ANAF integration
implementation 'io.github.dboncioaga:cui4j-anaf:0.1.0'

// Spring Boot Starter
implementation 'io.github.dboncioaga:cui4j-spring-boot-starter:0.1.0'
```

## Quick Start

### Basic CUI Validation

```java
import io.github.dboncioaga.cui4j.core.CuiValidator;
import io.github.dboncioaga.cui4j.core.DefaultCuiValidator;
import io.github.dboncioaga.cui4j.core.ValidationResult;

public class Example {
    public static void main(String[] args) {
        CuiValidator validator = new DefaultCuiValidator();
        
        // Validate a CUI
        ValidationResult result = validator.validate("18547290");
        
        if (result.valid()) {
            System.out.println("Valid CUI: " + result.normalizedCui());
            System.out.println("Has RO prefix: " + result.vatPrefixPresent());
        } else {
            System.out.println("Invalid: " + result.errorMessage());
        }
        
        // Supports RO prefix
        ValidationResult withPrefix = validator.validate("RO18547290");
        System.out.println("Valid with prefix: " + withPrefix.valid());
        
        // Handles separators
        ValidationResult withSpaces = validator.validate("18 547 290");
        System.out.println("Valid with spaces: " + withSpaces.valid());
    }
}
```

### ANAF Integration

```java
import io.github.dboncioaga.cui4j.anaf.AnafClient;
import io.github.dboncioaga.cui4j.anaf.DefaultAnafClient;
import io.github.dboncioaga.cui4j.anaf.CompanyInfo;

public class AnafExample {
    public static void main(String[] args) {
        AnafClient client = new DefaultAnafClient();
        
        // Lookup single CUI
        CompanyInfo company = client.lookup("18547290");
        
        if (company.foundInAnafRegistry()) {
            System.out.println("Company: " + company.companyName());
            System.out.println("Address: " + company.address());
            System.out.println("VAT Payer: " + company.isVatPayer());
        }
        
        // Batch lookup (more efficient)
        List<CompanyInfo> companies = client.lookupBatch(
            List.of("18547290", "RO10000008", "27")
        );
    }
}
```

### Spring Boot Integration

Add the starter dependency and configure in `application.yml`:

```yaml
cui4j:
  anaf:
    enabled: true
    timeout: 10s
    max-retries: 2
    max-batch-size: 500
    cache-ttl: 24h
    rate-limit: 2
```

Inject and use the beans:

```java
import io.github.dboncioaga.cui4j.core.CuiValidator;
import io.github.dboncioaga.cui4j.anaf.AnafClient;
import org.springframework.stereotype.Service;

@Service
public class TaxService {
    
    private final CuiValidator cuiValidator;
    private final AnafClient anafClient;
    
    public TaxService(CuiValidator cuiValidator, AnafClient anafClient) {
        this.cuiValidator = cuiValidator;
        this.anafClient = anafClient;
    }
    
    public CompanyInfo verifyCompany(String cui) {
        // Validate first
        ValidationResult validation = cuiValidator.validate(cui);
        if (!validation.valid()) {
            throw new IllegalArgumentException("Invalid CUI: " + validation.errorMessage());
        }
        
        // Query ANAF
        return anafClient.lookup(cui);
    }
}
```

### Disable ANAF Integration

If you only need validation without ANAF integration:

```yaml
cui4j:
  anaf:
    enabled: false
```

## Features

### CUI/CIF Validation

- Validates 2-10 digit CUIs according to Romanian legislation
- Supports optional "RO" prefix (case-insensitive)
- Automatically removes spaces and common separators (-, _, ., /)
- Uses the official Romanian checksum algorithm
- Comprehensive error messages

**Validation Rules:**
1. CUI must contain 2-10 digits
2. Optional "RO" prefix is supported
3. Checksum validation using Romanian algorithm with key: `[7, 5, 3, 2, 1, 7, 5, 3, 2]`

### ANAF REST API Integration

The `cui4j-anaf` module provides integration with ANAF's public TVA (VAT) REST API:

- Query company tax information
- Batch lookups for efficiency
- Automatic retries with exponential backoff
- Configurable timeouts
- Null-safe response mapping
- Comprehensive error handling
- **Synchronous blocking HTTP calls** using Spring RestClient

**API Endpoint:**
```
POST https://webservicesp.anaf.ro/PlatitorTvaRest/api/v9/ws/tva
```

**Retrieved Information:**
- Company name and registration details
- VAT registration status
- Split VAT regime status
- Company address and contact information
- Registration dates
- Inactive status

### Spring Boot Auto-Configuration

The starter provides:
- Automatic bean configuration for `CuiValidator` and `AnafClient`
- Configuration properties binding
- Conditional bean creation based on properties
- Seamless integration with Spring Boot applications

## Configuration Reference

All configuration properties for the Spring Boot starter:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `cui4j.anaf.enabled` | boolean | `true` | Enable/disable ANAF integration |
| `cui4j.anaf.timeout` | Duration | `10s` | Request timeout for ANAF API calls |
| `cui4j.anaf.max-retries` | int | `2` | Maximum number of retries for failed requests |
| `cui4j.anaf.max-batch-size` | int | `500` | Maximum number of CUIs in a single batch request |
| `cui4j.anaf.cache-ttl` | Duration | `24h` | Cache time-to-live for responses (not yet implemented) |
| `cui4j.anaf.rate-limit` | int | `2` | Rate limit in requests per second (not yet implemented) |
| `cui4j.anaf.base-url` | String | ANAF prod URL | Base URL for ANAF API (for testing purposes) |

## ANAF API Limitations

⚠️ **Important Notes about ANAF API:**

1. **Rate Limiting**: The ANAF API may have rate limits. Be respectful and implement appropriate throttling.
2. **Availability**: The API may experience downtime. Always implement proper error handling.
3. **Data Currency**: Company information is updated periodically by ANAF. Data may not be real-time.
4. **Batch Limits**: While the API supports batching, sending too many requests simultaneously may result in throttling.
5. **No Authentication Required**: The API is public and doesn't require authentication.

## Legal Disclaimer

⚠️ **IMPORTANT:**

- This library provides **validation** of CUI/CIF format and checksum only
- ANAF integration queries **publicly available** tax information
- Always verify critical information through official ANAF channels
- This library is **not affiliated with or endorsed by ANAF**
- Use of ANAF data must comply with Romanian data protection laws
- The maintainers assume **no liability** for incorrect or outdated data

## Module Structure

```
cui4j/
├── cui4j-core/                    # Core validation (no dependencies)
├── cui4j-anaf/                    # ANAF REST API integration
└── cui4j-spring-boot-starter/     # Spring Boot auto-configuration
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/dboncioaga/cui4j.git
cd cui4j

# Build with Maven
mvn clean install

# Run tests
mvn test

# Generate coverage report
mvn verify
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Java 21 or later
2. Maven 3.9+
3. Your favorite IDE with Java support

### Code Style

- Constructor injection (no field injection)
- Immutable DTOs where possible
- Small, focused classes following SOLID principles
- Comprehensive test coverage (minimum 85%)

## Versioning

This project follows [Semantic Versioning](https://semver.org/).

| Version | Status | Description |
|---------|--------|-------------|
| 0.1.0 | ✅ Current | Core CUI validation stable |
| 0.2.0 | 🔄 Planned | ANAF integration stable |
| 0.3.0 | 📋 Planned | Spring Boot starter stable |
| 1.0.0 | 🎯 Goal | Production ready |

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Credits

Created and maintained by [Daniel Boncioaga](https://github.com/dboncioaga).

## Related Projects

- [cnp4j](https://github.com/dboncioaga/cnp4j) - Romanian CNP (Personal Numerical Code) validation SDK

## Support

- 📖 [Documentation](https://github.com/dboncioaga/cui4j/wiki)
- 🐛 [Issue Tracker](https://github.com/dboncioaga/cui4j/issues)
- 💬 [Discussions](https://github.com/dboncioaga/cui4j/discussions)

---

Made with ☕ in Romania
