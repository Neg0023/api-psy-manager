# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1 - build
# Java 17 to match <java.version>17</java.version> in pom.xml.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Dependency layer: only invalidated when pom.xml changes.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline -DskipTests

# Source layer.
COPY src ./src

# Tests are skipped on purpose: they require a live Postgres (Neon) and real
# Google credentials, which must not be present at image build time.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests && \
    cp target/*.jar /build/app.jar

# ---------------------------------------------------------------------------
# Stage 2 - runtime
# JRE-only (no compiler/Maven) for a smaller surface area.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

# Non-root, no login shell, no home-dir clutter.
RUN groupadd --system --gid 1001 app && \
    useradd --system --uid 1001 --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=app:app /build/app.jar /app/app.jar

USER app

EXPOSE 8080

# MaxRAMPercentage lets the JVM size the heap from the container memory limit
# instead of the host's total RAM. Extra flags can be appended via JAVA_OPTS.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# exec so the JVM is PID 1 and receives SIGTERM for graceful shutdown.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
