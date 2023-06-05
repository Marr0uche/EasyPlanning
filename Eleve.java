public class Eleve implements Comparable<Eleve>
{
    /*--Attribut--*/

    private final String nom;
    private final String prenom;
    private final char   groupe;
    private final char   categorie;
    private       int    equipe;

    /*--Constructeur--*/

    public Eleve(String nom, String prenom, char groupe, char categorie)
    {
        this.nom       = nom;
        this.prenom    = prenom;
        this.groupe    = groupe;
        this.categorie = categorie;
        this.equipe    = -1;
    }

    /*--Méthodes--*/

    public String toString()
    {
        String sRet;

        sRet = String.format("%-10s\t%-10s\t%s",this.nom,this.prenom,this.groupe);
        return sRet;
    }

    public String getNom()       { return this.nom;       }
    public String getPrenom()    { return this.prenom;    }
    public char   getGroupe()    { return this.groupe;    }
    public char   getCategorie() { return this.categorie; }
    public int    getEquipe()    { return this.equipe;    }

    public void   setEquipe(int equipe) { this.equipe = equipe; }

    public int compareTo(Eleve autreEleve)
    {
        String nom1, nom2;

        nom1 = ( this.nom            + this.prenom            ).toLowerCase();
        nom2 = ( autreEleve.getNom() + autreEleve.getPrenom() ).toLowerCase();

        return nom1.compareTo(nom2);
    }
}
