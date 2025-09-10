# RPG Combat Simulator

## 🎮 Description
Ce projet est un **mini jeu de combat RPG** au tour par tour en Java.  
Le joueur contrôle une équipe de héros qui affrontent successivement des ennemis.  
Le jeu inclut des mécaniques avancées : inventaire, objets, buffs, régénération automatique, attaques parallèles entre les bancs, et statistiques détaillées en fin de partie.

---

## ✨ Fonctionnalités principales

### 1. **Système de combat 1v1**
- Un héros actif combat un ennemi.
- Gestion de la **vitesse** pour déterminer qui attaque en premier.
- Possibilité de **switcher** entre différents héros encore en vie.

### 2. **Inventaire & Objets**
Chaque héros peut utiliser des objets durant son tour :

| Objet        | Effet |
|--------------|-------|
| 🍎 **Pomme** | Soigne +30 HP sans dépasser le HP max |
| 🔥 **Rage** | +20 ATK pendant 3 tours |
| 🛡 **Bouclier** | +20 DEF pendant 3 tours |

---

### 3. **Buffs temporaires**
- Rage et Bouclier appliquent des bonus **temporaires (3 tours)**.
- Les buffs sont suivis grâce à une `Map<Personnage, Buff>`.

---

### 4. **Attaque parallèle**
Pendant chaque tour du joueur, une **mini escarmouche parallèle** se déclenche :
- Un allié du banc attaque un ennemi du banc.
- En même temps, un ennemi du banc attaque un allié du banc.
- Ces deux attaques s'exécutent **en parallèle** grâce à des threads (`ExecutorService`).

Cela rend le combat plus dynamique et réaliste.

---

### 5. **Régénération automatique hors combat**
- En dehors des combats, tous les héros gagnent **+5 HP toutes les 5 secondes**.
- Implémenté avec un `ScheduledExecutorService`.

---

### 6. **Statistiques en fin de partie**
À la fin du jeu, un tableau récapitulatif affiche :
- Dégâts infligés et subis par chaque personnage.
- Nombre de coups portés et reçus.
- HP restants.
- **Meilleur DPS** et **meilleur tank**.

---

## 🗂 Structure du projet

```
src/
│
├── Test2.java              # Main + logique du combat et du jeu
│
├── inventaire/
│   └── Item.java           # Gestion des objets et des buffs
│
└── personnages/
    ├── Personnage.java     # Classe de base pour tous les personnages
    ├── Chevalier.java
    ├── Mage.java
    ├── Guerrier.java
    ├── Gobelin.java
    ├── Orc.java
    ├── Dragon.java
    └── Ennemi.java
```

---

## ⚙️ Classes importantes

### **1. Personnage.java**
Classe abstraite représentant tout personnage (héros ou ennemi) :
- Attributs : HP, ATK, DEF, SPD...
- Méthodes :
  - `performAttack()` → inflige des dégâts à un autre personnage.
  - Gestion des statistiques (dégâts infligés, coups portés, etc.).

---

### **2. Item.java**
- Contient la logique de **gestion des objets** et des **buffs**.
- Buffs gérés via une classe interne `Buff` :

```java
public static class Buff {
    public int atkBonus;
    public int defBonus;
    public int turns; // Durée restante en tours
}
```

**Méthodes clés :**
- `useApple()` → soigne un héros.
- `useRage()` → +20 ATK pendant 3 tours.
- `useShield()` → +20 DEF pendant 3 tours.
- `tickBuffs()` → décrémente les durées des buffs à la fin de chaque tour.

---

### **3. Test2.java**
Classe principale qui gère :
- Création des héros et ennemis.
- Boucle principale du jeu.
- Interaction utilisateur via le terminal.
- Lancement des threads pour les combats parallèles.
- Gestion de la régénération hors combat.

---

## 🕹️ Gameplay

### Déroulement d'un tour
Le joueur choisit une action :
1. **Attaquer**  
2. **Utiliser un objet**  
3. **Changer de héros actif**  
4. **Fuir**

- Le héros ou l'ennemi attaque selon l'ordre de vitesse.
- Une mini escarmouche parallèle se déclenche automatiquement :
  - 1 allié du banc attaque 1 ennemi du banc.
  - En même temps, 1 ennemi du banc attaque 1 allié du banc.
- Buffs mis à jour (`tickBuffs`).

---

### Exemple de sortie console

```java
=== DUEL === JOHAN vs Thrall [Orc]

Au tour de JOHAN (Chevalier)
Choix : 1-Attaquer  2-Objet  3-Allié (switch)  4-Fuir
> 1
Thrall a 190 HP

⚡ [Banc] Maelle attaque Griblix !
💥 [Banc] Griblix est KO !

Au tour de : Thrall (Orc)
JOHAN a 120 HP
```

---

## 🚀 Lancement du projet

### Prérequis
- **Java 17+** installé sur votre machine.

### Compilation
Dans le dossier `src/` :

```bash
javac inventaire/*.java personnages/*.java Test2.java
```

### Exécution
Toujours depuis `src/` :

```bash
java Test2
```

---

## 🔧 Améliorations possibles
- Système d'XP et de montée de niveau pour les héros.
- Plus d'objets et de buffs (ex: poison, ralentissement, etc.).
- Sauvegarde/chargement de partie.
- Interface graphique avec JavaFX.

---

## 👨‍💻 Auteur
**Johan** – Projet réalisé dans le cadre du cours Java 2025.


