import java.util.Scanner;
import java.io.FileReader;
import java.io.*;
import iut.algo.*;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Desktop;

public class Main
{
    public static void main(String[] args)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        Equipe[]    equipes;
        Jury[]      juries;
        boolean     trieParCategorie;
        Eleve[][]   eleves;
        boolean     modeSombre;

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/

        // Lecture du choix de l'utilisateur
        System.out.print("Trier par Catégorie (O/N) : ");

        trieParCategorie = Character.toUpperCase(Clavier.lire_char()) == 'O';

        // Lecture du choix de l'utilisateur
        System.out.print("Mode sombre         (O/N) : ");

        modeSombre = Character.toUpperCase(Clavier.lire_char()) == 'O';

        // Création des élèves
        eleves = Main.genererEleve(trieParCategorie);

        // Création des équipes
        equipes = Main.genererEquipe(eleves);

        // Répartition des salles de groupes
        Main.repartitionSalle(equipes);

        // Création des pages un et deux en HTML avec leurs fichiers CSS respectif
        Main.genererHtmlpage1(equipes);
        Main.genererHtmlpage2(equipes, trieParCategorie);

        // Création des juries
        juries = Main.genererJury(equipes);

        // Création de la page trois en HTML avec son fichier CSS
        Main.genererHtmlpage3(juries);

        // Création de la page d'acceuil en HTML avec ses fichiers CSS
        Main.genererHtmlAcceuil(modeSombre);
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour créer les élèves*/

    public static Eleve[][] genererEleve(boolean trieParCategorie)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Constantes
        final int MAX_CAT = 26;

        //Variables
        Eleve[][] eleves;
        int[]     nbEleve; // Nombre d'élèves par catégorie
        int[]     cptEleve;


        /*----------------*/
        /*--Instructions--*/
        /*----------------*/

        // Initialisation des tableaux
        nbEleve    = new int[MAX_CAT];
        cptEleve   = new int[MAX_CAT];
        eleves     = new Eleve[MAX_CAT][];

        try
        {
            Scanner sc = new Scanner ( new FileReader("promotion.data") );
            sc.nextLine();

            while ( sc.hasNextLine() )
            {
                // Récupération du nombre d'élève par catégorie
                Decomposeur dec       = new Decomposeur(sc.nextLine());
                char        categorie = dec.getChar(3);

                if (!trieParCategorie) { categorie = 'A'; }

                nbEleve[categorie - 'A']++;
            }

            sc.close();

            // Initialisation des tableau d'élèves avec les tailles respectives ainsi que des compteurs d'élèves
            for (int cpt = 0; cpt < nbEleve.length; cpt++) {
                eleves[cpt]   = new Eleve[nbEleve[cpt]];
                cptEleve[cpt] = 0;
            }

            sc = new Scanner ( new FileReader("promotion.data") );
            sc.nextLine();

            while ( sc.hasNextLine() )
            {
                Decomposeur dec = new Decomposeur(sc.nextLine());

                // Création des élèves, on doit avoir le nom, prénom, groupe et catégorie individiuellement 
                String nom     = dec.getString(0);
                String prenom  = dec.getString(1);
                char groupe    = dec.getChar(2);
                char categorie = dec.getChar(3);

                // Si on ne trie pas par catégorie, touts les élèves sont mis dans la catégorie A
                // Néanmoins, on ajoute quand même la bonne catégorie dans le constructeur de l'élève
                if (!trieParCategorie) { categorie = 'A'; }

                eleves  [categorie - 'A'][cptEleve[categorie - 'A']] = new Eleve(nom, prenom, groupe, dec.getChar(3));
                cptEleve[categorie - 'A']++;
            }

            sc.close();
        }
        catch (Exception e){
            // Initialisation du tableau d'élève au cas ou il y a une erreur pour que le programme continu à s'exécuter
            eleves = new Eleve[0][0];
            e.printStackTrace();
        }

        // Tri des élèves par ordre alphabétique
        for (Eleve[] eleve : eleves)
            Arrays.sort(eleve);

        return eleves;
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo créer les équipes*/

    public static Equipe[] genererEquipe(Eleve[][] eleves)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        int         nbEquipe;
        int         numEquipe;
        int         tailleEquipe;
        Equipe[]    equipes;


        /*----------------*/
        /*--Instructions--*/
        /*----------------*/

        // Initialisation des variables
        tailleEquipe = 0;
        nbEquipe     = 0;
        numEquipe    = 0;

        // Initialisation de la taille des équipes
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("ressources.data"));
            tailleEquipe      = Integer.parseInt(br.readLine());
            
            Equipe.tailleMax = tailleEquipe + 1;

            br.close();
        }
        catch (Exception e){ e.printStackTrace(); }

        // Calcule du nombre d'équipes
        for (Eleve[] eleve : eleves)
        {
            // Simulation de la création des équipes pour définir le nombre d'équipes à créer
            int testNbEquipeSup      =  eleve.length % tailleEquipe;
            int testNbEquipeStandard = (eleve.length - testNbEquipeSup * (tailleEquipe + 1)) / tailleEquipe;

            if (testNbEquipeStandard >= 0)
            {
                nbEquipe += testNbEquipeStandard + testNbEquipeSup;
            }
            else
            {
                nbEquipe += eleve.length / tailleEquipe + 1;
            }
        }

        // Initialisation un tableau d'équipes
        equipes = new Equipe[nbEquipe];

        // Création des équipes
        for (Eleve[] eleve : eleves)
        {
            int nbEquipeSup      =  eleve.length % tailleEquipe;                                     // équipes auquelle on rajoute un élève
            int nbEquipeStandard = (eleve.length - nbEquipeSup * (tailleEquipe + 1)) / tailleEquipe; // équipes avec la taille par défault
            int nbReste          = 0;

            // Prise en charge des erreurs de créations de groupes trop petits
            if (nbEquipeStandard < 0)
            {
                nbEquipeSup      = 0;
                nbEquipeStandard = eleve.length / tailleEquipe;
                nbReste          = eleve.length - nbEquipeStandard * tailleEquipe;
            }

            // Création des équipes de taille par défaut
            for (int numEquipeCategorie = 0; numEquipeCategorie < nbEquipeStandard; numEquipeCategorie++) 
            {
                Eleve[] equipeActuelle = new Eleve[tailleEquipe];

                System.arraycopy(eleve, numEquipeCategorie * tailleEquipe, equipeActuelle, 0, tailleEquipe);

                equipes[numEquipe] = new Equipe(numEquipe + 1, equipeActuelle);
                numEquipe++;
            }

            // Création des équipes de un élève en plus
            for (int numEquipeCategorie = 0; numEquipeCategorie < nbEquipeSup; numEquipeCategorie++) 
            {
                Eleve[] equipeActuelle = new Eleve[tailleEquipe + 1];

                for (int cptEleve = 0; cptEleve < tailleEquipe + 1; cptEleve++) 
                    equipeActuelle[cptEleve] = eleve[nbEquipeStandard * tailleEquipe + numEquipeCategorie * (tailleEquipe + 1) + cptEleve];
                

                equipes[numEquipe] = new Equipe(numEquipe + 1, equipeActuelle);
                numEquipe++;
            }

            // Création des équipes avec les élèves en trop
            if (nbReste > 0)
            {
                Eleve[] equipeActuelle = new Eleve[nbReste];

                for (int cptEleve = 0; cptEleve < nbReste; cptEleve++)
                    equipeActuelle[cptEleve] = eleve[nbEquipeStandard * tailleEquipe + cptEleve];

                equipes[numEquipe] = new Equipe(numEquipe + 1, equipeActuelle);
                numEquipe++;
            }
        }

        return equipes;
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour générer un jury*/

    public static Jury[] genererJury(Equipe[] equipes)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        Jury[] tabJury;
        int nbJury;
        int tempsPause;
        int tempsActivite;
        int cptEquipe;

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/

        try
        {
            Scanner     sc  = new Scanner    ( new FileReader("jury.data") );
            Decomposeur dec = new Decomposeur( sc.nextLine() );

            tempsActivite   = dec.getInt(0); // Définiion du temps d'activité
            tempsPause      = dec.getInt(1); // Définiion du temps de pause
            
            // Comptage du nombre de juries
            nbJury = 0;
            while (sc.hasNextLine()) { sc.nextLine(); nbJury++; }
            
            sc.close();
            
            // Initialisation du tableau de jury
            tabJury = new Jury[nbJury];

            sc = new Scanner ( new FileReader("jury.data") );
            sc.nextLine();

            // Création des juries
            for (int cpt = 0; cpt < tabJury.length; cpt++)
            {
                String line = sc.nextLine();
                Decomposeur decJury = new Decomposeur(line);
                
                //Compter le nombre de professeurs qu'un jury contient
                int nbSpace = 0;
                for (int i = 0; i < line.length(); i++) { if (line.charAt(i) == '\t') { nbSpace++; } }

                // Initialiser le tableau de profs
                String[] tabProf = new String[nbSpace - 4];
                
                // Remplir le tableau de profs
                for (int cptProf = 0; cptProf < tabProf.length; cptProf++) 
                { tabProf[cptProf] = decJury.getString(5+cptProf); }

                // Récupération du numéro du jury
                int numJuryActuel = Integer.parseInt( "" + decJury.getString(0).substring(5, decJury.getString(0).length()) );

                // Créer les jury
                tabJury[cpt] = new Jury(numJuryActuel, decJury.getString(1), decJury.getString(2), decJury.getString(3), decJury.getString(4), tempsActivite, tempsPause, tabProf);
            }
            
            sc.close();
        }
        catch (Exception e){ e.printStackTrace(); tabJury = new Jury[0]; }

        // Ajout des équipes aux juries
        cptEquipe = 0;

        for (Jury jury : tabJury) {
            while (jury.encoreDuTemps() && equipes.length > cptEquipe) {
                jury.addEquipe(equipes[cptEquipe]);
                cptEquipe++;
            }
        }

        return tabJury;
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo réparti les salles dans les équipes*/

    public static void repartitionSalle(Equipe[] equipes)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        int cptEquipe;


        /*----------------*/
        /*--Instructions--*/
        /*----------------*/

        try
        {
            // Initialisation du compteur d'équipes et de place occupée
            cptEquipe = 0;

            //Lecture du fichier pour compter le nombre de groupes par salles
            Scanner sc = new Scanner ( new FileReader("ressources.data") );
            sc.nextLine();

            while(sc.hasNextLine())
            {
                String[] parts = sc.nextLine().split("\t");
                String   salle = parts[0];
                int      place = Integer.parseInt(parts[1]);

                for (int cpt = 0; cpt < place; cpt++) {
                    if (cptEquipe < equipes.length)
                    {
                        equipes[cptEquipe].setSalle(salle);

                        cptEquipe   ++;
                    }
                }
            }

            sc.close();
        }
        catch (Exception e){ e.printStackTrace(); }
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour générer la première page html/css*/

    public static void genererHtmlpage1(Equipe[] equipes)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        String codeTop;
        String codeBottom;
        String codeMiddle;
        String codeCss;

        Eleve[] eleves;
        int     cpt;

        PrintWriter pwA; // Fichier HTML
        PrintWriter pwB; // Fichier CSS

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/
        
        codeTop = "<!DOCTYPE html>\n" +
                  "<html lang=\"fr\">\n" +
                  "\t<head>\n" +
                  "\t\t<meta charset=\"Utf-8\">\n" +
                  "\t\t<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"page1.css\">\n" +
                  "\n" +
                  "\t\t<title>Liste des étudiants </title>\n" +
                  "\n" +
                  "\t</head>\n" +
                  "\t<body>\n" +
                  "\t\t<header> Liste des étudiants </header>\n" +
                  "\n" +
                  "\t\t<div class=\"top\">\n" +
                  "\t\t\t<table>\n" +
                  "\t\t\t\t<tbody>\n" +
                  "\t\t\t\t\t<tr> <th>Nom</th> <th>Prénom</th> <th>Groupe</th> <th>N°équipe</th> </tr>\n" +
                  "\t\t\t\t</tbody>\n" +
                  "\t\t\t</table>\n" +
                  "\t\t</div>\n" +
                  "\n" +
                  "\t\t<div class=\"scroller\">\n" +
                  "\t\t\t<table>\n" +
                  "\t\t\t\t<tbody>\n";

        codeBottom = "\t\t\t\t</tbody>\n" +
                     "\t\t\t</table>\n" +
                     "\t\t</div>\n" +
                     "\t</body>\n" +
                     "</html>\n";

        codeMiddle = "\t\t\t\t\t<tr> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> </tr>\n";

        codeCss = "@font-face\n" +
                  "{\n" +
                  "\tfont-family: \"Iceland\";\n" +
                  "\tsrc : url(\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/Iceland/Iceland-Regular.ttf\");\n" +
                  "}\n" +
                  "body\n" + 
                  "{\n" + 
                  "\tfont-family: Iceland;\n" +
                  "}\n" +
                  "header {\n" +
                  "\tmargin-left : 20%;\n" +
                  "\twidth : 60%;\n" +
                  "\theight : 55px;\n" +
                  "\tborder : black solid 2px;\n" +
                  "\ttext-align : center;\n" +
                  "\tpadding-top : 20px;\n" +
                  "\tfont-size : 200%;\n" +
                  "\tfont-weight : bold;\n" +
                  "\tbackground-color: #e6ccab;\n" +
                  "}\n" +
                  "\n" +
                  "tr:nth-child(even) {\n" +
                  "\tbackground-color : #f2f2f2;\n" +
                  "}\n" +
                  "tr:nth-child(odd) {\n" +
                  "\tbackground-color : #e6ccab;\n" +
                  "}\n" +
                  "\n" +
                  ".top {\n" +
                  "\tmargin-top : 30px;\n" +
                  "\twidth : 60%;\n" +
                  "\tmargin-left : 20%;\n" +
                  "}\n" +
                  "\n" +
                  "table {\n" +
                  "\twidth : 100%;\n" +
                  "\tborder-spacing : 0;\n" +
                  "}\n" +
                  "\n" +
                  "th {\n" +
                  "\twidth : 25%;\n" +
                  "\theight : 40px;\n" +
                  "\tbackground-color : #333f4f;\n" +
                  "\tcolor : white;\n" +
                  "\ttext-align : center;\n" +
                  "\tfont-size : 130%;\n" +
                  "}\n" +
                  "\n" +
                  "td {\n" +
                  "\twidth : 25%;\n" +
                  "\theight : 40px;\n" +
                  "\ttext-align : center;\n" +
                  "\tfont-size : 130%;\n" +
                  "}\n" +
                  "\n" +
                  ".scroller {\n" +
                  "\theight : 500px;\n" +
                  "\twidth : 61%;\n" +
                  "\tmargin-left : 20%;\n" +
                  "\toverflow-y : scroll;\n" +
                  "}\n" +
                  "::-webkit-scrollbar {\n" +
                  "\twidth: 10px;" +
                  "}\n" +
                  "/* Track */\n" +
                  "::-webkit-scrollbar-track {\n" +
                  "box-shadow: inset 0 0 5px #6B0F1A;\n" +
                  "\tborder-radius: 10px;\n" +
                  "}\n" +
                  "/* Handle */\n" +
                  "::-webkit-scrollbar-thumb {\n" +
                  "\tbackground: #282a3a;\n" +
                  "\tborder-radius: 10px;\n" +
                  "}\n";

        try {
            // Création du fichier HTML
            pwA = new PrintWriter("page1.html");

            pwA.write(codeTop);

            cpt = 0;
            for (Equipe equipe : equipes)
                cpt += equipe.getEleve().length;

            eleves = new Eleve[cpt];

            cpt = 0;
            for (Equipe equipe : equipes)
            {
                for (Eleve eleve : equipe.getEleve())
                {
                    eleves[cpt] = eleve;
                    cpt++;
                }
            }

            Arrays.sort(eleves);

            for (Eleve eleve : eleves) {
                pwA.write(String.format(codeMiddle, eleve.getNom(), eleve.getPrenom(), eleve.getGroupe(), eleve.getEquipe()));
            }

            pwA.write(codeBottom);
            pwA.close();

            // Création du fichier CSS
            pwB = new PrintWriter("page1.css");

            pwB.write(codeCss);

            pwB.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour générer la deuxième page html/css*/

    public static void genererHtmlpage2(Equipe[] equipes, boolean trieParCategorie)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Constantes
        final Color[] colors = new Color[] {
            new Color( 172, 133, 36  ), // jaune
            new Color( 61,  112, 169 ), // bleu
            new Color( 159, 61,  51  ), // rouge
            new Color( 130, 84,  50  ), // marron
            new Color( 128, 199, 31  ), // lime
            new Color( 93,  124, 21  ), // vert
            new Color( 58,  179, 218 ), // bleu clair
            new Color( 22,  156, 157 ), // cyan
            new Color( 243, 140, 170 ), // rose
            new Color( 198, 79,  189 ), // magenta
            new Color( 137, 50,  183 ), // purple
            new Color( 249, 128, 29  ), // orange
            new Color( 249, 255, 255 ), // blanc
            new Color( 156, 157, 151 ), // gris clair
            new Color( 71,  79,  82  ), // gris
            new Color( 29,  28,  33  )  // noir
        };

        //Variables
        String codeTop;
        String codeBottom;
        String codeMiddleA;
        String codeMiddleB1;
        String codeMiddleB2;
        String codeMiddleBEntre;
        String codeMiddleC;
        String codeCssTop;
        String codeCssMiddle;
        String codeCssBottom;

        int     nbCategorie;
        int     size;
        int     lastMargin;
        int     lastCat;
        int     categorie;
        int     cptEleve;

        PrintWriter pwA; // Fichier HTML
        PrintWriter pwB; // Fichier CSS

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/
        
        codeTop = "<!DOCTYPE html>\n" +
                  "<html lang=\"fr\">\n" +
                  "\t<head>\n" +
                  "\t\t<meta charset=\"Utf-8\">\n" +
                  "\t\t<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"page2.css\">\n" +
                  "\n" +
                  "\t\t<title>Liste des équipes </title>\n" +
                  "\n" +
                  "\t</head>\n" +
                  "\t<body>\n" +
                  "\t\t<header> Liste des équipes </header>\n" +
                  "\n" +
                  "\t\t<div class=\"equipes\">\n";

        codeBottom = "\t\t</div>\n" +
                     "\t</body>\n" +
                     "</html>\n";

        codeMiddleA = "\t\t\t<div class=\"categorie-%s\">\n" +
                      "\t\t\t\t<table>\n" +
                      "\t\t\t\t\t<tbody>\n";

        codeMiddleB1 = "\t\t\t\t\t\t<tr> <td class=\"side\" rowspan=\"%s\" style=\"background-color: #%s;\">%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td class=\"side\" rowspan=\"%s\" style=\"background-color: #%s;\">%s</td> </tr>\n";

        codeMiddleB2 = "\t\t\t\t\t\t<tr> <td>%s</td> <td>%s</td> <td>%s</td> </tr>\n";

        codeMiddleBEntre = "\t\t\t\t\t</tbody>\n" +
                           "\t\t\t\t</table>\n" +
                           "\t\t\t\t<table>\n" +
                           "\t\t\t\t\t<tbody>\n";

        codeMiddleC = "\t\t\t\t\t</tbody>\n" +
                      "\t\t\t\t</table>\n" +
                      "\t\t\t</div>\n";

        codeCssTop = "@font-face\n" +
                     "{\n" +
                     "\tfont-family: \"Iceland\";\n" +
                     "\tsrc : url(\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/Iceland/Iceland-Regular.ttf\");\n" +
                     "}\n" +
                     "header {\n" +
                     "\theight : 20px;\n" +
                     "\tborder : black solid 2px;\n" +
                     "\ttext-align : center;\n" +
                     "\tpadding-top : 5px;\n" +
                     "\tfont-weight : bold;\n" +
                     "\tmargin: 8px 8px 8px 8px;\n" +
                     "\tbackground-color: #e6ccab;\n" +
                     "}\n" +
                     "\n" +
                     "body {\n" +
                     "\tmargin: 0px 0px 0px 0px;\n" +
                     "\tfont-family: Iceland;\n" +
                     "\toverflow-y: scroll;\n" +
                     "}\n" +
                     "\n" +
                     ".equipes {\n" +
                     "\tmargin-left: 0px;\n" +
                     "\tmargin-top: 15px;\n" +
                     "\twidth: 100%;\n" +
                     "}\n" +
                     "\n";

        codeCssMiddle = ".categorie-%s {\n" +
                        "\tposition: absolute;\n" +
                        "\twidth: %s%%;\n" +
                        "\tmargin-left: %s%%;\n" +
                        "}\n" +
                        "\n";

        codeCssBottom = "td {\n" +
                        "\twidth : 20%;\n" +
                        "\theight : 20px;\n" +
                        "\ttext-align : left;\n" +
                        "\tfont-size : 100%;\n" +
                        "}\n" +
                        "\n" +
                        "td.side {\n" +
                        "\twidth : 10%;\n" +
                        "\ttext-align : center;\n" +
                        "}\n" +
                        "\n" +
                        "table {\n" +
                        "\tborder: 1px solid;\n" +
                        "\tbackground-color: #e6ccab;\n" +
                        "}\n" +
                        "\n" +
                        "tr .side {\n" +
                        "\tborder: 1px solid;\n" +
                        "}\n" +
                        "\n" +
                        "table {\n" +
                        "\twidth: 100%;\n" +
                        "\tmargin-top: 15px;\n" +
                        "\tborder-collapse: collapse;\n" +
                        "\twhite-space: pre;\n" +
                        "}\n" +
                        "::-webkit-scrollbar {\n" +
                        "\twidth: 10px;" +
                        "}\n" +
                        "/* Track */\n" +
                        "::-webkit-scrollbar-track {\n" +
                        "box-shadow: inset 0 0 5px #6B0F1A;\n" +
                        "\tborder-radius: 10px;\n" +
                        "}\n" +
                        "/* Handle */\n" +
                        "::-webkit-scrollbar-thumb {\n" +
                        "\tbackground: #282a3a;\n" +
                        "\tborder-radius: 10px;\n" +
                        "}\n";

        try {
            // Création du fichier HTML
            pwA = new PrintWriter("page2.html");

            pwA.write(codeTop);

            // Comptage du nombre de catégorie
            nbCategorie = 0;
            for (Equipe equipe : equipes) {
                char cat = equipe.getEleve()[0].getCategorie();

                if (cat - 'A' + 1 > nbCategorie) { nbCategorie = cat - 'A' + 1; }
            }

            // Création des équipes par catégorie
            lastCat = -1;

            for (Equipe equipe : equipes) {
                categorie = equipe.getEleve()[0].getCategorie() - 'A';

                if (!trieParCategorie) { categorie = 0; }

                if (lastCat != categorie)
                {
                    lastCat = categorie;

                    if (lastCat != 0)
                        pwA.write(codeMiddleC + "\t\n");
                    pwA.write(String.format(codeMiddleA, (char) (lastCat + 'A')));
                }
                else
                {
                    pwA.write(codeMiddleBEntre);
                }

                cptEleve = 0;

                for (Eleve eleve : equipe.getEleve()) {
                    if (eleve == equipe.getEleve()[0])
                    {
                        String color = Main.colorToHex(colors, lastCat);

                        pwA.write(String.format(codeMiddleB1, equipes[0].getEleve().length + 1, color, equipe.getNum(), eleve.getNom(), eleve.getPrenom(), eleve.getGroupe(), equipes[0].getEleve().length + 1, color, equipe.getSalle()));
                    }
                    else
                    {
                        pwA.write(String.format(codeMiddleB2, eleve.getNom(), eleve.getPrenom(), eleve.getGroupe()));
                    }

                    cptEleve++;
                }

                for (int cpt = 0; cpt < Equipe.tailleMax - cptEleve; cpt++)
                    pwA.write(String.format(codeMiddleB2, " ", " ", " "));
            }

            pwA.write(codeMiddleC);
            pwA.write(codeBottom);
            pwA.close();

            // Création du fichier CSS
            pwB = new PrintWriter("page2.css");

            pwB.write(codeCssTop);

            lastMargin = 3;
            size = 97/nbCategorie-3;
            for (int cpt = 0; cpt < nbCategorie; cpt++) {
                pwB.write(String.format(codeCssMiddle, (char) (cpt + 'A'), size, lastMargin));
                lastMargin += size + 3;
            }

            pwB.write(codeCssBottom);
            pwB.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour transformer les couleur java en hexa pour les utiliser dans le code html*/

    public static String colorToHex(Color[] colors, int colorIndex)
    {
        String sRet;

        while (colorIndex >= colors.length) { colorIndex -= colors.length; }

        sRet  = Integer.toHexString(colors[colorIndex].getRed());
        sRet += Integer.toHexString(colors[colorIndex].getGreen());
        sRet += Integer.toHexString(colors[colorIndex].getBlue());
        sRet += Integer.toHexString(colors[colorIndex].getAlpha());

        colors[colorIndex] = new Color(colors[colorIndex].getRed(), colors[colorIndex].getGreen(), colors[colorIndex].getBlue(), (int) (colors[colorIndex].getAlpha() * 0.90));

        return sRet;
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour générer la troisième page html/css*/

    public static void genererHtmlpage3(Jury[] juries)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        String codeTop;
        String codeBottom;
        String codeMiddleSepA;
        String codeMiddleSepB;
        String codeMiddleA;
        String codeMiddleB;
        String codeMiddleC;
        String codeCss;

        String   profs;
        String   lastDate;
        String[] horaires;

        PrintWriter pwA; // Fichier HTML
        PrintWriter pwB; // Fichier CSS

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/
        
        codeTop = "<!DOCTYPE html>\n" +
                  "<html lang=\"fr\">\n" +
                  "\t<head>\n" +
                  "\t\t<meta charset=\"Utf-8\">\n" +
                  "\t\t<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"page3.css\">\n" +
                  "\n" +
                  "\t\t<title>Planning</title>\n" +
                  "\t</head>\n" +
                  "\t<body>\n";

        codeBottom = "\t</body>\n" +
                     "</html>\n";

        codeMiddleSepA = "\t\t<div class=\"day\">\n" +
                         "\t\t\t<div class=\"day_label\"><h4><b>%s</b></h4></div>\n" +
                         "\n";

        codeMiddleSepB = "\t\t</div>\n" +
                         "\n";

        codeMiddleA = "\t\t\t<div class=\"jury\">\n" +
                      "\t\t\t\t<div class=\"header\">Jury %s</div>\n" +
                      "\t\t\t\t<p>%s</p>\n";

        codeMiddleB = "\n" +
                      "\t\t\t\t<div class=bloque>\n" +
                      "\t\t\t\t\t<div class=\"heure\">%s<br><br>%s</div>\n" +
                      "\t\t\t\t\t<div class=\"salle\">%s<br>%s</div>\n" +
                      "\t\t\t\t</div>\n";

        codeMiddleC = "\t\t\t</div>\n";

        codeCss = "@font-face\n" +
                  "{\n" +
                  "\tfont-family: \"Iceland\";\n" +
                  "\tsrc : url(\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/Iceland/Iceland-Regular.ttf\");\n" +
                  "}\n" +
                  "body\n" + 
                  "{\n" + 
                  "\tfont-family: Iceland;\n" +
                  "}\n" +
                  ".day_label {\n" +
                  "\tborder-style: solid none solid none;\n" +
                  "\twidth: 175px;\n" +
                  "\tfont-size: 87.5%;\n" +
                  "\ttext-align: center;\n" +
                  "\tborder-width: 1px;\n" +
                  "\tposition: absolute;\n" +
                  "}\n" +
                  "\n" +
                  ".jury {\n" +
                  "\tborder-style: solid solid solid solid;\n" +
                  "\twidth: 175px;\n" +
                  "\tborder-width: 1px;\n" +
                  "\tmargin: 40px 25px 20px 0px;\n" +
                  "\tbackground-color: darkgray;\n" +
	              "\tcolor: #282a3a;\n" +
                  "}\n" +
                  "\n" +
                  ".header {\n" +
                  "\tbackground-color: #282a3a;\n" +
                  "\tcolor: #c58940;\n" +
                  "\tfont-weight: bold;\n" +
                  "}\n" +
                  "\n" +
                  "hr {\n" +
                  "\twidth: 150px;\n" +
                  "\theight: 1px;\n" +
                  "\tmargin-top: 0;\n" +
                  "\tmargin-bottom: 0;\n" +
                  "}\n" +
                  "\n" +
                  "p {\n" +
                  "\tmargin-top: 0;\n" +
                  "\tmargin-left: 2px;\n" +
                  "}\n" +
                  "\n" +
                  "h4 {\n" +
                  "\tmargin-top: 0;\n" +
                  "\tmargin-bottom: 0;\n" +
                  "}\n" +
                  "\n" +
                  ".day {\n" +
                  "\tdisplay: flex;\n" +
                  "\tflex-wrap: wrap;\n" +
                  "}\n" +
                  "\n" +
                  ".bloque {\n" +
                  "\tmargin-left: 20px;\n" +
                  "\tmargin-bottom: 15px;\n" +
                  "\theight: 50px;\n" +
                  "}\n" +
                  ".heure {\n" +
                  "\tfloat: left;\n" +
                  "\twhite-space: pre;\n" +
                  "}\n" +
                  ".salle {\n" +
                  "\tmargin-top: 10px;\n" +
                  "\tmargin-left: 3px;\n" +
                  "\tpadding-top: 2px;\n" +
                  "\twidth: 75px;\n" +
                  "\theight: 36px;\n" +
                  "\tborder: solid 1px #6B0F1A;\n" +
                  "\ttext-align: center;\n" +
                  "\tfloat: left;\n" +
                  "}\n" +
                  "::-webkit-scrollbar {\n" +
                  "\twidth: 10px;" +
                  "}\n" +
                  "/* Track */\n" +
                  "::-webkit-scrollbar-track {\n" +
                  "box-shadow: inset 0 0 5px #6B0F1A;\n" +
                  "\tborder-radius: 10px;\n" +
                  "}\n" +
                  "/* Handle */\n" +
                  "::-webkit-scrollbar-thumb {\n" +
                  "\tbackground: #282a3a;\n" +
                  "\tborder-radius: 10px;\n" +
                  "}\n";

        lastDate = "";

        try {
            // Création du fichier HTML
            pwA = new PrintWriter("page3.html");

            pwA.write(codeTop);

            for (Jury jury : juries) {
                // Date
                if ( !lastDate.equals(jury.getDate()) )
                {
                    if ( !lastDate.equals("") ) { pwA.write(codeMiddleSepB); }
                    lastDate = jury.getDate();
                    pwA.write(String.format(codeMiddleSepA, lastDate));
                }

                // Nom Jury, Prof 1, Prof 2, Prof 3, Prof 4, ...
                profs = "";

                for (String prof : jury.getProfs()) {
                    profs += prof + "<br>";
                }

                if (jury.getProfs().length < 4)
                {
                    for (int cpt = 0; cpt < 4 - jury.getProfs().length; cpt++) {
                        profs += "<br>";
                    }
                }

                pwA.write(String.format(codeMiddleA, jury.getNumJury(), profs));

                // Heure Début, Heure Fin, Groupe, Salle
                for (int numEquipe = 0; numEquipe < jury.getEquipe().length; numEquipe++) {
                    horaires = jury.getHoraire(numEquipe).split("\tà\t");

                    if (horaires[0].length() < 5) { horaires[0] = " " + horaires[0]; }
                    if (horaires[1].length() < 5) { horaires[1] = " " + horaires[1]; }

                    pwA.write(String.format(codeMiddleB, horaires[0], horaires[1], jury.getEquipe()[numEquipe].getNum(), jury.getNumSalle()));
                }

                pwA.write(codeMiddleC);

                // Ajoute les balise de fin du dernnier jury
                if (jury == juries[juries.length - 1]) { pwA.write("\t\t</div>\n"); }
            }

            pwA.write(codeBottom);
            pwA.close();

            // Création du fichier CSS
            pwB = new PrintWriter("page3.css");

            pwB.write(codeCss);
            pwB.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

/*-----------------------------------------------------------------------------------------------------------------------*/

    /*Sous Algo pour générer la page d'acceuil html/css*/

    public static void genererHtmlAcceuil(boolean modeSombre)
    {
        /*-------------*/
        /*---Données---*/
        /*-------------*/

        //Variables
        String codeHtml;
        String codeCssClair;
        String codeCssSombre;

        String imgSombre;
        String imgClair;

        PrintWriter pwAClair; // Fichier HTML
        PrintWriter pwASombre;// Fichier HTML
        PrintWriter pwBClair; // Fichier CSS
        PrintWriter pwBSombre;// Fichier CSS

        /*----------------*/
        /*--Instructions--*/
        /*----------------*/
        
        codeHtml =  "<!DOCTYPE html>\n" +
                    "<html lang=\"fr\">\n" +
                    "\n" +
                    "\t<head>\n" +
                    "\t\t<meta charset=\"utf-8\">\n" +
                    "\t\t<link rel=\"stylesheet\" href=\"%s.css\" media=\"all\" type=\"text/css\">\n" +
                    "\n" +
                    "\t\t<title> EasyPlanning </title>\n" +
                    "\t</head>\n" +
                    "\n" +
                    "\t<body>\n" +
                    "\t\t<header>\n" +
                    "\t\t\t\t<img src=\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/logo.png \" class=\"logo\" alt=\"logo\">\n" +
                    "\t\t\t\t<span class=\"title\">EASY PLANNING</span> \n" +
                    "\t\t</header>\n" +
                    "\n" +
                    "\t\t<aside class=\"aleft\">\n" +
                    "\t\t\t<div id=\"conteneur-menu2\">\n" +
                    "\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t<li><a href=\"page1.html\" target=\"FramePage\">Liste des Étudiants</a></li>\n" +
                    "\t\t\t\t\t<li><a href=\"page2.html\" target=\"FramePage\">Liste des Groupes</a></li>\n" +
                    "\t\t\t\t\t<li><a href=\"page3.html\" target=\"FramePage\">Planning</a></li>\n" +
                    "\t\t\t\t</ul>\n" +
                    "\t\t\t</div>\n" +
                    "\n" +
                    "\t\t\t<div class=\"aleftbot\">\n" +
                    "\t\t\t\t<a href=\"%s.html\"><img class=\"bouton\" src=\"%s\" alt=\"bouton sombre\"></a>\n" +
                    "\t\t\t\t<p class=\"bouton\">Mode Sombre</p>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t</aside>\n" +
                    "\n" +
                    "\t\t<aside class=\"aright\">\n" +
                    "\t\t\tMarrouche Mohamad<br>\n" +
                    "\t\t\tClément Daubeuf<br>\n" +
                    "\t\t\tMathys Poret<br>\n" +
                    "\t\t\tMartin Queval\n" +
                    "\t\t</aside>\n" +
                    "\n" +
                    "\t\t<div class=\"center\">\n" +
                    "\t\t\t<iframe src=\"page1.html\" name=\"FramePage\"></iframe>\n" +
                    "\t\t</div>\n" +
                    "\t</body>\n" +
                    "</html>\n";

        codeCssClair = "@font-face {\n" +
                       "\tfont-family: \"Iceland\";\n" +
                       "\tsrc : url(\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/Iceland/Iceland-Regular.ttf\");\n" +
                       "}\n" +
                       "\n" +
                       "body {\n" +
                       "\tbackground-color : #1b5177;\n" +
                       "\tfont-family : Iceland;\n" +
                       "}\n" +
                       "\n" +
                       ".logo {\n" +
                       "\tfloat : left;\n" +
                       "\twidth: 175px;\n" +
                       "\theight : 100px;\n" +
                       "}\n" +
                       "\n" +
                       ".title {\n" +
                       "\theight : 95px;\n" +
                       "\twidth : 50%;\n" +
                       "\tborder : #1b5177 solid 3.5px;\n" +
                       "\tborder-radius : 7px;\n" +
                       "\tmargin-right : 25%;\n" +
                       "\tpadding-top : 10px;\n" +
                       "\tfloat : right;\n" +
                       "}\n" +
                       "\n" +
                       "header {\n" +
                       "\tfont-size : 450%;\n" +
                       "\theight : 130px;\n" +
                       "\tcolor : #1b5177;\n" +
                       "\tpadding-top : 2%;\n" +
                       "\ttext-align : center;\n" +
                       "\tborder : #1b5177 solid 2px;\n" +
                       "\tborder-radius : 5px;\n" +
                       "\tbackground-color : bisque;\n" +
                       "}\n" +
                       "\n" +
                       "div {\n" +
                       "\tmargin-left: 17%;\n" +
                       "\twidth: 66%;\n" +
                       "\tbackground-color : bisque;\n" +
                       "\tcolor : #6B0F1A;\n" +
                       "}\n" +
                       "\n" +
                       "div.center {\n" +
                       "\tborder : #1b5177 solid 5px;\n" +
                       "\tborder-radius : 20px;\n" +
                       "\theight: 550px;\n" +
                       "}\n" +
                       "\n" +
                       "aside {\n" +
                       "\tbackground-color : bisque;\n" +
                       "\tborder : #1b5177 solid 5px;\n" +
                       "\tborder-radius : 10px;\n" +
                       "\tcolor : #1b5177;\n" +
                       "\theight: 550px;\n" +
                       "}\n" +
                       "\n" +
                       ".aleft {\n" +
                       "\tfloat : left;\n" +
                       "\twidth : 15%;\n" +
                       "}\n" +
                       "\n" +
                       ".aleftbot {\n" +
                       "\tmargin-top : 260px;\n" +
                       "\tfloat : left;\n" +
                       "}\n" +
                       "\n" +
                       ".bouton {\n" +
                       "\tfloat : left;\n" +
                       "}\n" +
                       "\n" +
                       ".aright {\n" +
                       "\tfloat : right;\n" +
                       "\twidth : 15%;\n" +
                       "}\n" +
                       "\n" +
                       "#conteneur-menu2 {\n" +
                       "\theight : 150px;\n" +
                       "\twidth : 11.5%;\n" +
                       "\tpadding-top : 20px;\n" +
                       "\tmargin : 5px 0 0 0;\n" +
                       "}\n" +
                       "\n" +
                       "#conteneur-menu2 ul {\n" +
                       "\tpadding : 0;\n" +
                       "\tmargin : 0;\n" +
                       "\tfloat : left;\n" +
                       "}\n" +
                       "\n" +
                       "#conteneur-menu2 ul li {\n" +
                       "\tlist-style : none;\n" +
                       "\tpadding : 0;\n" +
                       "}\n" +
                       "\n" +
                       "#conteneur-menu2 ul li a {\n" +
                       "\ttext-decoration : none;\n" +
                       "\tfont-size : 20px;\n" +
                       "\tpadding : 4px;\n" +
                       "\tdisplay : block;\n" +
                       "\tcolor : #1b5177;\n" +
                       "\tbackground : transparent;\n" +
                       "\twidth : 400px;\n" +
                       "\ttransition : all 0.3s;\n" +
                       "\tline-height : 20px;\n" +
                       "}\n" +
                       "\n" +
                       "#conteneur-menu2 ul li a:hover {\n" +
                       "\tbackground : #1b5177;\n" +
                       "\tcolor : #FAEAB1;\n" +
                       "\tpadding-left : 20px;\n" +
                       "\twidth : 200px;\n" +
                       "\ttransition : all 0.3s;\n" +
                       "}\n" +
                       "\n" +
                       "iframe {\n" +
                       "\tposition : absolute;\n" +
                       "\twidth: 63.50%;\n" +
                       "\tmargin-left: 0.75%;\n" +
                       "\tmargin-top: 0.75%;\n" +
                       "\theight: 512px;\n" +
                       "\tborder-radius : 5px;\n" +
                       "\tborder : #1b5177 solid 5px;\n" +
                       "}\n" +
                       "\n" +
                       "::-webkit-scrollbar {\n" +
                       "\twidth : 10px;\n" +
                       "}\n" +
                       "\n" +
                       "::-webkit-scrollbar-track {\n" +
                       "\tbox-shadow : 0 0 5px #1b5177 inset;\n" +
                       "\tborder-radius : 10px;\n" +
                       "}\n" +
                       "\n" +
                       "::-webkit-scrollbar-thumb {\n" +
                       "\tbackground : #1b5177;\n" +
                       "\tborder-radius : 10px;\n" +
                       "}\n";

        codeCssSombre = "@font-face {\n" +
                        "\tfont-family: \"Iceland\";\n" +
                        "\tsrc : url(\"https://raw.githubusercontent.com/MartinQueval/SAE.01/main/Iceland/Iceland-Regular.ttf\");\n" +
                        "}\n" +
                        "\n" +
                        "body {\n" +
                        "\tbackground-color : black;\n" +
                        "\tfont-family : Iceland;\n" +
                        "}\n" +
                        "\n" +
                        ".logo {\n" +
                        "\tfloat : left;\n" +
                        "\twidth: 175px;\n" +
                        "\theight : 100px;\n" +
                        "}\n" +
                        "\n" +
                        ".title {\n" +
                        "\theight : 95px;\n" +
                        "\twidth : 50%;\n" +
                        "\tborder : black solid 3.5px;\n" +
                        "\tborder-radius : 7px;\n" +
                        "\tmargin-right : 25%;\n" +
                        "\tpadding-top : 10px;\n" +
                        "\tfloat : right;\n" +
                        "}\n" +
                        "\n" +
                        "header {\n" +
                        "\tfont-size : 450%;\n" +
                        "\theight : 130px;\n" +
                        "\tcolor : #c69749;\n" +
                        "\tpadding-top : 2%;\n" +
                        "\ttext-align : center;\n" +
                        "\tborder : #282a3a solid 2px;\n" +
                        "\tborder-radius : 5px;\n" +
                        "\tbackground-color : #282a3a;\n" +
                        "}\n" +
                        "\n" +
                        "div {\n" +
                        "\tmargin-left: 17%;\n" +
                        "\twidth: 66%;\n" +
                        "\tbackground-color : #282a3a;\n" +
                        "\tcolor : #c69749;\n" +
                        "}\n" +
                        "\n" +
                        "div.center {\n" +
                        "\tborder : black solid 5px;\n" +
                        "\tborder-radius : 20px;\n" +
                        "\theight: 550px;\n" +
                        "}\n" +
                        "\n" +
                        "aside {\n" +
                        "\tbackground-color : #282a3a;\n" +
                        "\tborder : black solid 5px;\n" +
                        "\tborder-radius : 10px;\n" +
                        "\tcolor : #c69749;\n" +
                        "\theight: 550px;\n" +
                        "}\n" +
                        "\n" +
                        ".aleft {\n" +
                        "\tfloat : left;\n" +
                        "\twidth : 15%;\n" +
                        "}\n" +
                        "\n" +
                        ".aleftbot {\n" +
                        "\tmargin-top : 260px;\n" +
                        "\tfloat : left;\n" +
                        "}\n" +
                        "\n" +
                        ".bouton {\n" +
                        "\tfloat : left;\n" +
                        "}\n" +
                        "\n" +
                        ".aright {\n" +
                        "\tfloat : right;\n" +
                        "\twidth: 15%;\n" +
                        "}\n" +
                        "\n" +
                        "#conteneur-menu2 {\n" +
                        "\theight : 150px;\n" +
                        "\twidth : 11.5%;\n" +
                        "\tpadding-top : 20px;\n" +
                        "\tmargin : 5px 0 0 0;\n" +
                        "}\n" +
                        "\n" +
                        "#conteneur-menu2 ul {\n" +
                        "\tpadding : 0;\n" +
                        "\tmargin : 0;\n" +
                        "\tfloat : left;\n" +
                        "}\n" +
                        "\n" +
                        "#conteneur-menu2 ul li {\n" +
                        "\tlist-style : none;\n" +
                        "\tpadding : 0;\n" +
                        "}\n" +
                        "\n" +
                        "#conteneur-menu2 ul li a {\n" +
                        "\ttext-decoration : none;\n" +
                        "\tfont-size : 20px;\n" +
                        "\tpadding : 4px;\n" +
                        "\tdisplay : block;\n" +
                        "\tcolor : #c69749;\n" +
                        "\tbackground : transparent;\n" +
                        "\twidth : 400px;\n" +
                        "\ttransition : all 0.3s;\n" +
                        "\tline-height : 20px;\n" +
                        "}\n" +
                        "\n" +
                        "#conteneur-menu2 ul li a:hover {\n" +
                        "\tbackground : #c58940;\n" +
                        "\tcolor : #282a3a;\n" +
                        "\tpadding-left : 20px;\n" +
                        "\twidth : 200px;\n" +
                        "\ttransition : all 0.3s;\n" +
                        "}\n" +
                        "\n" +
                        "iframe {\n" +
                        "\tposition : absolute;\n" +
                        "\twidth: 63.50%;\n" +
                        "\tmargin-left: 0.75%;\n" +
                        "\tmargin-top: 0.75%;\n" +
                        "\theight: 512px;\n" +
                        "\tbackground : #c58940;\n" +
                        "\tborder-radius : 5px;\n" +
                        "\tborder : black solid 5px;\n" +
                        "}\n" +
                        "\n" +
                        "::-webkit-scrollbar {\n" +
                        "\twidth : 10px;\n" +
                        "}\n" +
                        "\n" +
                        "::-webkit-scrollbar-track {\n" +
                        "\tbox-shadow : 0 0 5px #c58940 inset;\n" +
                        "\tborder-radius : 10px;\n" +
                        "}\n" +
                        "\n" +
                        "::-webkit-scrollbar-thumb {\n" +
                        "\tbackground : #c58940;\n" +
                        "\tborder-radius : 10px;\n" +
                        "}\n";

        imgSombre = "https://raw.githubusercontent.com/MartinQueval/SAE.01/main/sombre.png";
        imgClair  = "https://raw.githubusercontent.com/MartinQueval/SAE.01/main/clair.png";

        try {
            // Création du fichier HTML Clair
            pwAClair = new PrintWriter("acceuilClair.html");

            pwAClair.write(String.format(codeHtml, "acceuilClair", "acceuilSombre", imgClair));
            pwAClair.close();

            // Création du fichier HTML Sombre
            pwASombre = new PrintWriter("acceuilSombre.html");


            pwASombre.write(String.format(codeHtml, "acceuilSombre", "acceuilClair", imgSombre));
            pwASombre.close();

            // Création du fichier CSS Clair
            pwBClair = new PrintWriter("acceuilClair.css");

            pwBClair.write(codeCssClair);
            pwBClair.close();

            // Création du fichier CSS Sombre
            pwBSombre = new PrintWriter("acceuilSombre.css");

            pwBSombre.write(codeCssSombre);
            pwBSombre.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        // Ouvrir le page d'acceuil
        try {
            Desktop.getDesktop().open(new File(modeSombre ? "acceuilSombre.html" : "acceuilClair.html"));
        } catch (Exception e) {}
    }
}