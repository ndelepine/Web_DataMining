package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import noeuds.Noeud;
import noeuds.NoeudNonTerminal;
import noeuds.NoeudTerminal;

import com.google.common.collect.HashBiMap;

public class Index {
	//liste des noeus initiaux (correspondant à la première lettre de chaque terme présent dans l'index)
	private static ArrayList<Noeud> debutTerme;
	//liste contenant la taille de chaque fichier
	private static HashMap<Integer, Integer> sizeFile=new HashMap<Integer, Integer>();

	//set contenant l'ensemble des fichers ayant servis à construire l'index
	private static Set<Integer> fichierParcourus=new HashSet<Integer>();

	public Index(String path) throws IOException {
		TermTooFrequent frequent=new TermTooFrequent(path, 0.2f);

		debutTerme=this.InitialiserIndex(path, frequent);

		/*for(int i=0; i<frequent.getFrequentTerm().size();i++){
			this.deleteTerm(frequent.getFrequentTerm().get(i));
		}*/

		//on supprime les termes trop fréquents de l'index
		/**
		 * Possibilité d'ajouter des termes directement ici : déterminants, ...
		 */
	}

	@Override
	public String toString() {
		return "debut terme :"+debutTerme;
	}


	/**
	 * Function qui associe un id à un nom de fichier
	 * @param folder
	 * @return
	 */
	public static HashBiMap<String, Integer> identifiantFichier(File folder) {
		// Va lister tous les fichiers dans le répertoire et leur attribue un ID
		HashBiMap<String, Integer> id = HashBiMap.create(folder.list().length);
		int i = 1;
		for (final File entry : folder.listFiles()) {
			if (entry.isDirectory()==false) {
				id.put(entry.getName(), entry.getName().hashCode() );
				i++;
			}
		}
		return id;
	}

	/**
	 * Pour initialiser l'arbre d'index
	 * @throws IOException
	 */
	public static ArrayList<Noeud> InitialiserIndex(String path, TermTooFrequent frequent) throws IOException {
		File file = new File(path);
		File[] filesInDir = file.listFiles();
		ArrayList<Noeud> result=new ArrayList<Noeud>();
		HashBiMap<String, Integer> id = identifiantFichier(file);


		//liste temporaire des fichiers dans le dossier


		for (final File f : filesInDir) 
		{
			//on regarde si le fichier à déja été parcourus
			if(!(fichierParcourus.contains(f.getName().hashCode()))){
				//si il à déja été parcourus, on met à jour l'index
				fichierParcourus.add(f.getName().hashCode());
				result=addFileToIndex(f, result, id, frequent);
			}
		}
		
		/**
		 * UpdateOnDelete pour updater aprés suppression
		 */

		//updateOnDelete(file, result);



		return result;
	}
	
	/**
	 * function to launch to update after deleting some file in the directory
	 * @throws IOException 
	 * 
	 */
	public static void updateOnDelete(File file, ArrayList<Noeud> result) throws IOException{
		File[] filesInDir = file.listFiles();
		//on regarde si des fichiers ont été enlevés
				Set<Integer> fileToDelete=new HashSet<Integer>();
				for (Iterator<Integer> it = fichierParcourus.iterator(); it.hasNext(); ) {
					Integer f = it.next();
					boolean trouve=false;
					for(int i=0;i<file.list().length;i++){
						if(fichierParcourus.contains(file.list()[i].hashCode())) {
							trouve=true;
						}
					}
					//si le fichier à été supprimé
					if(trouve==false){
						//on enléve de l'index les fichiers supprimés du répertoire
						fileToDelete.add(f);
						fichierParcourus.remove(it);

						for (final File f2 : filesInDir) 
						{
							//on regarde si le fichier à déja été parcourus
							if(fileToDelete.contains(f2.getName().hashCode())){
								//si il à déja été parcourus, on met à jour l'index
								result=deleteFileToIndex(f2, result);
							}
						}
					}
				}
	}

	/**
	 * function to update index when some file are deleted from the directory
	 * @param f
	 * @throws IOException 
	 */
	public static ArrayList<Noeud> deleteFileToIndex(File f, ArrayList<Noeud> result) throws IOException{

		if (f.isDirectory()==false) 
		{
			BufferedReader entree = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String ligne;
			StringTokenizer st;
			String mot;
			while ((ligne = entree.readLine()) != null)
			{
				//On récupére le mot lématiser
				String text[]= ligne.split("\t");
				mot=text[2];

				//on le normalise à nouveau (étape inutile si la lématisation à été faite correctement)
				mot=normalize(mot);	
				getNoeudTerminal(mot).setFrequenceCorpus(getNoeudTerminal(mot).getFrequenceCorpus()-1);
				if(getNoeudTerminal(mot).getFrequenceCorpus()==0){
					deleteTerm(mot);
					NoeudTerminal.setNbNoeudTerminaux(NoeudTerminal.getNbNoeudTerminaux()-1);
					Noeud.setNbNoeud(Noeud.getNbNoeud()-1);
					}
				else{
					getNoeudTerminal(mot).getIndexPositions().remove(f.getName().hashCode());
				}
			}
		}
		return result;
	}

	/**
	 * Fonction pour ajouter un fichier à l'index
	 * @param f
	 * @param result
	 * @param id
	 * @param frequent
	 * @throws IOException
	 */
	public static ArrayList<Noeud> addFileToIndex(File f, ArrayList<Noeud> result, HashBiMap<String, Integer> id, TermTooFrequent frequent) throws IOException{


		char[] decomposition;
		int trouve;
		int j;
		int positionLigne;
		ArrayList<Integer> tempPosition;
		HashMap<Integer, ArrayList<Integer>> hashmapTemp;
		Noeud temp;

		positionLigne=0;

		if (f.isDirectory()==false) 
		{
			BufferedReader entree = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String ligne;
			StringTokenizer st;
			String mot;
			int nbOcc;
			//pour chaque ligne dans lématisation
			while ((ligne = entree.readLine()) != null)
			{
				positionLigne++;
				//On récupére le mot lématiser
				String text[]= ligne.split("\t");
				mot=text[2];

				//on le normalise à nouveau (étape inutile si la lématisation à été faite correctement)
				mot=normalize(mot);		

				if(!frequent.getFrequentTerm().contains(mot)){
					//on décompose le mot
					decomposition=mot.toCharArray();

					//on regarde si le noeud correspondant à la première lettre du mot existe déja
					if(decomposition.length>0){

						trouve=-1;
						for(int i=0;i<result.size();i++){
							if(result.get(i).getLettre()==decomposition[0]){
								trouve=i;
							}
						}
						//Si il n'existe pas on le crée
						if(trouve ==-1){
							result.add(new NoeudNonTerminal(decomposition[0], null));
							trouve=result.size()-1;
						}
						j=1;
						temp=result.get(trouve);
						//jusqu'à la fin du mot
						while(j<decomposition.length){

							trouve=-1;
							if(temp.getNoeudsFils()!=null){
								for(int i=0;i<temp.getNoeudsFils().size();i++){
									if(temp.getNoeudsFils().get(i).getLettre()==decomposition[j]){
										trouve=i;
									}
								}
							}
							//Si il n'existe pas on le crée
							if(trouve ==-1){
								temp.getNoeudsFils().add(new NoeudNonTerminal(decomposition[j], temp));
								trouve=temp.getNoeudsFils().size()-1;
							}
							temp=temp.getNoeudsFils().get(trouve);
							j++;
						}

						//On crée le noeud terminal
						trouve=-1;
						for(int i=0; i<temp.getNoeudsFils().size();i++){
							if(temp.getNoeudsFils().get(i) instanceof NoeudTerminal){
								trouve=i;
							}
						}

						if(trouve==-1){
							temp.getNoeudsFils().add(new NoeudTerminal(temp));
							trouve=temp.getNoeudsFils().size()-1;
							temp=temp.getNoeudsFils().get(trouve);

						}
						else{
							temp=temp.getNoeudsFils().get(trouve);
						}
						//On modifie les éléemnts ud noeud terminal
						((NoeudTerminal)temp).setFrequenceCorpus((((NoeudTerminal)temp).getFrequenceCorpus())+1);					
						if(((NoeudTerminal)temp).getIndexPositions().containsKey(id.get(f.getName()))){
							((NoeudTerminal)temp).getIndexPositions().get(id.get(f.getName())).add(Integer.valueOf(positionLigne));
						}
						else{
							tempPosition=new ArrayList<Integer>();
							tempPosition.add(positionLigne);
							((NoeudTerminal)temp).getIndexPositions().put(id.get(f.getName()),tempPosition);
						}
					}
				}


			}
		}
		sizeFile.put(id.get(f.getName()), positionLigne);
		return result;
	}



	/**
	 * Pour supprimer un terme dans l'index
	 * @param mot
	 */
	public static void deleteTerm(String mot){
		//1 - on se place dans le noeud terminal correspondant au terme
		Noeud temp=getNoeudTerminal(mot);
		char[] arrayChar=mot.toCharArray();

		//2 - on supprime tous les noeuds jusqu'a ce qu'un noeud ait plusieurs Noeuds fils
		if(temp!=null){
			int g=0;
			if(temp.getNoeudPere().getNoeudPere()!=null){
				Noeud temp2=temp.getNoeudPere();
				Noeud temp3=temp;
				while((temp2.getNoeudsFils().size()<2)&&(g<arrayChar.length)){
					temp2.getNoeudsFils().remove(temp3);
					temp3=temp2;
					temp2=temp2.getNoeudPere();
					g++;
				}
				temp2.getNoeudsFils().remove(temp3);


				for(int i=0;i<temp2.getNoeudsFils().size();i++){
					if(temp2.getNoeudsFils().get(i) instanceof NoeudTerminal){
						temp2.getNoeudsFils().remove(i);
					}
				}


			}
			else{
				debutTerme.remove(debutTerme.indexOf(temp.getNoeudPere()));
			}
		}

	}



	/**
	 * Function to normalize a string (delete punctuation and change in lower case)
	 * @param string
	 * @return
	 */
	public static String normalize(String string){
		string=string.toLowerCase();
		string = Normalizer.normalize(string, Normalizer.Form.NFD);
		string=string.replace(",",  "");
		string=string.replace(".",  "");
		string=string.replace(";",  "");
		return string;
	}

	public static ArrayList<Noeud> getDebutTerme() {
		return debutTerme;
	}

	public void setDebutTerme(ArrayList<Noeud> debutTerme) {
		this.debutTerme = debutTerme;
	}

	/**
	 * Function to return the terminal node for a particular string
	 * @param terme
	 * @return
	 */
	public static NoeudTerminal getNoeudTerminal(String terme){
		Noeud temp=null;

		//on cherche le noeud terminal correspondant au terme
		//1 - on se place dans le noeud terminal correspondant au terme
		char[] arrayChar=terme.toCharArray();
		int trouve=-1;
		//Si il s'agit d'une chaine vide on ne fait rien
		if(arrayChar.length<=0){
			return null;
		}
		for(int j=0;j<getDebutTerme().size();j++){
			if(arrayChar[0]==getDebutTerme().get(j).getLettre()){
				trouve=j;
			}
		}

		if(trouve !=-1){

			temp=getDebutTerme().get(trouve);
			for(int i=1;i<arrayChar.length;i++){
				trouve=-1;
				for(int k=0;k<temp.getNoeudsFils().size();k++){
					if(temp.getNoeudsFils().get(k).getLettre()==arrayChar[i]){
						trouve=k;
					}
				}
				if(trouve==-1){
					return null;
				}
				temp=temp.getNoeudsFils().get(trouve);
			}
			for(int k=0;k<temp.getNoeudsFils().size();k++){
				if(temp.getNoeudsFils().get(k) instanceof NoeudTerminal){
					trouve=k;
				}
			}
			try
			{
				temp=temp.getNoeudsFils().get(trouve);
			}
			catch(IndexOutOfBoundsException e){
				JOptionPane.showMessageDialog(null, "Le terme \""+terme +"\" n'est pas dans l'index", "Le terme \""+terme +"\" n'est pas dans l'index",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else{
			System.out.println("Le terme \""+terme +"\" n'est pas dans l'index");
		}
		try{
			return (NoeudTerminal)temp;
		}
		catch(ClassCastException e){
			JOptionPane.showMessageDialog(null, "Le terme \""+terme +"\" n'est pas dans l'index", "Le terme \""+terme +"\" n'est pas dans l'index",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;

	}


	public static HashMap<Integer, Integer> getSizeFile() {
		return sizeFile;
	}





}


