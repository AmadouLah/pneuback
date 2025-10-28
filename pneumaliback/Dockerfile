# Étape 1 : Build du projet
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src ./src
RUN ./mvnw -q -DskipTests package

# Étape 2 : Image finale
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS="-Xmx384m -Xms192m"
EXPOSE 9999
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
