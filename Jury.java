import java.util.Locale;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Jury 
{
    /*--Attributs--*/
    
    private final int      numJury;
    private final String   numSalle;
    private final String   date;
    private final String   heureD;
    private final String   heureF;
    private final int      tempsAct;
    private final int      pause;
    private final String[] profs;
    private       Equipe[] equipes;

    static String lastDate;

    /*--Constructeurs--*/

    public Jury(int numJury, String numSalle, String date, String heureD, String heureF, int tempsAct, int pause, String[] profs)  
    {
        this.numJury    = numJury;
        this.numSalle   = numSalle;
        this.date       = date;
        this.heureD     = heureD;
        this.heureF     = heureF;
        this.tempsAct   = tempsAct;
        this.pause      = pause;
        this.profs      = profs;
        this.equipes    = new Equipe[0];
    }

    /*--Méthodes--*/

    //accesseurs
    public int      getNumJury()  { return this.numJury;               }
    public String   getNumSalle() { return this.numSalle;              }
    public String   getDate()     { return this.formatDate(this.date); }
    public String   getHeureD()   { return this.heureD;                }
    public String   getHeureF()   { return this.heureF;                }
    public String[] getProfs()    { return this.profs;                 }
    public Equipe[] getEquipe()   { return this.equipes;               }

    public void addEquipe(Equipe equipeAutre)
    {
        Equipe[] tabTemp = new Equipe[this.equipes.length + 1];

        System.arraycopy(this.equipes, 0, tabTemp, 0, this.equipes.length);

        equipeAutre.setSalle(this.numSalle);

        tabTemp[this.equipes.length] = equipeAutre;
        this.equipes                 = tabTemp;
    }

    public boolean encoreDuTemps()
    {
        return enMinute(this.heureF) - enMinute(this.heureD) >= (this.equipes.length + 1) * this.tempsAct + (this.equipes.length) * this.pause;
    }

    public static int enMinute(String duree)
    {
        int dureeEnMinute;
        int H;
        int M;

        H   = Integer.parseInt( "" + duree.charAt(0) + duree.charAt(1) );
        M   = Integer.parseInt( "" + duree.charAt(3) + duree.charAt(4) );

        dureeEnMinute = H*60 + M;

        return dureeEnMinute;
    }

    public String getHoraire(int numEquipe)
    {
        int m1, m2, h1, h2;
        int timeToAdd;

        h1   = Integer.parseInt( "" + this.heureD.charAt(0) + this.heureD.charAt(1) ) * 60;
        m1   = Integer.parseInt( "" + this.heureD.charAt(3) + this.heureD.charAt(4) );

        timeToAdd = (numEquipe + 1)*this.tempsAct + numEquipe*this.pause;

        h2 = (h1+m1+ timeToAdd)/60;
        m2 = (h1+m1+ timeToAdd)%60;

        h1=h1/60;

        h1=(h1*60+m1 + (timeToAdd-this.tempsAct))/60;
        m1=(h1*60+m1 + (timeToAdd-this.tempsAct))%60;

        return ""      + String.format( "%2d",  h1 ) +
               'h'     + String.format( "%02d", m1 ) +
               "\tà\t" + String.format( "%2d",  h2 ) +
               'h'     + String.format( "%02d", m2 );
    }

    public String toString()
    {
        String sRet       = "";
        String sSeparator = "\n";
        
        String date = formatDate(this.date);

        // Si la dernière date affiché est nulle alors on l'initialise
        if (lastDate == null) { lastDate = ""; }

        // On ajoute la date du jour des oraux si elle a pas déjà été mise
        // Pour éviter d'avoir plusieurs  fois la même date
        if (!lastDate.equals(date))
        {
            for (int cpt = 0; cpt < date.length(); cpt++) 
                sSeparator = '-' + sSeparator;

            sRet     = sSeparator + date + '\n' + sSeparator;
            lastDate = date;
        }
        
        // Mise an place du nom & numéro du jury ainsi que de la liste des professeurs
        sRet += "\nJury " + this.numJury + '\n';
        for (String prof : this.profs) sRet += prof + '\n';
            
        sRet += '\n';    

        // Mise en place des horaires par équipes
        for (int cptCol = 0; cptCol < this.equipes.length; cptCol++) 
            sRet += getHoraire(cptCol) + "\tEquipe " + this.equipes[cptCol].getNum() + "\tSalle " + this.numSalle + '\n';

        return sRet;
    }

    private String formatDate(String date)
    {
        int num;
        int mois;
        int annees;
        
        Locale locale = new Locale("fr", "FR");

        num     = Integer.parseInt( "" + date.charAt(0) + date.charAt(1) );
        mois    = Integer.parseInt( "" + date.charAt(3) + date.charAt(4) );
        annees  = Integer.parseInt( "" + date.charAt(6) + date.charAt(7) + date.charAt(8) + date.charAt(9));
        
        LocalDate localDate = LocalDate.of(annees, mois, num);

        String jour    = localDate.format( DateTimeFormatter.ofPattern( "EEEE", locale ) );
        String num2    = localDate.format( DateTimeFormatter.ofPattern( "d",    locale ) );
        String mois2   = localDate.format( DateTimeFormatter.ofPattern( "MMMM", locale ) );
        String annees2 = localDate.format( DateTimeFormatter.ofPattern( "yyyy", locale ) );

        jour  = jour .substring(0, 1).toUpperCase() + jour .substring(1);
        mois2 = mois2.substring(0, 1).toUpperCase() + mois2.substring(1);

        return String.format("%s %s %s %s", jour, num2, mois2, annees2);
    }
}