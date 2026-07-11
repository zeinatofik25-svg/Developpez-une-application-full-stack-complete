# MDD Réseau social des développeurs

## Prérequis

- Java 21
- Node.js 20+ / npm 10+
- Docker Desktop (ou Docker Engine + Docker Compose)

---

## Lancement

### 1. Base de données MySQL avec Docker
Lancez Docker Desktop sur votre machine.
Depuis la racine du projet:

```bash
docker compose up -d mysql
docker compose ps
docker compose up
```

Et après démarrez le backend pour que les tables se créent 
avec Hibernate automatiquement.

La base est exposée sur `localhost:3307` avec les identifiants:
- database: `mdd`
- user: `mdd_user`
- password: `mdd_pass_123`

Les données sont persistées dans le volume Docker `mdd_mysql_data`.

#### Insertion des données dans les tables

Dans le container mdd-mysql, ouvrez un terminal et exécutez:

1. mysql -u root -p et utilisez le password 'root_password_123'

2.  SHOW DATABASES;
    USE mdd;
    SHOW TABLES;

INSERT INTO topics (id, name, description, created_at) VALUES
(1, 'Java', 'Discussions about Java programming', NOW()),
(2, 'JavaScript', 'Frontend and backend JavaScript topics', NOW()),
(3, 'Python', 'Python development and frameworks', NOW()),
(4, 'Web3', 'Blockchain and decentralized technologies', NOW()),
(5, 'DevOps', 'CI/CD, Docker, cloud and infrastructure', NOW());

INSERT INTO posts (id, author_id, topic_id, title, content, created_at) VALUES
(1, 1, 1, 'Getting started with Spring Boot', 'Spring Boot makes it easy to create Java applications quickly.', NOW()),
(2, 2, 2, 'Understanding Angular Signals', 'Angular signals are a new way to handle reactivity.', NOW()),
(3, 3, 3, 'Python tips for beginners', 'Here are some useful Python tips for new developers.', NOW()),
(4, 1, 5, 'CI/CD with GitHub Actions', 'Automating builds and deployments using GitHub Actions.', NOW());

INSERT INTO comments (id, author_id, post_id, content, created_at) VALUES
(1, 2, 1, 'Very helpful, thanks for sharing!', NOW()),
(2, 3, 1, 'Spring Boot is really powerful.', NOW()),
(3, 1, 2, 'Great explanation about signals.', NOW()),
(4, 2, 3, 'Python is such a great language to start with.', NOW());

INSERT INTO subscriptions (id, user_id, topic_id, created_at) VALUES
(1, 1, 1, NOW()),
(2, 1, 5, NOW()),
(3, 2, 2, NOW()),
(4, 2, 3, NOW()),
(5, 3, 3, NOW()),
(6, 3, 4, NOW());


### 2. Backend

```bash
cd back
./mvnw spring-boot:run          # Linux/Mac
./mvnw.cmd spring-boot:run      # Windows
```

Configuration backend (déjà prête):
- `DB_URL` (défaut: `jdbc:mysql://localhost:3307/mdd`)
- `DB_USER` (défaut: `mdd_user`)
- `DB_PASSWORD` (défaut: `mdd_pass_123`)
- `APP_JWT_SECRET` (défaut défini dans `application.properties`)

### 3. Frontend

```bash
cd front
npm install
npm start              # http://localhost:4200
```

Si le port 4200 est occupé : `npm start -- --port 4201`

---

## Documentation des endpoints API

Documentation interactive : `http://localhost:8080/swagger-ui/index.html`

> Les routes marquées `Privé` nécessitent une session authentifiée. Dans le navigateur, l'authentification repose sur un cookie `HttpOnly` envoyé automatiquement. L'API accepte aussi un header `Authorization: Bearer <token>` pour les tests/outils externes.

### Authentification `/api/auth`

| Méthode | Route | Auth | Description |
| POST | `/api/auth/register` | Public | Crée un compte utilisateur |
| POST | `/api/auth/login` | Public | Connecte un utilisateur et retourne un JWT |
| POST | `/api/auth/logout` | Public | Ferme la session et supprime le cookie d'authentification |
| GET | `/api/auth/me` | Privé | Retourne le profil de l'utilisateur connecté |
| PUT | `/api/auth/me` | Privé | Met à jour le profil (email, username, password optionnel) |

Règle mot de passe : min. 6 caractères, au moins une lettre, un chiffre et un caractère spécial (ex: `Mdp2026!`).

### Sujets `/api/topics`

| Méthode | Route | Auth | Description |
| GET | `/api/topics` | Privé | Liste tous les sujets avec statut abonnement |
| POST | `/api/topics/{topicId}/subscribe` | Privé | S'abonner à un sujet |
| DELETE | `/api/topics/{topicId}/unsubscribe` | Privé | Se désabonner d'un sujet |

### Articles `/api/posts`

| Méthode | Route | Auth | Description |
| GET | `/api/posts/feed?sort=newest&page=0&size=4` | Privé | Feed paginé des articles des sujets abonnés, ordre décroissant |
| GET | `/api/posts/feed?sort=oldest&page=0&size=4` | Privé | Feed paginé des articles des sujets abonnés, ordre croissant |
| POST | `/api/posts` | Privé | Créer un article |
| GET | `/api/posts/{postId}` | Privé | Détail d'un article avec commentaires, accessible seulement si l'utilisateur est abonné au thème |

Paramètres du feed :
- `sort` : `newest` ou `oldest`
- `page` : index de page, base 0
- `size` : nombre d'articles par page

Réponse du feed :
- `items` : articles de la page courante
- `page` : index courant
- `size` : taille de page
- `totalElements` : total d'articles disponibles
- `totalPages` : nombre total de pages
- `hasNext` : indique si une page suivante existe

### Commentaires

| Méthode | Route | Auth | Description |
| POST | `/api/posts/{postId}/comments` | Privé | Ajouter un commentaire |

### Codes HTTP

| Code | Signification |
|---|---|
| 200 | Succes |
| 201 | Ressource créée |
| 204 | Succes sans contenu |
| 400 | Validation échouée |
| 401 | Non authentifié |
| 403 | Accès refusé |
| 404 | Ressource introuvable |
| 409 | Conflit (email/pseudo déjà pris, déjà abonné) |
| 500 | Erreur interne serveur |

---

## Commandes de test

```bash
# Backend tous les tests + couverture JaCoCo
cd back
./mvnw test
./mvnw verify        # vérifie seuil couverture >= 80%

# Frontend Jest
cd front
npm test
npm run test:coverage

# Cypress e2e (serveur Angular doit etre lancé)
npm run cypress:open        # interface graphique
npm run cypress:run         # headless
npm run test:e2e:ci         # lance serveur + cypress automatiquement
```

---

## Couverture de tests

| Couche | Outil | Tests |
| Backend (services, controllers, sécurité) | JUnit 5 + MockMvc | 51 tests seuil JaCoCo 80 % |
| Frontend composants et services | Jest | 73 tests |
| End-to-end | Cypress | 10 scénarios |

Rapport JaCoCo : `back/target/site/jacoco/index.html`
Rapport Jest by Istanbul: `front/coverage/lcov-report/index.html`
---

## Architecture

**Backend** : `controller ? service ? repository`
- Gestion d'erreurs centralisée : `GlobalExceptionHandler` (`@ControllerAdvice`)
- Sécurité : JWT stateless, `JwtAuthenticationFilter`, interface `CurrentUserProvider`
- DTOs Java `record` pour toutes les requetes et réponses
- Authentification côté navigateur via cookie `HttpOnly`, pas de stockage du token dans `localStorage`

**Frontend** : composants Angular standalone
- `authGuard` protège les routes privées
- `authInterceptor` envoie automatiquement les credentials (`withCredentials`) pour le cookie `HttpOnly`
- page `topics` : pagination locale côté frontend (4 cartes par page)
- page `feed` : pagination côté backend avec appel API paginé (`sort`, `page`, `size`)

---

### Erreurs courantes

| Erreur | Cause probable | Solution |
| 400 | Champ invalide | Vérifiez les champs |
| 401 | Token absent ou expiré | Reconnectez-vous |
| 404 | Ressource inexistante | Vérifiez l'URL |
| 409 | Email/pseudo pris, déjà abonné | Changez la valeur |
| 500 | Erreur serveur | Vérifiez que le backend est démarré |
