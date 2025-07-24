# --------- Stage 1: Build Angular frontend ---------
FROM node:20.19.1 AS frontend-build

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install

COPY frontend/ .
RUN npm run build --prod

# --------- Stage 2: Build Spring Boot backend ---------
FROM maven:3.9.4-eclipse-temurin-17 AS backend-build

WORKDIR /app
COPY . .

# change directory to backend
WORKDIR /app/backend
RUN mvn clean install -DskipTests

# --------- Stage 3: Final container ---------
FROM eclipse-temurin:17-jdk AS final

WORKDIR /app

# Copy built Angular static files into Spring Boot's static path
COPY --from=frontend-build /app/frontend/dist/frontend/browser /app/static/

# Copy the built backend JAR
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]