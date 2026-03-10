# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom first to leverage docker cache
COPY pom.xml .

# download deps (cacheable layer)
RUN mvn -q -DskipTests dependency:go-offline || true

#copy sources and build
COPY src ./src
RUN mvn -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Run as non-root
RUN useradd -r -u 1001 appuser
USER 1001

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
