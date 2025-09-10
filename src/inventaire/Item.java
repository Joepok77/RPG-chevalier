package inventaire;

import personnages.Personnage;

import java.util.Iterator;
import java.util.Map;

public class Item {
    private String name;
    private String description;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Item() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return this.name + " : " + this.description;
    }

    public static class Buff {
        public int atkBonus = 0;
        public int defBonus = 0;
        public int turns = 0;
    }

    public static Buff getBuff(Map<Personnage, Buff> buffs, Personnage p) {
        return buffs.computeIfAbsent(p, k -> new Buff());
    }

    public static void tickBuffs(Map<Personnage, Buff> buffs) {
        Iterator<Map.Entry<Personnage, Buff>> it = buffs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Personnage, Buff> entry = it.next();
            Buff buff = entry.getValue();
            if (buff.turns > 0) {
                buff.turns--;
                if (buff.turns == 0) {
                    buff.atkBonus = 0;
                    buff.defBonus = 0;
                }
            }
        }
    }

    public static void giveItem(Personnage p, String itemName, int qty) {
        if (qty <= 0) return;
        int currentQty = p.getInventory().getOrDefault(itemName, 0);
        p.getInventory().put(itemName, currentQty + qty);
    }

    public static boolean useApple(Personnage user, Personnage target, Map<Personnage, Integer> maxHp) {
        if (target.isBusy()) {
            System.out.println("⏳ " + target.getName() + " est en action : soin impossible pour l'instant.");
            return false;
        }

        int availableApples = user.getInventory().getOrDefault("Pomme", 0);
        if (availableApples == 0) {
            System.out.println("❌ Pas de Pomme dans l'inventaire.");
            return false;
        }

        int hpCap = maxHp.getOrDefault(target, target.getHp());
        boolean healed = target.healSafe(30, hpCap);

        if (!healed) {
            System.out.println("ℹ️ " + target.getName() + " est déjà au maximum (" + target.getHp() + "/" + hpCap + ").");
            return false;
        }

        user.getInventory().put("Pomme", availableApples - 1);
        System.out.println("🍎 " + target.getName() + " récupère des HP (" + target.getHp() + "/" + hpCap + ")");
        return true;
    }

    public static boolean useRage(Personnage user, Personnage target, Map<Personnage, Buff> buffs) {
        int availableRage = user.getInventory().getOrDefault("Rage", 0);
        if (availableRage == 0) {
            System.out.println("❌ Vous n'avez pas de Rage.");
            return false;
        }

        user.getInventory().put("Rage", availableRage - 1);
        Buff buff = getBuff(buffs, target);
        buff.atkBonus += 20;
        buff.turns = Math.max(buff.turns, 3);

        System.out.println("🔥 " + target.getName() + " gagne +20 ATK pendant 3 tours !");
        return true;
    }

    public static boolean useShield(Personnage user, Personnage target, Map<Personnage, Buff> buffs) {
        int availableShields = user.getInventory().getOrDefault("Bouclier", 0);
        if (availableShields == 0) {
            System.out.println("❌ Vous n'avez pas de Bouclier.");
            return false;
        }

        user.getInventory().put("Bouclier", availableShields - 1);
        Buff buff = getBuff(buffs, target);
        buff.defBonus += 20;
        buff.turns = Math.max(buff.turns, 3);

        System.out.println("🛡️ " + target.getName() + " gagne +20 DEF pendant 3 tours !");
        return true;
    }
}