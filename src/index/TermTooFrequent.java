package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.HashBiMap;

public class TermTooFrequent {

	public ArrayList<String> frequentTerm;
	public int nbDocs;

	//Constructeur
	public TermTooFrequent(String path, float threshold){
		this.frequentTerm= new ArrayList<String>();
		//On calcule les mots trop fréquents
		try {
			tooFrequent(threshold,nbOccurence(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			graphThreshold(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Fonction qui calcule le nombre de documents dans lesquels chaque mot appartient
	public HashMap<String,Float> nbOccurence(String path) throws IOException{
		//HashMap qui contiendra le nombre de documents qui contiennent chaque mot
		HashMap<String,Float> occurences = new HashMap<String,Float>();

		//HashMap utilisée pour stocker chaque fichier qui contient le mot
		HashMap<String,ArrayList<Integer>> fichiers = new HashMap<String,ArrayList<Integer>>();

		//Liste des diférents fichiers
		ArrayList<Integer> idFichiers = null;


		File file = new File(path);
		HashBiMap<String, Integer> id = Index.identifiantFichier(file);
		File[] filesInDir = file.listFiles();

		//On récupère le nombre de documents
		int nbDocs = file.listFiles().length;
		this.nbDocs=nbDocs;

		for (File f : filesInDir) {
			if (f.isDirectory()==false) {

				BufferedReader entree = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String ligne;
				String mot;

				while ((ligne = entree.readLine()) != null)
				{
					String text[]= ligne.split("\t");
					mot=text[2];

					//Si les mots ne sont pas des stop words, on analyse leur fréquence
					if(!((text[1].substring(0, 3).equals("DET"))||(text[1].substring(0, 3).equals("PRP"))||(text[1].substring(0, 3).equals("PUN"))||(text[1].substring(0, 3).equals("PRO"))||(text[1].substring(0, 3).equals("SEN")))){
						mot=normalize(mot);

						//Si la liste des mots et des fichiers contenant ce mot contient déjà le mot, on rajoute le fichier à la liste des fichiers
						if (fichiers.containsKey(mot))
						{
							//Si le fichier n'est pas déjà dans la liste des fichiers, on le rajoute
							if(fichiers.get(mot).contains(id.get(f.getName()))==false ){
								fichiers.get(mot).add(id.get(f.getName()));
							}


						}
						//Sinon, on ajoute le mot ainsi que le fichier auquel il appartient
						else  {
							idFichiers=new ArrayList<Integer>();
							idFichiers.add(id.get(f.getName()));
							fichiers.put(mot, idFichiers);
						}
					}

					//Sinon, on les enlève d'office
					else{
						if(mot!=null && !mot.equals("")){
							this.frequentTerm.add(mot);
						}
					}
				}
				entree.close();
			}
		}

		//System.out.println("Il y a "+frequentTerm.size()+" mots trop fréquents");

		//En récupérant la taille de la liste des fichiers pour chaque mot, on obtient le nombre de documents auquel chaque mot appartient
		for(Entry<String, ArrayList<Integer>> entry : fichiers.entrySet()) {
			String cle = entry.getKey();
			ArrayList<Integer> valeur = entry.getValue();
			float pct = ((float) valeur.size())/((float) nbDocs);
			//On rajoute pour chaque mot le pourcentage de documents dans lequel ils apparaissent
			//System.out.println("Le mot "+cle+" apparait dans "+valeur.size()+" documents sur "+nbDocs+" soit dans "+pct+"% des documents.");
			occurences.put(cle, pct);
		}


		return occurences;
	}

	//Fonction qui determine les mots trop fréquents en fonction du threshold et de la liste des occurences
	public ArrayList<String> tooFrequent(float threshold,HashMap<String,Float> nbOccurence){

		//On stocke les mots enlevés grâce au threshold (utile nottamment pour le graphe)
		ArrayList<String> nbMotsTreshold = new ArrayList<String>();

		//On parcourt le pourcentage de documents auquel chaque mot appartient
		for(Entry<String,Float> entry : nbOccurence.entrySet()) {
			String cle = entry.getKey();
			float pct = entry.getValue();
			//Si le pourcentage dépasse le threshold, on rajoute le mot aux termes trop fréquents
			if(pct>threshold){
				this.frequentTerm.add(cle);
				nbMotsTreshold.add(cle);
			}
		}
		//System.out.println("Après les treshold, il y a "+frequentTerm.size()+" mots trop fréquents");
		return nbMotsTreshold;
	}

	//Fonction qui normalise les mots
	public static String normalize(String string){
		string=string.toLowerCase();
		string = Normalizer.normalize(string, Normalizer.Form.NFD);
		string=string.replace(",",  "");
		string=string.replace(".",  "");
		string=string.replace(";",  "");
		return string;
	}

	//Getter sur les mots fréquents
	public ArrayList<String> getFrequentTerm() {
		return frequentTerm;
	}

	//Fonction pour faire un graphique du nombre de mots enlevés à cause du treshold
	public void graphThreshold(String path) throws IOException{
		HashMap<String,Float> occurences = new HashMap<String,Float>();

		//On stockes les pct de chaque mot
		try {
			occurences = nbOccurence(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File ff=new File("Doc/graphique_threshold.txt"); // définir l'arborescence
		ff.createNewFile();
		FileWriter ffw=new FileWriter(ff);
		//On boucle et on regarde le nombre de mots supprimés pour chaque treshold
		for(float i=0.01f;i<=1;i+=0.01){			
			ffw.write((float) Math.round(i*10000)/10000.0+"\t"+tooFrequent(i, occurences).size());  // écrire une ligne dans le fichier resultat.txt
			ffw.write("\n"); // forcer le passage à la ligne
		}
		ffw.close(); // fermer le fichier à la fin des traitements
	}
}