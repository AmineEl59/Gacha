# Gacha API

Projet de jeu de type Gacha composé de 5 microservices Spring Boot communiquant entre eux, avec MongoDB comme base de données.

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

## Prérequis

- [Docker](https://www.docker.com/get-started) (version 20+)
- [Docker Compose](https://docs.docker.com/compose/) (version 2+)

Aucune installation de Java ou Maven n'est requise : tout est buildé dans les conteneurs.

---

## Lancer le projet

### 1. Cloner le dépôt

```bash
git clone <url-du-repo>
cd Gacha
```

### 2. Démarrer tous les services

```bash
docker compose up --build
```

> Le premier démarrage peut prendre plusieurs minutes (build Maven de chaque service).

### 3. Vérifier que tout tourne

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

### 1. S'authentifier

```bash
# Inscription
POST http://localhost:8080/api/auth/register
Content-Type: application/json
{ "username": "alice", "password": "alice123" }

# Connexion → récupérer le token
POST http://localhost:8080/api/auth/login
Content-Type: application/json
{ "username": "alice", "password": "alice123" }
```

Réponse :
```json
{ "token": "U2Fsd..." }
```

Utiliser ce token dans le header `Authorization: Bearer <token>` pour tous les appels suivants.

---

### 2. API Joueur — `http://localhost:8081`

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

### 3. API Monstres — `http://localhost:8082`

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/monsters` | Créer un monstre |
| GET | `/api/monsters/{id}` | Récupérer un monstre |
| DELETE | `/api/monsters/{id}` | Supprimer son monstre |
| POST | `/api/monsters/{id}/experience` | Donner de l'XP `{"amount": 50}` |
| POST | `/api/monsters/{id}/skills/{index}/improve` | Améliorer une compétence (0, 1 ou 2) |

---

### 4. API Invocations — `http://localhost:8083`

**Front-end** : ouvrir `http://localhost:8083` dans un navigateur.

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/invocations` | Invoquer un monstre aléatoire |
| GET | `/api/invocations` | Historique des invocations |
| POST | `/api/invocations/replay` | Rejouer les invocations non terminées |
| GET | `/api/invocations/monsters` | Liste des monstres invocables |

---

### 5. API Combat — `http://localhost:8084`

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/combats` | Lancer un combat `{"monster1Id": "...", "monster2Id": "..."}` |
| GET | `/api/combats` | Historique de tous les combats |
| GET | `/api/combats/{numero}` | Rediffusion d'un combat |

---

## Exemple complet

```bash
# 1. Connexion
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Créer son profil joueur
curl -X POST http://localhost:8081/api/players \
  -H "Authorization: Bearer $TOKEN"

# 3. Invoquer un monstre
curl -X POST http://localhost:8083/api/invocations \
  -H "Authorization: Bearer $TOKEN"
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
