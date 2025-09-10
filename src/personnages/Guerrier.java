package personnages;

public class Guerrier extends Personnage {
    public Guerrier(String name, int hp, int defense, int atk, int speed, int luck, boolean start){
        super(name, hp, defense, atk, speed, luck, start);
    }
    public Guerrier(){ super(); }
    public Guerrier(String name){ super(name); }

    @Override
    public String toString(){
        return "Nom : " + this.getName() +
                "\nHp : " + this.getHp() +
                "\nDefense : " + this.getDefense() +
                "\nAttaque : " + this.getAtk() +
                "\nAgilité : " + this.getSpeed() +
                "\nChance : " + this.getLuck() +
                "\nStart : " + this.isStart() + "\n";
    }
}
