# Projet Timer EscapeKraft

## Objectif fonctionnel

Le mod implemente un systeme d'escape game par equipe.

- Une equipe peut contenir un ou plusieurs joueurs.
- Une escape est demarree via la commande `/ek start <player>`, typiquement appelee par un `command_block`.
- Le joueur cible sert uniquement a retrouver son equipe.
- Quand le timer demarre, tous les membres connectes de cette equipe voient un chrono partage dans l'action bar.
- La fin de l'escape est declenchee via `/ek stop <player> <escapeName>`.
- Le temps final de l'equipe est enregistre sous le nom de l'escape.
- Les scores doivent ensuite etre consultables en jeu via `/ek leaderboard <escapeName>`.

## Etat actuel de l'implementation

### Equipes

- `src/main/java/fr/ekrec/teams/EKTeam.java`
- `src/main/java/fr/ekrec/teams/EKTeamManager.java`

Les equipes sont gerees en memoire par `EKTeamManager`.

- `createTeam(name, leader)` cree une equipe.
- `joinTeam(name, player)` ajoute un joueur a une equipe, en le retirant de son equipe precedente.
- `getTeamOfPlayer(player)` retrouve l'equipe d'un joueur.
- `removePlayer(player)` retire un joueur et supprime l'equipe si elle devient vide.

Chaque `EKTeam` contient :

- `name`
- `leader`
- `members`
- `startTime`

### Timer

- `EKTeamManager.startTimer(UUID player)` demarre le chrono pour l'equipe du joueur.
- `EKTeamManager.tickTimers(MinecraftServer server)` met a jour l'affichage une fois par seconde.
- `EKTeamManager.stopTimer(UUID player)` stoppe le chrono et retourne le temps ecoule en millisecondes.

L'affichage actuel est envoye via `OverlayMessageS2CPacket`, ce qui correspond a l'intention d'un affichage type action bar.

### Commandes

- `src/main/java/fr/ekrec/commands/EKCommands.java`

Commandes actuellement presentes :

- `/ek create <teamName>`
- `/ek join <teamName>`
- `/ek start <player>`
- `/ek stop <player> <escapeName>`

Flux actuel :

- `start` retrouve l'equipe et lance son timer.
- `stop` arrete le timer, cree un score, le sauvegarde et notifie les membres connectes.

### Persistance des scores

- `src/main/java/fr/ekrec/scores/EKScore.java`
- `src/main/java/fr/ekrec/scores/EKScoreManager.java`
- `src/main/java/fr/ekrec/scores/EKPersistentState.java`

Les scores sont stockes par `escapeName` dans un `PersistentState`.

- Un score contient `teamName` et `elapsedMs`.
- Les scores d'une meme escape sont tries du plus rapide au plus lent.
- Si une equipe rejoue la meme escape, son ancien score est remplace.

La persistance repose maintenant sur un `Codec`, compatible avec `PersistentStateType` en `1.21.11`.

## Ce qui est deja valide

- La structure technique du projet est coherente avec l'objectif produit.
- Le systeme d'equipe existe deja.
- Le timer partage par equipe existe deja.
- L'enregistrement persistant des scores existe deja.
- La compilation Java passe avec l'etat actuel du code.

## Travail restant pour finaliser le systeme

### Priorite 1

- Ajouter `/ek leaderboard <escapeName>`.
- Afficher en jeu le classement trie des equipes pour une escape donnee.
- Definir le format d'affichage du leaderboard.

### Priorite 2

- Verifier en jeu que le message envoye par `tickTimers()` apparait bien comme voulu dans l'action bar.
- Uniformiser le format du temps entre timer live et score final.

### Priorite 3

- Definir les permissions et les usages prevus des commandes `start` et `stop`.
- Verifier si ces commandes doivent etre reservees aux command blocks / operateurs.

### Priorite 4

- Gerer explicitement les cas limites:
- `start` sur un joueur sans equipe
- `start` sur une equipe deja en cours
- `stop` sur une equipe sans timer actif
- joueurs deconnectes/reconnectes pendant une partie

## Attendu final

Le systeme doit permettre a une map escape game de :

- constituer des equipes
- lancer automatiquement une partie pour une equipe
- afficher un chrono partage pendant la partie
- arreter le chrono a la fin
- enregistrer le resultat de l'equipe pour une escape nommee
- afficher le classement en jeu avec `/ek leaderboard <escapeName>`
