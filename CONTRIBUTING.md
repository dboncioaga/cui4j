# Contributing to cui4j

Thank you for your interest in contributing to cui4j! This document provides guidelines and instructions for contributing.

## Code of Conduct

Be respectful, professional, and constructive in all interactions.

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check existing issues. When creating a bug report, include:

- **Clear title** and description
- **Steps to reproduce** the behavior
- **Expected behavior**
- **Actual behavior**
- **Environment** (Java version, OS, etc.)
- **Code samples** if applicable

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title** and description
- **Use case** explaining why this enhancement would be useful
- **Examples** of how the enhancement would work

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `develop` (not `main`)
3. **Make your changes**
   - Follow existing code style
   - Add tests for new functionality
   - Update documentation as needed
4. **Run tests** locally: `mvn clean verify`
5. **Ensure coverage** stays above 85%
6. **Commit** with clear, descriptive messages
7. **Push** to your fork
8. **Open a Pull Request** to the `develop` branch

## Development Setup

### Prerequisites

- Java 25 or later
- Maven 3.9+
- Git

### Building

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/cui4j.git
cd cui4j

# Build and test
mvn clean verify

# Check coverage
mvn jacoco:report
# Open cui4j-core/target/site/jacoco/index.html in browser
```

### Running Tests

```bash
# All tests
mvn test

# Specific module
mvn test -pl cui4j-core

# Integration tests
mvn verify
```

## Code Style

### General

- **No abbreviations** in variable names (except standard ones like `i`, `j` for loops)
- **Constructor injection** over field injection
- **Immutable objects** where possible
- **Small, focused methods** (< 20 lines preferred)
- **SOLID principles**

### Java Conventions

```java
// Good - immutable, null-safe, proper error handling
public final class DefaultCuiValidator implements CuiValidator {
    
    private static final int MAX_DIGITS = 10;
    
    @Override
    public ValidationResult validate(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return ValidationResult.failure("CUI cannot be null or blank");
        }
        // ...
    }
}

// Good - using RestClient for synchronous HTTP calls
public final class DefaultAnafClient implements AnafClient {
    
    private final RestClient restClient;
    private final CuiValidator cuiValidator;
    
    public DefaultAnafClient(RestClient restClient, CuiValidator cuiValidator) {
        this.restClient = restClient;
        this.cuiValidator = cuiValidator;
    }
}

// Bad - mutable, no null check, no error handling
public class CuiValidator {
    private int maxDigits = 10;
    
    public boolean validate(String input) {
        return input.length() <= maxDigits;
    }
}
```

**Key Principles:**
- Use **RestClient** (not WebClient) for HTTP calls - synchronous, simpler
- Use **records** for immutable DTOs
- Use **@Nullable** annotations from JSpecify
- Use **constructor injection** (final fields)
- Use **Spring Boot 4.0** features and APIs

### Documentation

- **Public APIs** must have Javadoc
- **Complex logic** should have inline comments
- **Examples** in Javadoc where helpful
- **@param**, **@return**, **@throws** tags required

```java
/**
 * Validates a Romanian CUI/CIF number.
 * <p>
 * This method performs checksum validation using the official Romanian
 * algorithm and supports optional "RO" prefix.
 *
 * @param input the CUI/CIF to validate (may contain spaces and separators)
 * @return validation result with normalized CUI or error message
 * @throws IllegalArgumentException if input format is completely invalid
 */
@Override
public ValidationResult validate(@Nullable String input) {
    // ...
}
```

### Testing

- **Test class per production class**
- **Nested test classes** for logical grouping
- **DisplayName** for readable test output
- **AAA pattern**: Arrange, Act, Assert
- **One assertion concept per test** (multiple assertions on same object OK)
- **WireMock** for HTTP API testing (with proper JPMS configuration)

```java
@DisplayName("DefaultCuiValidator")
class DefaultCuiValidatorTest {
    
    @Nested
    @DisplayName("Valid CUI Tests")
    class ValidCuiTests {
        
        @Test
        @DisplayName("should validate correct CUI")
        void shouldValidateCorrectCui() {
            // Arrange
            CuiValidator validator = new DefaultCuiValidator();
            
            // Act
            ValidationResult result = validator.validate("18547290");
            
            // Assert
            assertThat(result.valid()).isTrue();
            assertThat(result.normalizedCui()).isEqualTo("18547290");
        }
    }
}

// WireMock integration test example
@DisplayName("DefaultAnafClient")
class DefaultAnafClientTest {
    
    private WireMockServer wireMock;
    private DefaultAnafClient client;
    
    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        RestClient restClient = RestClient.builder()
            .baseUrl(wireMock.baseUrl())
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();
        
        client = new DefaultAnafClient(restClient, 500);
    }
    
    @Test
    @DisplayName("should successfully lookup valid CUI")
    void shouldSuccessfullyLookupValidCui() {
        // Arrange
        wireMock.stubFor(post(urlEqualTo("/PlatitorTvaRest/api/v8/ws/tva"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{...}")));
        
        // Act
        CompanyInfo result = client.lookup("18547290");
        
        // Assert
        assertThat(result.cui()).isEqualTo("18547290");
    }
}
```

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

### Examples

```
feat(core): add support for 2-digit CUI validation

Extend validation to support 2-digit CUIs as per Romanian law.
Previously only 8-10 digit CUIs were supported.

Closes #123
```

```
fix(anaf): handle null response from ANAF API

The ANAF API occasionally returns null responses. Handle this
gracefully by retrying with exponential backoff.

Fixes #456
```

## Project Structure

```
cui4j/
├── cui4j-core/              # Core validation (no dependencies)
│   ├── src/main/java/       # Production code
│   │   └── module-info.java # JPMS module descriptor
│   ├── src/test/java/       # Tests
│   └── pom.xml
├── cui4j-anaf/              # ANAF integration
│   ├── src/main/java/
│   │   └── module-info.java # JPMS module descriptor
│   ├── src/test/java/
│   └── pom.xml
├── cui4j-spring-boot-starter/  # Spring Boot auto-config
│   ├── src/main/java/
│   │   └── module-info.java # JPMS module descriptor
│   ├── src/main/resources/
│   │   └── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   ├── src/test/java/
│   └── pom.xml
├── .github/workflows/       # CI/CD
├── pom.xml                  # Parent POM
└── README.md
```

### JPMS Modules

This project uses the Java Platform Module System (JPMS). Each module has a `module-info.java`:

**Key considerations:**
- Export only public API packages
- Use `requires static transitive` for JSpecify annotations
- Use `--add-opens` in test configuration for framework access (Jackson, Spring)
- Automatic modules (Spring) require special compiler args to suppress warnings

Example `module-info.java`:
```java
module io.github.dboncioaga.cui4j.core {
    requires static transitive org.jspecify;
    exports io.github.dboncioaga.cui4j.core;
}
```

## Code Quality

The project uses static analysis tools to maintain code quality:

- **Checkstyle**: Google Java Style with custom rules
- **PMD**: Static code analyzer for finding common issues
- **JaCoCo**: Code coverage tool (minimum 85% for core modules, 75% for ANAF)

Run quality checks locally:

```bash
# Run all quality checks
mvn verify

# Run Checkstyle only
mvn checkstyle:check

# Run PMD only
mvn pmd:check

# Generate coverage report
mvn jacoco:report
# Open target/site/jacoco/index.html
```

## Release Process

Releases are automated via GitHub Actions:

1. Ensure all tests pass and coverage requirements are met
2. Create and push a tag: `git tag -a v0.2.0 -m "Release 0.2.0"`
3. Push tag: `git push origin v0.2.0`
4. GitHub Actions will:
   - Run tests and quality checks
   - Build artifacts
   - Deploy to Maven Central via central-publishing-plugin
   - Create GitHub release with changelog

## Questions?

Feel free to open a [Discussion](https://github.com/dboncioaga/cui4j/discussions) or reach out via [Issues](https://github.com/dboncioaga/cui4j/issues).

Thank you for contributing! 🙏
