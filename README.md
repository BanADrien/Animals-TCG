# Animals-TCG

Jeu de cartes a collectionner avec des animaux. Les animaux viennent de l'API
[iNaturalist](https://api.inaturalist.org/v1/docs/) (exemple : https://api.inaturalist.org/v1/taxa?q=fox)
et les cartes sont enregistrees dans une base MySQL.

Projet Java en binome : Spring Boot, Spring Data JPA, Thymeleaf, MySQL avec docker compose.

## Prerequis

- Java 27
- Docker Desktop

## Lancer le projet

```
docker compose up -d
mvnw.cmd spring-boot:run
```

Puis ouvrir http://localhost:8080

Les tests : `mvnw.cmd test`
