FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-noble
ARG MODULE
WORKDIR /app
RUN sed -i 's|http://|https://|g' /etc/apt/sources.list.d/ubuntu.sources \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && install -d -o 10001 -g 10001 -m 0750 /app/data
COPY --from=build /src/${MODULE}/target/${MODULE}-1.0.0.jar /app/app.jar
USER 10001:10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
