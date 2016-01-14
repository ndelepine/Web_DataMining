package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashBiMap;

import noeuds.Noeud;

public class termTooFrequent {

	private ArrayList<String> frequentTerm;
	private int nombreTotalMots;

	public termTooFrequent(String path, int thresholdCorpus, int thresholdNbDoc) throws IOException {
		frequentTerm=termTooFrequent(thresholdCorpus, thresholdNbDoc, nbOccurence(path),path);

	}

	public Hashtable<String, HashMap<Integer, ArrayList<Integer>>> nbOccurence(String path) throws IOException {
		//table contenant le nombre d'occurence par mot dans l'ensemble du corpus
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		//Table contenant l'ensemble des documents contenant au moins une occurence du mot
		HashMap<String, ArrayList<Integer>> talb = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> listId = null;
		//Hashmap fusion de table et talb pour le résultat final, le premier element de value est le nombre d'occurence dans le document
		Hashtable<String, HashMap<Integer, ArrayList<Integer>>> result = new Hashtable<String, HashMap<Integer, ArrayList<Integer>>>();


		int nbTotal=0;
		File file = new File(path);
		HashBiMap<String, Integer> id = Index.identifiantFichier(file);
		File[] filesInDir = file.listFiles();

		for (File f : filesInDir) {

			if (f.isDirectory()==false) {

				BufferedReader entree = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String ligne;
				String mot;

				int nbOcc;
				float nbLine=0;
				while ((ligne = entree.readLine()) != null)
				{
					String text[]= ligne.split("\t");
					mot=text[2];
					nbLine++;
					nbTotal++;

					if(!((text[1].substring(0, 3).equals("DET"))||(text[1].substring(0, 3).equals("PRP"))||(text[1].substring(0, 3).equals("PUN"))||(text[1].substring(0, 3).equals("PRO"))||(text[1].substring(0, 3).equals("SEN")))){

						/*st = new StringTokenizer(ligne, "\t");
						while(st.hasMoreTokens())
						{
							st.nextToken();
							st.nextToken();
							mot = st.nextToken();*/
						mot=normalize(mot);
						


						if (table.containsKey(mot))
						{
							nbOcc = table.get(mot).intValue();
							nbOcc++;
						}
						else  {
							nbOcc = 1;
						}
						table.put(mot, nbOcc);
						//talb.put(mot, f.getName());
						System.out.println(mot + " : "+table.get(mot));

						if(talb.containsKey(mot)){
							if(talb.get(mot).contains(id.get(f.getName()))==false ){
								talb.get(mot).add(id.get(f.getName()));
							}

						}
						else{
							listId=new ArrayList<Integer>();
							listId.add(id.get(f.getName()));
							talb.put(mot, listId);
						}
					}
					else{
						nbOcc=-1;
						table.put(mot, new Integer(nbOcc));
						listId=new ArrayList<Integer>();
						listId.add(id.get(f.getName()));
						talb.put(mot, listId);
					}



					//}
				}
				//On transforme les occurences en pourcentage en divisant par le nombre de mots
				
				Enumeration<String> clefs = table.keys();
				String key;
				while(clefs.hasMoreElements()) {
					key=clefs.nextElement();
					float value=  ((float) table.get(key))/nbLine;
					int replacement = (int)(value*100);
					table.put(key, new Integer(replacement));

				}
				
				
				


				entree.close();
			}

		}		
		List<Map.Entry<String, Integer>> table2=sortMapValues2(table);
		HashMap<Integer, ArrayList<Integer>> temp;
		//on renvoi une liste contenant les termes avec trop d'apparitions dans le corpus ou dans un nombre de documents trop important
		for(int i=0;i<table2.size();i++){
			String key = table2.get(i).getKey();
			temp= new HashMap<Integer, ArrayList<Integer>>();
			temp.put(table.get(key), talb.get(key));
			result.put(key, temp);
		}
		this.nombreTotalMots=nbTotal;
		System.out.println("Total : ");
		return result;
	}

	public static List<Map.Entry<String, Integer>> sortMapValues2(Map<String, Integer> map){
		//Sort Map.Entry by value
		List<Map.Entry<String, Integer>> result = new ArrayList(map.entrySet());
		Collections.sort(result, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}});

		return result;  
	}

	public static String normalize(String string){
		string=string.toLowerCase();
		string = Normalizer.normalize(string, Normalizer.Form.NFD);
		string=string.replace(",",  "");
		string=string.replace(".",  "");
		string=string.replace(";",  "");
		return string;
	}

	public static ArrayList<String> termTooFrequent(double thresholdCorpus, double thresholdNbDoc, Hashtable<String, HashMap<Integer, ArrayList<Integer>>> frequence,String path){
		ArrayList<String> termeFrequent=new ArrayList<String>();

		//On récupère le nombre de documents
		File file = new File(path);
		float nbDocs = file.listFiles().length;
		Set keys=frequence.keySet();	
		String key;
		HashMap<Integer, ArrayList<Integer>> temp;

		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			key=iter.next().toString();
			temp= frequence.get(key);
			float occurenceParDoc = (float) temp.get(temp.keySet().iterator().next()).size();
			int nbdoc = (int) ((occurenceParDoc/nbDocs)*100.0);
			if(temp.keySet().contains(-1)){
				termeFrequent.add(key);
			}		    
			else if((temp.keySet().iterator().next()>= thresholdCorpus)||(nbdoc>=thresholdNbDoc)){
				termeFrequent.add(key);
			}
		}
		return termeFrequent;
	}

	public ArrayList<String> frequentTerm(double thresholdCorpus, double thresholdNbDoc, Index index){
		ArrayList<String> result = new ArrayList<String>();
		Noeud temp;

		//on aprcourt tous les noeuds terminaux
		for(int i=0; i<index.getDebutTerme().size();i++){
			temp=index.getDebutTerme().get(i);
		}

		return result;
	}

	public ArrayList<String> getFrequentTerm() {
		return frequentTerm;
	}

}
