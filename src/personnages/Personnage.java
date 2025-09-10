package personnages;

import stats.Stat;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Personnage implements IPersonnage {
    protected final Random random = new Random();

    private String name;
    private int hp;
    private int defense;
    private int atk;
    private int speed;
    private int luck;
    private boolean start;

    private final Map<Stat, Integer> stats = new EnumMap<>(Stat.class);
    private final Map<String, Integer> inventory = new TreeMap<>();

    private int totalDamageDealt = 0;
    private int totalDamageTaken = 0;
    private int hitsDone = 0;
    private int hitsReceived = 0;

    private final Object hpLock = new Object();
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public Personnage() {
        this.name = "Bot";
        this.hp = 50;
        this.defense = 15;
        this.atk = 35;
        this.speed = 75;
        this.luck = 5;
        this.start = true;

        syncStats();
    }

    public Personnage(String name) {
        this();
        this.name = name;
    }

    public Personnage(String name, int hp, int defense, int atk, int speed, int luck, boolean start) {
        this.name = name;
        this.hp = hp;
        this.defense = defense;
        this.atk = atk;
        this.speed = speed;
        this.luck = luck;
        this.start = start;

        syncStats();
    }

    private void syncStats() {
        stats.put(Stat.HP, hp);
        stats.put(Stat.DEFENSE, defense);
        stats.put(Stat.ATTAQUE, atk);
        stats.put(Stat.SPEED, speed);
        stats.put(Stat.LUCK, luck);
    }

    @Override
    public void performAttack(Personnage defender) {
        setBusy(true);
        try {
            if (defender.dodge()) {
                System.out.println(defender.getName() + " a esquivé !");
                return;
            }

            int damage = Math.max(1, this.atk - defender.defense / 2);
            defender.receiveDamage(damage);

            updateCombatStats(damage);
            defender.updateDefenseStats(damage);

            System.out.println(this.name + " attaque " + defender.name + " et inflige " + damage + " dégâts !");
        } finally {
            setBusy(false);
        }
    }

    private void updateCombatStats(int damage) {
        this.totalDamageDealt += damage;
        this.hitsDone++;
    }

    private void updateDefenseStats(int damage) {
        this.totalDamageTaken += damage;
        this.hitsReceived++;
    }

    public void receiveDamage(int damage) {
        if (damage < 0) damage = 0;
        synchronized (hpLock) {
            setHp(Math.max(0, getHp() - damage));
        }
    }

    public boolean healSafe(int amount, int cap) {
        if (amount <= 0) return false;
        synchronized (hpLock) {
            int currentHp = getHp();
            int newHp = Math.min(cap, currentHp + amount);
            if (newHp == currentHp) return false;
            setHp(newHp);
            return true;
        }
    }

    public boolean dodge() {
        int dodgeChance = Math.max(0, Math.min(50, this.luck / 2));
        return random.nextInt(100) < dodgeChance;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHp() { return hp; }
    public void setHp(int hp) {
        this.hp = hp;
        stats.put(Stat.HP, hp);
    }

    public int getDefense() { return defense; }
    public void setDefense(int defense) {
        this.defense = defense;
        stats.put(Stat.DEFENSE, defense);
    }

    public int getAtk() { return atk; }
    public void setAtk(int atk) {
        this.atk = atk;
        stats.put(Stat.ATTAQUE, atk);
    }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) {
        this.speed = speed;
        stats.put(Stat.SPEED, speed);
    }

    public int getLuck() { return luck; }
    public void setLuck(int luck) {
        this.luck = luck;
        stats.put(Stat.LUCK, luck);
    }

    public boolean isStart() { return start; }
    public void setStart(boolean start) { this.start = start; }

    public Map<Stat, Integer> getStats() { return stats; }
    public Map<String, Integer> getInventory() { return inventory; }

    public void addItem(String name, Integer nb) {
        this.inventory.put(name, nb);
    }

    public void removeItem(String name) {
        this.inventory.remove(name);
    }

    public boolean isBusy() { return busy.get(); }
    public void setBusy(boolean value) { busy.set(value); }

    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
    public int getHitsDone() { return hitsDone; }
    public int getHitsReceived() { return hitsReceived; }

    public void printInventorySorted() {
        System.out.println("Inventaire de " + getName() + " (trié) :");
        inventory.entrySet().forEach(entry ->
                System.out.println(" - " + entry.getKey() + " x" + entry.getValue())
        );
    }

    @Override
    public String toString() {
        return "Nom : " + name +
                "\nHp : " + hp +
                "\nDefense : " + defense +
                "\nAttaque : " + atk +
                "\nAgilité : " + speed +
                "\nChance : " + luck +
                "\nStart : " + start +
                "\nInventaire : " + inventory;
    }
}