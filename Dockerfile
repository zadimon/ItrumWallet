FROM eclipse-temurin:17.0.18_8-jdk-ubi9-minimal AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN chmod +x gradlew && ./gradlew clean build

FROM eclipse-temurin:17.0.18_8-jre-ubi9-minimal

WORKDIR /app
COPY --from=builder /app/build/libs/ItrumWallet-*.jar ItrumWallet.jar
EXPOSE 8080

CMD ["java", "-jar", "ItrumWallet.jar"]