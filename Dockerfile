# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Better caching
COPY pom.xml .
COPY src ./src

# If your CI already runs verify, consider `clean package -DskipTests` instead.
RUN mvn -B -ntp clean verify

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Run as non-root
RUN useradd -r -u 1001 appuser
USER 1001

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
