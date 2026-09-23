# === FAZA 1: build ===
# Koristimo poseban "builder" sloj sa Maven-om samo da kompajliramo .jar -
# finalni image (faza 2) nece uopste sadrzati Maven ni izvorni kod, samo
# gotov .jar. Ovo drasticno smanjuje finalnu velicinu image-a.
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Prvo samo pom.xml - isti trik kao kod ML servisa: Docker kesira ovaj
# korak, pa se zavisnosti ne povlace ponovo ako se menja samo Java kod.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# === FAZA 2: pokretanje ===
# Mnogo manja bazna slika - samo JRE (ne treba nam JDK/Maven za pokretanje
# vec gotovog .jar-a).
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Kopira SAMO gotov .jar iz builder faze, nista drugo iz nje ne prelazi ovde.
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
