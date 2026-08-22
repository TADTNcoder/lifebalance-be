package com.lifebalance.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ProductionConfigurationTest {

    private static final Pattern PROMETHEUS_TARGET =
            Pattern.compile("targets:\\s*\\[\"([^\"]+):8080\"\\]");
    private static final Set<String> EXPECTED_PROMETHEUS_TARGETS = Set.of(
            "discovery-server",
            "gateway",
            "identity-service",
            "task-service",
            "timeline-service",
            "resource-capital-service",
            "finance-service",
            "notification-service",
            "analytics-service",
            "ai-service"
    );
    private static final Set<String> JPA_MODULES = Set.of(
            "identity-service",
            "task-service",
            "timeline-service",
            "resource-capital-service",
            "finance-service",
            "notification-service",
            "analytics-service",
            "ai-service"
    );
    private static final Map<String, String> STABLE_PAGE_SERIALIZATION_APPS = Map.of(
            "identity-service", "src/main/java/com/lifebalance/identity/IdentityServiceApplication.java",
            "task-service", "src/main/java/com/lifebalance/task/TaskServiceApplication.java"
    );

    @Test
    void prometheusScrapeTargetsHaveRegistryAndExportEnabled() throws IOException {
        Path root = projectRoot();
        Set<String> targets = prometheusTargets(root);

        assertThat(targets).containsAll(EXPECTED_PROMETHEUS_TARGETS);

        for (String target : targets) {
            Path module = root.resolve(target);
            assertThat(module).isDirectory();

            String pom = Files.readString(module.resolve("pom.xml"));
            assertThat(pom)
                    .as(target + " must include the Prometheus registry")
                    .contains("<artifactId>micrometer-registry-prometheus</artifactId>");
            assertThat(pom)
                    .as(target + " must include Caffeine for production LoadBalancer cache")
                    .contains("<artifactId>caffeine</artifactId>");

            String applicationYaml = Files.readString(
                    module.resolve("src/main/resources/application.yaml")
            );
            assertThat(applicationYaml)
                    .as(target + " must enable Prometheus export")
                    .contains("prometheus:")
                    .contains("metrics:")
                    .contains("export:")
                    .contains("enabled: true")
                    .contains("include: health,info");
        }
    }

    @Test
    void jpaServicesDisableOpenInView() throws IOException {
        Path root = projectRoot();

        for (String moduleName : JPA_MODULES) {
            String applicationYaml = Files.readString(
                    root.resolve(moduleName).resolve("src/main/resources/application.yaml")
            );

            assertThat(applicationYaml)
                    .as(moduleName + " must not keep database sessions open during view rendering")
                    .contains("jpa:")
                    .contains("open-in-view: false");
        }
    }

    @Test
    void servicesReturningPagesUseStableSerializationMode() throws IOException {
        Path root = projectRoot();

        for (Map.Entry<String, String> application : STABLE_PAGE_SERIALIZATION_APPS.entrySet()) {
            String source = Files.readString(root.resolve(application.getKey()).resolve(application.getValue()));

            assertThat(source)
                    .as(application.getKey() + " must avoid direct PageImpl JSON serialization")
                    .contains("@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)");
        }
    }

    @Test
    void dockerDevProfilesUseInternalDiscoveryPort() throws IOException {
        Path root = projectRoot();

        assertThat(Files.readString(root.resolve(".env.example")))
                .as(".env.example")
                .doesNotContain("http://discovery-server:8761/eureka");

        try (Stream<Path> paths = Files.walk(root, 4)) {
            paths
                    .filter(path -> path.getFileName().toString().equals("application-dev.yaml"))
                    .filter(path -> !path.toString().contains("\\target\\"))
                    .filter(path -> !path.toString().contains("/target/"))
                    .forEach(path -> assertThat(read(path))
                            .as(root.relativize(path).toString())
                            .doesNotContain("http://discovery-server:8761/eureka"));
        }
    }

    @Test
    void productionComposeResetsInheritedPortMappings() throws IOException {
        Path root = projectRoot();
        String productionCompose = Files.readString(root.resolve("compose.prod.yaml"));

        assertThat(productionCompose)
                .contains("ports: !reset []")
                .doesNotContain("ports: []");
    }

    private Set<String> prometheusTargets(Path root) throws IOException {
        String prometheusConfig = Files.readString(root.resolve("docker/prometheus/prometheus.yml"));
        Matcher matcher = PROMETHEUS_TARGET.matcher(prometheusConfig);
        Set<String> targets = new LinkedHashSet<>();

        while (matcher.find()) {
            targets.add(matcher.group(1));
        }

        return targets;
    }

    private Path projectRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();

        while (current != null) {
            if (Files.exists(current.resolve("compose.yaml"))
                    && Files.exists(current.resolve("docker/prometheus/prometheus.yml"))) {
                return current;
            }
            current = current.getParent();
        }

        fail("Project root with compose.yaml was not found");
        return Path.of(".");
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }
}
