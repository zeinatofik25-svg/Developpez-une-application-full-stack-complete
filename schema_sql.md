## Base de données MDD (Monde de Dév)

Ce document décrit la structure de la base de données utilisée dans l'application MDD. La base est utilisée par le backend Spring Boot via l'ORM Hibernate et est exécutée dans un conteneur Docker MySQL.

Technologies utilisées

- Base de données : MySQL 8
- ORM : Hibernate (via Spring Data JPA)
- Conteneurisation : Docker

## Nom de la base de données

mdd

## Structure générale

La base de données contient 5 tables principales :

- users
- topics
- posts
- comments
- subscriptions

Ces tables permettent de gérer les utilisateurs, les thèmes de programmation, les articles publiés, les commentaires et les abonnements aux thèmes.

---

## Table : users

Stocke les informations des utilisateurs de la plateforme.

## Champs :

- id : identifiant unique de l'utilisateur
- email : adresse email
- username : nom d'utilisateur
- password : mot de passe hashé
- created_at : date de création du compte

## Schéma SQL

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

---

## Table : topics

Stocke les sujets ou thèmes de programmation auxquels les utilisateurs peuvent s'abonner.

## Champs :

- id : identifiant du sujet
- name : nom du sujet
- description : description du sujet
- created_at : date de création

## Schéma SQL

CREATE TABLE topics (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

---

## Table : posts

Stocke les articles publiés par les utilisateurs.

## Champs :

- id : identifiant de l'article
- author_id : auteur de l'article
- topic_id : sujet associé
- title : titre de l'article
- content : contenu de l'article
- created_at : date de publication

## Schéma SQL

CREATE TABLE posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  author_id BIGINT NOT NULL,
  topic_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (author_id) REFERENCES users(id),
  FOREIGN KEY (topic_id) REFERENCES topics(id)
);

---

## Table : comments

Stocke les commentaires publiés sur les articles.

## Champs :

- id : identifiant du commentaire
- author_id : auteur du commentaire
- post_id : article concerné
- content : contenu du commentaire
- created_at : date de publication

Schéma SQL

CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  author_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (author_id) REFERENCES users(id),
  FOREIGN KEY (post_id) REFERENCES posts(id)
);

---

## Table : subscriptions

Permet de gérer les abonnements des utilisateurs aux sujets.

## Champs :

- id : identifiant de l'abonnement
- user_id : utilisateur abonné
- topic_id : sujet suivi
- created_at : date d'abonnement

## Schéma SQL

CREATE TABLE subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  topic_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY unique_subscription (user_id, topic_id),

  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (topic_id) REFERENCES topics(id)
);

---

## Relations entre les tables

User
- un utilisateur peut créer plusieurs articles
- un utilisateur peut écrire plusieurs commentaires
- un utilisateur peut s'abonner à plusieurs sujets

Topic
- un sujet peut contenir plusieurs articles
- un sujet peut avoir plusieurs abonnés

Post
- un article appartient à un utilisateur
- un article appartient à un sujet
- un article peut avoir plusieurs commentaires

Comment
- un commentaire appartient à un article
- un commentaire appartient à un utilisateur

Subscription
- relie un utilisateur à un sujet

---

## Contraintes importantes

- Les emails et usernames des utilisateurs doivent être uniques
- Un utilisateur ne peut s’abonner qu’une seule fois au même sujet
- Chaque commentaire est lié à un seul article
- Les relations sont gérées via des clés étrangères

---