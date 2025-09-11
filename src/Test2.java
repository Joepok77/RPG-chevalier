import inventaire.Item;
import inventaire.Item.Buff;
import personnages.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class Test2 {
 // Affichage des heros/alliés ou ennemis
    private static void displayStartup(List<Personnage> coteHeros, List<Personnage> ennemis) {
        System.out.println("=== HÉROS & ALLIÉS (switch possibles) ===");
        for (Personnage p : coteHeros) {
            System.out.println(" - " + p.getName().toUpperCase() + " (" + p.getHp() + " HP) [" + p.getClass().getSimpleName() + "]");
        }
        System.out.println("=== ENNEMIS  ===");
        for (Personnage p : ennemis) {
            System.out.println(" - " + p.getName().toLowerCase() + " (" + p.getHp() + " HP) [" + p.getClass().getSimpleName() + "]");
        }
        System.out.println();

        // Affichage de l'inventaire d'équipe
        System.out.println("=== INVENTAIRE D'ÉQUIPE ===");
        if (Item.getTeamInventory().isEmpty()) {
            System.out.println(" (vide)");
        } else {
            Item.getTeamInventory().forEach((nom, qty) ->
                    System.out.println(" - " + nom + " : " + qty)
            );
        }
        System.out.println();
    }

    private static void printEndStats(List<Personnage> participants) {
        if (participants.isEmpty()) return;

        var mostDamageDealer = participants.stream().max(Comparator.comparingInt(Personnage::getTotalDamageDealt)).orElse(null);
        var mostDamageTaken  = participants.stream().max(Comparator.comparingInt(Personnage::getTotalDamageTaken)).orElse(null);

        System.out.println("\n=== STATISTIQUES DE FIN ===");
        for (Personnage p : participants) {
            System.out.println(
                    p.getName() + " -> " +
                            "Dégâts infligés: " + p.getTotalDamageDealt() +
                            ", Dégâts subis: " + p.getTotalDamageTaken() +
                            ", Coups portés: " + p.getHitsDone() +
                            ", Coups reçus: " + p.getHitsReceived() +
                            ", HP restants: " + p.getHp()
            );
        }
        if (mostDamageDealer != null)
            System.out.println("\n> Meilleur DPS : " + mostDamageDealer.getName() + " (" + mostDamageDealer.getTotalDamageDealt() + ")");
        if (mostDamageTaken != null)
            System.out.println("> meilleur TANK : " + mostDamageTaken.getName() + " (" + mostDamageTaken.getTotalDamageTaken() + ")\n");
    }

    // Lecture sécurisée des entrées joueur dans le terminal
    private static int readInt(Scanner sc, int... allowed) {
        while (true) {
            try {
                String s = sc.next();
                int val = Integer.parseInt(s.trim());
                if (allowed.length == 0) return val;
                for (int a : allowed) if (a == val) return val;
                System.out.print("❌ Choix invalide. Réessaie: ");
            } catch (NumberFormatException e) {
                System.out.print("⚠️ Entrez un nombre valide: ");
            }
        }
    }

    private static List<Personnage> alive(List<Personnage> team) {
        List<Personnage> out = new ArrayList<>();
        for (Personnage p : team) if (p.getHp() > 0) out.add(p);
        return out;
    }

    // Gère les attaques avec buffs (rage ou bouclier)
    private static void attackWithBuffs(Personnage attacker, Personnage defender, Map<Personnage, Buff> buffs) {
        int atk0 = attacker.getAtk(), def0 = defender.getDefense();
        Buff ba = Item.getBuff(buffs, attacker), bd = Item.getBuff(buffs, defender);
        attacker.setAtk(atk0 + Math.max(0, ba.atkBonus));
        defender.setDefense(def0 + Math.max(0, bd.defBonus));
        attacker.performAttack(defender);
        attacker.setAtk(atk0);
        defender.setDefense(def0);
    }

    // Permet de changer de héros actif ssi il est toujours en vie
    private static Personnage chooseFighter(Scanner scan, List<Personnage> roster, Personnage active) {
        System.out.println("Choisir le combattant (remplace " + active.getName() + ") :");
        for (int i = 0; i < roster.size(); i++) {
            Personnage p = roster.get(i);
            String status = p.getHp() > 0 ? (p == active ? " (actif)" : "") : " [KO]";
            System.out.println("  " + (i + 1) + " - " + p.getName() + " (" + p.getHp() + " HP) [" + p.getClass().getSimpleName() + "]" + status);
        }
        System.out.print("> ");
        int idx = readInt(scan);
        if (idx < 1 || idx > roster.size()) {
            System.out.println("Choix hors limites.");
            return active;
        }
        Personnage chosen = roster.get(idx - 1);
        if (chosen.getHp() <= 0) {
            System.out.println("❌ " + chosen.getName() + " est KO.");
            return active;
        }
        if (chosen == active) {
            System.out.println("Déjà actif.");
            return active;
        }
        System.out.println("🔁 " + active.getName() + " -> " + chosen.getName() + " (tour consommé)");
        return chosen;
    }

    //  sac partagé avc tout les alliés
    private static boolean menuObjets(Scanner scan, Personnage user, List<Personnage> roster,
                                      Map<Personnage,Integer> maxHp, Map<Personnage, Buff> buffs) {
        try {
            List<String> items = Arrays.asList("Pomme", "Rage", "Bouclier");
            System.out.println("Objets de l'équipe (sac partagé) :");
            for (int i = 0; i < items.size(); i++) {
                String it = items.get(i);
                int q = Item.getQty(it);            // 🔹 quantité depuis le sac commun
                System.out.println("  " + (i + 1) + " - " + it + " (x" + q + ")");
            }
            System.out.println("  0 - Annuler");
            System.out.print("> ");
            int ch = readInt(scan);
            if (ch == 0) return false;

            // Sélection d'une cible qui n'est pas ko pour tous les objets
            List<Personnage> options = new ArrayList<>(roster);
            options.removeIf(p -> p.getHp() <= 0);
            if (options.isEmpty()) {
                System.out.println("❌ Aucun allié valide.");
                return false;
            }

            Personnage target = user;
            if (ch == 1 || ch == 2 || ch == 3) {
                System.out.println("Choisir la cible :");
                for (int i = 0; i < options.size(); i++) {
                    Personnage p = options.get(i);
                    System.out.println("  " + (i + 1) + " - " + p.getName() + " (" + p.getHp() + " HP)");
                }
                System.out.print("> ");
                int idx = readInt(scan);
                if (idx >= 1 && idx <= options.size()) target = options.get(idx - 1);
            }

            switch (ch) {
                case 1:
                    return Item.useApple(target, maxHp);
                case 2:
                    return Item.useRage(target, buffs);
                case 3:
                    return Item.useShield(target, buffs);
                default:
                    System.out.println("Choix invalide.");
                    return false;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors de l'utilisation d'un objet: " + e.getMessage());
            return false;
        }
    }

    private static void combatParallele(List<Personnage> roster,
                                        Personnage activeHero,
                                        Personnage currentEnemy,
                                        Personnage[] allEnemies,
                                        Map<Personnage, Buff> buffs,
                                        Random rng) {

        List<Personnage> benchAllies = new ArrayList<>();
        for (Personnage a : roster) if (a != activeHero && a.getHp() > 0) benchAllies.add(a);

        List<Personnage> benchFoes = new ArrayList<>();
        for (Personnage e : allEnemies) if (e != currentEnemy && e.getHp() > 0) benchFoes.add(e);

        if (benchAllies.isEmpty() || benchFoes.isEmpty()) return;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Void> allyTask = () -> {
            Personnage atk = benchAllies.get(rng.nextInt(benchAllies.size()));
            Personnage tgt = benchFoes.get(rng.nextInt(benchFoes.size()));
            System.out.println("⚡ [Banc] " + atk.getName() + " attaque " + tgt.getName() + " !");
            attackWithBuffs(atk, tgt, buffs);
            if (tgt.getHp() <= 0) System.out.println("💥 [Banc] " + tgt.getName() + " est KO !");
            return null;
        };
        Callable<Void> foeTask = () -> {
            Personnage atk = benchFoes.get(rng.nextInt(benchFoes.size()));
            Personnage tgt = benchAllies.get(rng.nextInt(benchAllies.size()));
            System.out.println("⚡ [Banc ENNEMI] " + atk.getName() + " attaque " + tgt.getName() + " !");
            attackWithBuffs(atk, tgt, buffs);
            if (tgt.getHp() <= 0) System.out.println("💥 [Banc] " + tgt.getName() + " est KO !");
            return null;
        };
        try {
            pool.invokeAll(Arrays.asList(allyTask, foeTask), 600, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        } finally {
            pool.shutdownNow();
        }
    }

    // Régénération automatique hors combat
    private static ScheduledFuture<?> startIdleRegen(ScheduledExecutorService scheduler,
                                                     List<Personnage> heroes,
                                                     Map<Personnage,Integer> maxHp) {
        return scheduler.scheduleAtFixedRate(() -> {
            for (Personnage h : heroes) {
                int cap = maxHp.getOrDefault(h, h.getHp());
                boolean healed = h.healSafe(5, cap);
                if (healed) {
                    System.out.println("💚 Regen: " + h.getName() + " → " + h.getHp() + "/" + cap);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Random rng = ThreadLocalRandom.current();

        // Création du héros principal
        Chevalier johan = new Chevalier("Johan", 300, 30, 100, 50, 40, true);

        // Alliés
        Personnage maelle  = new Mage("Maelle", 120, 30, 85, 45, 30, false);
        Personnage gustave = new Guerrier("Gustave", 180, 55, 80, 35, 20, false);

        List<Personnage> roster = new ArrayList<>(Arrays.asList(johan, maelle, gustave));

        // Items — utilisent maintenant le sac partagé via Item.giveItem(String,int)
        Item.giveItem("Pomme", 3);
        Item.giveItem("Rage", 1);
        Item.giveItem("Bouclier", 1);

        // Ennemis
        Personnage[] adversairesArray = new Personnage[] {
                new Gobelin("Griblix", 100, 20, 35, 40, 10, false),
                new Orc("Thrall",     220, 40, 70, 25, 10, false),
                new Dragon("Smaug",   300, 80, 85, 30, 15, false),
                new Ennemi("Bandit",  140, 35, 45, 35, 15, false)
        };

        Map<Personnage,Integer> maxHp = new IdentityHashMap<>();
        for (Personnage p : roster) maxHp.put(p, p.getHp());
        for (Personnage e : adversairesArray) maxHp.put(e, e.getHp());
        Map<Personnage, Buff> buffs = new IdentityHashMap<>();

        displayStartup(roster, Arrays.asList(adversairesArray));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> regenTicker = startIdleRegen(scheduler, roster, maxHp);

        Personnage active = johan;

        // Boucle des combats 1v1
        for (Personnage enemy : adversairesArray) {
            if (alive(roster).isEmpty()) break;

            System.out.println("=== DUEL === " + active.getName() + " vs " + enemy.getName() + " [" + enemy.getClass().getSimpleName() + "]\n");


            List<Personnage> turnOrder = new ArrayList<>(roster);
            turnOrder.add(enemy);
            turnOrder.stream()
                    .sorted(Comparator.comparing(Personnage::getSpeed).reversed())
                    .forEachOrdered(p -> System.out.println(p.getName() + " jouera (SPEED " + p.getSpeed() + ")"));
            System.out.println();

            premierAttaque(active, enemy);
            boolean roundJustEnded = false;

            while (enemy.getHp() > 0 && !alive(roster).isEmpty()) {
                if (active.isStart()) {
                    System.out.println("Au tour de " + active.getName() + " (" + active.getClass().getSimpleName() + ")");
                    System.out.println("Choix : 1-Attaquer  2-Objet  3-Allié (switch)  4-Fuir");
                    int input = readInt(scan, 1,2,3,4);
                    boolean consumedTurn = true;

                    if (input == 1) {
                        attackWithBuffs(active, enemy, buffs);
                        System.out.println(enemy.getName() + " a " + enemy.getHp() + " HP\n");
                        if (enemy.getHp() <= 0) {
                            System.out.println("💥 " + enemy.getName() + " est KO !");
                            break;
                        }
                    } else if (input == 2) {
                        consumedTurn = menuObjets(scan, active, roster, maxHp, buffs); // 🔹 sac partagé
                        System.out.println();
                    } else if (input == 3) {
                        Personnage newActive = chooseFighter(scan, roster, active);
                        if (newActive != active) {
                            active.setStart(false);
                            active = newActive;
                        }
                        consumedTurn = true;
                        System.out.println();
                    } else {
                        System.out.println("Fuite. Fin de la partie.");
                        scheduler.shutdownNow();
                        return;
                    }

                    if (consumedTurn) {
                        active.setStart(false);
                        enemy.setStart(true);
                        roundJustEnded = false;

                        // attaque parallèle entre bancs
                        combatParallele(roster, active, enemy, adversairesArray, buffs, rng);

                    } else {
                        continue;
                    }

                } else {
                    System.out.println("Au tour de : " + enemy.getName() + " (" + enemy.getClass().getSimpleName() + ")");
                    attackWithBuffs(enemy, active, buffs);
                    System.out.println(active.getName() + " a " + active.getHp() + " HP\n");

                    if (active.getHp() <= 0) {
                        System.out.println("💥 " + active.getName() + " est KO !");
                        List<Personnage> vivants = alive(roster);
                        if (vivants.isEmpty()) break;
                        Personnage old = active;
                        active = vivants.get(0);
                        System.out.println("🔁 Remplacement automatique : " + old.getName() + " -> " + active.getName());
                    }

                    active.setStart(true); enemy.setStart(false); roundJustEnded = true;
                }

                if (roundJustEnded) { Item.tickBuffs(buffs); roundJustEnded = false; }
            }

            if (enemy.getHp() <= 0) {
                System.out.println("🏆 Vainqueur du duel : CÔTÉ JOUEUR (" + active.getName().toUpperCase() + ")\n");
            } else if (alive(roster).isEmpty()) {
                System.out.println("🏆 Vainqueur du duel : " + enemy.getName().toUpperCase() + "\n");
                break;
            }
        }

        System.out.println("Fin du combat !");
        List<Personnage> participants = new ArrayList<>(roster);
        participants.addAll(Arrays.asList(adversairesArray));
        printEndStats(participants);

        scheduler.shutdownNow();
    }

    private static void premierAttaque(Personnage a, Personnage b) {
        if (a.getSpeed() >= b.getSpeed()) {
            a.setStart(true);
            b.setStart(false);
        } else {
            b.setStart(true);
            a.setStart(false);
        }
    }
}
