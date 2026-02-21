# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1) Compilar e instalar common-security
COPY common-security/pom.xml common-security/pom.xml
COPY common-security/src common-security/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f common-security/pom.xml -DskipTests install

# 2) Build del backend
COPY blschool-intranet-backend/pom.xml blschool-intranet-backend/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f blschool-intranet-backend/pom.xml -DskipTests dependency:go-offline || true

COPY blschool-intranet-backend/src blschool-intranet-backend/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f blschool-intranet-backend/pom.xml -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
ENV TZ=America/Argentina/Cordoba \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Duser.timezone=America/Argentina/Cordoba"

WORKDIR /opt/app
COPY --from=build /workspace/blschool-intranet-backend/target/*.jar /opt/app/app.jar

RUN useradd -r -u 10001 -g root appuser && chown -R appuser:root /opt/app
USER appuser

EXPOSE 8091
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /opt/app/app.jar"]
