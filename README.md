# Gacha API

Projet de jeu de type Gacha composé de 5 microservices Spring Boot communiquant entre eux, avec MongoDB comme base de données.

Projet réalisé par : Adnane Ramdane, Yassine Khouchaf, Yassine Messaoui et Amine El Barkaoui

**Front-end** : [http://localhost:8083](http://localhost:8083) (accessible après `docker compose up --build`)

---

## Architecture

| Service | Port | Rôle |
|---------|------|------|
| `authapi` | 8080 | Authentification et gestion des tokens |
| `playerapi` | 8081 | Gestion des profils joueurs |
| `monsterapi` | 8082 | Gestion des monstres |
| `invocationapi` | 8083 | Invocation aléatoire de monstres + front-end |
| `combatapi` | 8084 | Simulation de combats automatiques |
| `mongodb` | 27017 | Base de données (5 bases séparées) |

---

## Description des APIs

### authapi — Authentification (port 8080)
Gère l'inscription, la connexion et la validation des tokens.
Chaque connexion génère un token AES chiffré valable **1 heure** (durée glissante à chaque appel).
Tous les autres services valident leurs requêtes en appelant cet endpoint.

### playerapi — Joueurs (port 8081)
Gère les profils joueurs : création, XP, montée de niveau (max 50), et inventaire de monstres.
La capacité d'inventaire augmente avec le niveau (`10 + niveau`).
Communique avec `monsterapi` pour créer/supprimer des monstres.

### monsterapi — Monstres (port 8082)
Gère le cycle de vie des monstres : création, XP, montée de niveau et amélioration de compétences.
À chaque niveau, les stats (HP, ATK, DEF, VIT) augmentent de +5 et un skill point est accordé.
Les compétences peuvent être améliorées manuellement via les skill points.

### invocationapi — Invocations (port 8083)
Implémente la mécanique gacha : tire un monstre au sort selon des taux de probabilité paramétrables.
L'invocation est persistée en base tampon et traitée en plusieurs étapes (création monstre → lien joueur) pour permettre le replay en cas d'échec.
Héberge également le **front-end** à la racine `http://localhost:8083`.

### combatapi — Combats (port 8084)
Simule des combats tour par tour entre deux monstres.
Chaque tour, le skill disponible avec le plus grand index est utilisé ; les cooldowns sont décrémentés à chaque tour.
Le dommage est calculé par `baseDamage + stat × ratio`. Le vainqueur reçoit 50 XP. Combat limité à 100 tours.

---

## Prérequis

- [Docker](https://www.docker.com/get-started) (version 20+)
- [Docker Compose](https://docs.docker.com/compose/) (version 2+)
---

## Lancer le projet

### 1. Démarrer tous les services

```bash
docker compose up --build
```

> Le premier démarrage peut prendre plusieurs minutes (build Maven de chaque service).

### 2. Vérifier que tout tourne

```bash
docker compose ps
```

Tous les services doivent être à l'état `running`.

### Arrêter le projet

```bash
docker compose down
```

Pour également supprimer les données MongoDB :

```bash
docker compose down -v
```

---

## Données de test

Au premier démarrage, MongoDB est automatiquement peuplé via `data/init-mongo.js` avec :

**Utilisateurs** (authapi)
| Username | Password |
|----------|----------|
| alice | alice123 |
| bob | bob123 |

**Joueurs** (playerapi) : profils Alice (level 1) et Bob (level 0)

**Monstres invocables** (invocationapi)
| Nom | Type | Taux |
|-----|------|------|
| Ignis | FIRE | 30% |
| Zephyr | WIND | 30% |
| Aqua | WATER | 30% |
| Tsunami | WATER | 10% |

---

## Utilisation des APIs

### 1. S'authentifier avec Postman

#### Étape 1 — Connexion (obtenir le token)

1. Ouvrir Postman et créer une nouvelle requête **POST**
2. URL : `http://localhost:8080/api/auth/login`
3. Onglet **Body** → sélectionner **raw** → type **JSON**
4. Saisir :
```json
{ "username": "alice", "password": "alice123" }
```
5. Cliquer **Send** → copier la valeur du champ `token` dans la réponse

#### Étape 2 — Utiliser le token dans les autres requêtes

Pour chaque requête vers les autres APIs (ports 8081–8084) :

1. Onglet **Authorization**
2. Type : **Bearer Token**
3. Coller le token dans le champ **Token**

> Le token expire après **1 heure d'inactivité**. Si vous obtenez une erreur 401, refaites l'étape 1.

---

### 2. API Joueur

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/players` | Créer son profil joueur |
| GET | `/api/players/{username}` | Récupérer le profil complet |
| GET | `/api/players/{username}/level` | Récupérer le niveau |
| GET | `/api/players/{username}/monsters` | Liste des IDs de monstres |
| POST | `/api/players/{username}/experience` | Gagner de l'XP `{"amount": 100}` |
| POST | `/api/players/{username}/levelup` | Monter de niveau |
| POST | `/api/players/{username}/monsters` | Ajouter un monstre manuellement |
| DELETE | `/api/players/{username}/monsters/{id}` | Supprimer un monstre |

---

### 3. API Monstres

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/monsters` | Créer un monstre |
| GET | `/api/monsters/{id}` | Récupérer un monstre |
| DELETE | `/api/monsters/{id}` | Supprimer son monstre |
| POST | `/api/monsters/{id}/experience` | Donner de l'XP `{"amount": 50}` |
| POST | `/api/monsters/{id}/skills/{index}/improve` | Améliorer une compétence (0, 1 ou 2) |

---

### 4. API Invocations

**Front-end** : ouvrir `http://localhost:8083` dans un navigateur.

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/invocations` | Invoquer un monstre aléatoire |
| GET | `/api/invocations` | Historique des invocations |
| POST | `/api/invocations/replay` | Rejouer les invocations non terminées |
| GET | `/api/invocations/monsters` | Liste des monstres invocables |

---

### 5. API Combat

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/combats` | Lancer un combat `{"monster1Id": "...", "monster2Id": "..."}` |
| GET | `/api/combats` | Historique de tous les combats |
| GET | `/api/combats/{numero}` | Rediffusion d'un combat |

---

## Tests unitaires

> **Prérequis** : Java 21 et Maven doivent être installés localement (pas nécessaire pour `docker compose up`).
>
> - Java 21
> - Maven (l'ajouter dans les variables d'environnements)
>
> Vérifier l'installation :
> ```powershell
> java -version
> mvn -version
> ```

Pour lancer tous les tests unitaires des 5 APIs en une seule commande (depuis la racine du projet et sur powershell) :

```bash
foreach ($api in @("authapi","playerapi","monsterapi","invocationapi","combatapi")) { Write-Host "=== $api ===" ; Push-Location $api ; mvn test ; Pop-Location }
```

Ou service par service :

```bash
cd authapi      && mvn test
cd playerapi    && mvn test
cd monsterapi   && mvn test
cd invocationapi && mvn test
cd combatapi    && mvn test
```

---

## Structure du projet

```
Gacha/
├── authapi/          # API authentification (port 8080)
├── playerapi/        # API joueur (port 8081)
├── monsterapi/       # API monstres (port 8082)
├── invocationapi/    # API invocations + front-end (port 8083)
├── combatapi/        # API combat (port 8084)
├── data/
│   └── init-mongo.js # Données initiales MongoDB
├── docker-compose.yml
└── README.md
```