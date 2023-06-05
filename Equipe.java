public class Equipe 
{
    /*--Attribut--*/

    private final int     num;
    private final Eleve[] eleves;
    private       String  salle;
    public static int     tailleMax;

    /*--Constructeur--*/

    public Equipe(int num, String salle, Eleve[] eleves)
    {
        this.num = num;
        this.salle = salle;
        this.eleves = eleves;

        for (Eleve eleve : eleves)
            eleve.setEquipe(num);
    }

    public Equipe(int num, Eleve[] eleves)
    {
        this(num, "", eleves);
    }

    /*--Méthodes--*/

    public int     getNum()   { return this.num;    }
    public String  getSalle() { return this.salle;  }
    public Eleve[] getEleve() { return this.eleves; }

    public void setSalle(String salle) { this.salle = salle; }

    public String toString()
    {
        String sRet;

        sRet = String.format("Equipe %s\n", this.num);

        for (Eleve eleve : eleves) {
            sRet += String.format("%s\n", eleve);
        }

        return sRet;
    }
}
