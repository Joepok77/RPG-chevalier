package personnages;

public class Ennemi extends Personnage {

    public Ennemi(String name, int hp, int defense, int atk, int speed, int luck, boolean start) {
        super(name, hp, defense, atk, speed, luck, start);
    }

    public Ennemi() { super(); }
    public Ennemi(String name) { super(name); }

    @Override
    public String toString() {
        return "Nom : " + this.getName() +
                "\nHp : " + this.getHp() +
                "\nDefense : " + this.getDefense() +
                "\nAttaque : " + this.getAtk() +
                "\nAgilité : " + this.getSpeed() +
                "\nChance : " + this.getLuck() +
                "\nStart : " + this.isStart() + "\n";
    }

    // tu peux surcharger performAttack ici si tu veux un comportement différent
}
