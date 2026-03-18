// ── Authapi : utilisateurs de test ────────────────────────────────
db = db.getSiblingDB('authapi');
db.users.insertMany([
  { username: "alice", password: "alice123" },
  { username: "bob",   password: "bob123"   }
]);

// ── Playerapi : profils joueurs de test ───────────────────────────
db = db.getSiblingDB('playerapi');
db.players.insertMany([
  {
    username: "alice",
    level: 1,
    experience: 0,
    experienceThreshold: 50,
    maxMonsters: 11,
    monsters: []
  },
  {
    username: "bob",
    level: 0,
    experience: 0,
    experienceThreshold: 50,
    maxMonsters: 10,
    monsters: []
  }
]);

// ── Invocationapi : monstres invocables ───────────────────────────
db = db.getSiblingDB('invocationapi');
db.base_monsters.insertMany([
  {
    name: "Ignis",
    elementType: "FIRE",
    hp: 1200,
    atk: 450,
    def: 300,
    vit: 85,
    invocationRate: 30
  },
  {
    name: "Zephyr",
    elementType: "WIND",
    hp: 1500,
    atk: 200,
    def: 450,
    vit: 80,
    invocationRate: 30
  },
  {
    name: "Aqua",
    elementType: "WATER",
    hp: 2500,
    atk: 150,
    def: 200,
    vit: 70,
    invocationRate: 30
  },
  {
    name: "Tsunami",
    elementType: "WATER",
    hp: 1200,
    atk: 550,
    def: 350,
    vit: 80,
    invocationRate: 10
  }
]);
