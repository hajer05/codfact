# CODINGFACT — Plateforme d'apprentissage (FR)

Une plateforme LMS (Learning Management System) full‑stack composée de : une API back-end (Spring Boot), une interface admin `backoffice` (Angular) et une interface utilisateur `frontoffice` (Angular). Le système propose gestion de cours, PFE, blogging, paiements Stripe, chat/assistant IA et notifications temps réel.

## 📋 Sommaire

- Aperçu
- Architecture
- Rôles
- Fonctionnalités clés
- Prérequis
- Exécution locale
- Exécution via Docker
- Configuration / Variables sensibles
- Tests & Qualité
- Contribution

---

## 🎯 Aperçu

CODINGFACT est une solution pour créer, vendre et gérer des cours en ligne avec des services associés (PFE, consulting). L'architecture est modulaire pour faciliter le développement et le déploiement (microservices / conteneurs).

---

## 🏗 Architecture (simplifiée)

- Frontend
  - `frontoffice` (port 4201) — interface apprenant
  - `backoffice` (port 4200) — interface administrateur
- Backend
  - `learning-service` (port 8090) — API REST Spring Boot (JWT, JPA, WebSocket)
- Base de données
  - PostgreSQL (port 5432)
- Monitoring (optionnel)
  - Prometheus / Grafana

Schéma :

Client (4200 / 4201) -> API (8090) -> PostgreSQL (5432)

---

## 👥 Rôles

- **ADMIN** — gestion complète (utilisateurs, contenu, paiements, plaintes)
- **TEACHER** — crée et gère cours, quiz, modules, PFE
- **CONSULTANT** — gère demandes de consulting et PFE
- **STUDENT** — navigue, s’inscrit, achète des cours, passe des quiz

---

## ✨ Fonctionnalités principales

- Gestion complète des cours (modules, leçons, fichiers)
- Quizzes (génération possible via OpenAI)
- PFE : publication & candidature
- Panier et paiement Stripe (intégration côté backend & frontend)
- Authentification JWT + RBAC
- Chatbot / assistant (OpenAI)
- Notifications en temps réel (WebSocket)
- Téléversement de fichiers (dossiers `uploads/`)
- Monitoring : Actuator + Prometheus

---

## ⚙️ Prérequis

- Java 17
- Maven
- Node.js (18+) & npm
- PostgreSQL 15
- Docker & Docker Compose (si usage en conteneur)

---

## 🚀 Exécution locale (développement)

1) Base de données

- Créer la BDD PostgreSQL :

```powershell
# via psql
psql -U postgres
CREATE DATABASE learning_db; -- ou learning_db1 selon votre config
```

2) Backend (learning-service)

```powershell
cd learning-service
# ajuster src/main/resources/application.properties (url/user/password)
mvn clean install
mvn spring-boot:run
# ou après build
java -jar target/learning-service-0.0.1-SNAPSHOT.jar
# API : http://localhost:8090
```

3) Backoffice (admin)

```powershell
cd backoffice
npm install
npm start
# app en dev : http://localhost:4200
```

4) Frontoffice (utilisateur)

```powershell
cd frontoffice
npm install
npm start
# app en dev : http://localhost:4201
```

---

## 🐳 Exécution avec Docker / Docker Compose

Le dépôt contient un `docker-compose.yml` racine qui orchestre : PostgreSQL, `learning-service`, `backoffice`, `frontoffice`.

Démarrer tous les services :

```powershell
docker-compose up -d --build
```

Afficher les logs :

```powershell
docker-compose logs -f
```

Arrêter et supprimer :

```powershell
docker-compose down
# pour supprimer aussi les volumes (attention perte de données) :
docker-compose down -v
```

URLs par défaut :

- Backoffice : http://localhost:4200
- Frontoffice : http://localhost:4201
- API backend : http://localhost:8090
- PostgreSQL : localhost:5432

> Remarque : les clés sensibles (Stripe, OpenAI, mot de passe mail, JWT secret) sont dans `learning-service/src/main/resources/application.properties` ou injectées via `docker-compose.yml`. Changez-les avant usage en production.

---

## 🔒 Configuration importante

Fichier principal du backend : `learning-service/src/main/resources/application.properties`.

Exemples de variables à vérifier/modifier :

- `spring.datasource.*` — connexion PostgreSQL
- `jwt.secret` et `jwt.expiration`
- `stripe.api.key` et `stripe.publishable.key`
- `openai.api.key`
- `spring.mail.username` / `spring.mail.password`

---

## ✅ Tests & qualité

- Backend : JUnit (exécuter `mvn test` dans `learning-service`)
- Frontend : `ng test` / `npm test`
- Couverture : JaCoCo
- Qualité : SonarQube (configuration dans `pom.xml`)

---

## 📦 Déploiement / Build rapide

- Backend : `mvn clean package`
- Front / Back : `npm run build` (ou `npm run build --prod`) puis servir via Nginx (les Dockerfiles le font déjà)

---

## 🤝 Contribution

1. Fork
2. Branche feature (`git checkout -b feat/ma-fonctionnalite`)
3. Commit & PR

Merci d'ajouter des tests et de documenter les nouvelles endpoints.

---

## 📞 Support / Contact

Pour toute question liée au projet, créer une issue dans le repo.

---

*Fichier généré automatiquement — adaptez les clés et mots de passe avant mise en production.*
