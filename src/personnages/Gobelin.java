package personnages;

public class Gobelin extends Ennemi {

    public Gobelin(){ super(); }

    public Gobelin(String name){ super(name); }

    public Gobelin(String name, int hp, int defense, int atk, int speed, int luck, boolean start) {
        super(name, hp, defense, atk, speed, luck, start);
    }

    @Override
    public boolean dodge() {
        int base = Math.max(0, Math.min(50, getLuck() / 2));
        int bonus = 5;
        return Math.random() * 100 < (base + bonus);
    }
}
