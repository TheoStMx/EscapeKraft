# Elements template ou encore incomplets

Ce document garde une trace des parties du projet qui proviennent encore du template Fabric, de ce qui a deja ete remplace, et des prochains sujets a traiter.

## Etat general

La base du mod n'est plus un simple exemple Fabric.

- Le coeur serveur existe deja: equipes, timer partage, sauvegarde des scores, commandes principales.
- Un leaderboard existe maintenant en chat et sous forme d'affichage persistant via des `text_display`.
- Le projet compile avec cette base.

Le nettoyage du template n'est cependant pas termine.

## Points encore issus du template

### Metadonnees du mod

- `src/main/resources/fabric.mod.json`
- La description est encore celle du template.
- Les auteurs sont encore generiques (`"Me!"`).
- Les liens `homepage` et `sources` pointent encore vers Fabric et le repo d'exemple.
- La licence est encore celle du template (`CC0-1.0`) et doit etre validee.

### Point d'entree client

- `src/client/java/fr/ekrec/EscapeKraftClient.java`
- Le point d'entree client est vide.
- Ce n'est pas bloquant tant qu'aucune logique purement client n'est necessaire.

### Datagen

- `src/client/java/fr/ekrec/EscapeKraftDataGenerator.java`
- Le point d'entree datagen est vide.
- Le projet declare le datagen, mais aucun provider n'est encore en place.

### Mixins d'exemple

- `src/main/java/fr/ekrec/mixin/ExampleMixin.java`
- `src/client/java/fr/ekrec/mixin/client/ExampleClientMixin.java`
- Ces deux mixins sont encore des placeholders du template Fabric.
- Ils n'implementent aucun comportement utile.
- Ils devraient etre supprimes tant qu'aucun vrai point d'injection n'est requis.

## Points deja sortis du template

### Systeme gameplay

- `src/main/java/fr/ekrec/teams/`
- `src/main/java/fr/ekrec/scores/`
- `src/main/java/fr/ekrec/commands/EKCommands.java`
- `src/main/java/fr/ekrec/leaderboard/`
- Ces zones correspondent maintenant a une vraie base de mod EscapeKraft et non plus a du code d'exemple.

### Persistance

- `src/main/java/fr/ekrec/scores/EKPersistentState.java`
- La persistance des scores repose maintenant sur un `Codec` coherent avec `PersistentStateType` pour la cible actuelle.
- Ce point n'a plus a etre traite comme "template", mais il reste a valider en situation de jeu.

### Ressources custom

- Le mod ajoute deja un disque musical personnalise, son son, sa texture, son modele et un onglet creatif.
- Cela reste un contenu de base, mais ce n'est plus seulement le contenu brut du template.

## Points techniques encore a corriger

### Localisation FR

- `src/main/resources/assets/escapekraft/lang/fr_fr.json`
- Le fichier contient encore des caracteres mal encodes (`Ã©`, `Ã‰`, etc.).
- Il faut le remettre en UTF-8 propre avant de continuer a enrichir les textes.

### Textes de commandes

- `src/main/java/fr/ekrec/commands/EKCommands.java`
- Une partie des retours utilisateur est encore ecrite en dur en anglais.
- Il faudra les basculer vers des cles de traduction pour garder une base propre et coherente.

## Ordre propose pour les prochaines etapes

1. Corriger `fr_fr.json` et stabiliser l'encodage des traductions.
2. Remplacer les messages en dur des commandes par des textes traduisibles.
3. Nettoyer `fabric.mod.json`.
4. Supprimer les mixins d'exemple si aucun besoin reel ne les justifie.
5. Decider si on garde le datagen declare, puis soit l'implementer, soit le retirer temporairement.

## But de ce document

Ce fichier sert maintenant de checklist de nettoyage et d'alignement du projet avec la vraie direction du mod, pas seulement de liste de restes du template initial.
