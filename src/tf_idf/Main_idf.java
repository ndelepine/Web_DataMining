package tf_idf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

public class Main_idf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*int[] tableauFrequences;
		ArrayList<String> colNames=new ArrayList<String>();
		ArrayList<String> rowNames;
		Set mots;

		HashMap<String, Integer> result=new HashMap<String, Integer>();
		HashMap<String, Double> resultIDF=new HashMap<String, Double>();
		HashMap<String, Integer> nbIterationsEnsemble=new HashMap<String, Integer>();
		ArrayList<HashMap<String, Integer>> resultFinal=new ArrayList<HashMap<String, Integer>>();
		ArrayList<HashMap<String, Double>> resultFinalIDF=new ArrayList<HashMap<String, Double>>();

		HashMap<String, Integer> nbDocContenantTerme=nombreDocumentsContenantTerme(new File("Doc/lemmatisation"));


		nbIterationsEnsemble=resultFinal( new File("Doc/lemmatisation")); 
		//on récupére l'ensemble des mots présents dans le corpus
		mots=nbIterationsEnsemble.keySet();
		rowNames=new ArrayList<String>(Arrays.asList(Arrays.copyOf(mots.toArray(), mots.toArray().length, String[].class)));

		File folder=new File("Doc/lemmatisation");
		System.out.println("nb file :"+folder.list().length);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// listFilesForFolder(fileEntry);
			} else {
				PrintWriter writer=null;
				try {
					writer = new PrintWriter("Doc/resultIDF/result"+fileEntry.getName(), "UTF-8");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writer.println(fileEntry.getName());

				colNames.add(fileEntry.getName());
				result=countWordLematise(fileEntry, mots);
				resultFinal.add(result);
				resultIDF=convertFrequenceToIDF(result, nbDocContenantTerme);
				resultFinalIDF.add(resultIDF);



				String res;
				for (HashMap.Entry<String, Double> entry : resultIDF.entrySet()) {
					res= entry.getKey() +" "+ entry.getValue();
					writer.println(res);
				}
				writer.close();

			}
		}*/

		try {
			//On repére les termes qui apparaissent au moins 800 fois dans le corpus ou dans plus de 10 textes différents
			String path = "Doc/lemmatisation";
			Hashtable<String, HashMap<Integer, ArrayList<Integer>>> nbOccurence=nbOccurence(path);
			System.out.println(nbOccurence);
			//on affiche les id associé à chaque documents
			File file = new File(path);
			HashMap<String, Integer> id = identifiantFichier(file);
			System.out.println("ID : "+id);
			
			//on affiche les termes trop fréquent selon des seuils rentrés en paramétres
			double thresholdCorpus=800;
			double thresholdNbDoc=10;
			ArrayList<String> tooFrequentTerm=termTooFrequent(thresholdCorpus, thresholdNbDoc, nbOccurence);
			System.out.println("too frequent : "+tooFrequentTerm);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static HashMap<String, Integer> identifiantFichier(File folder) {
		// Va lister tous les fichiers dans le répertoire et leur attribue un ID
		HashMap<String, Integer> id = new HashMap<String, Integer>();
		int i = 1;
		for (final File entry : folder.listFiles()) {
			if (entry.isDirectory()==false) {
				id.put(entry.getName(), i );
				i++;
			}
		}
		return id;
	}
	
	/**
	 * <pour détecter les termes apparaissant trop fréquemment dans le corpus ou dans un nombre de textes trop important
	 * @throws IOException
	 */
	public static Hashtable<String, HashMap<Integer, ArrayList<Integer>>> nbOccurence(String path) throws IOException {
		//table contenant le nombre d'occurence par mot dans l'ensemble du corpus
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		//Table contenant l'ensemble des documents contenant au moins une occurence du mot
		HashMap<String, ArrayList<Integer>> talb = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> listId = null;
		//HashMap visant à associer chaque fichier à un ID
		Hashtable<String, Integer> idFile = new Hashtable<String, Integer>();
		//Hashmap fusion de table et talb pour le résultat final, le premier element de value est le nombre d'occurence dans le document
		Hashtable<String, HashMap<Integer, ArrayList<Integer>>> result = new Hashtable<String, HashMap<Integer, ArrayList<Integer>>>();

		
		ArrayList<String> termeFrequent=new ArrayList<String>();


		File file = new File(path);
		HashMap<String, Integer> id = identifiantFichier(file);
		File[] filesInDir = file.listFiles();

		for (final File f : filesInDir) {

			if (f.isDirectory()==false) {

				BufferedReader entree = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String ligne;
				StringTokenizer st;
				String mot;

				int nbOcc;

				while ((ligne = entree.readLine()) != null)
				{
					String text[]= ligne.split("\t");
					mot=text[2];
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
						table.put(mot, new Integer(nbOcc));
						//talb.put(mot, f.getName());

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
					//}
				}

				entree.close();
			}
			
		}
		
		List<Map.Entry<String, Integer>> table2=sortMapValues2(table);
		HashMap<Integer, ArrayList<Integer>> temp;
		//on renvoi une liste contenant les termes avec trop d'apparitions dans le corpus ou dans un nombre de doculent trop important
		for(int i=0;i<table2.size();i++){
			 String key = table2.get(i).getKey();
			 int value = table.get(key);
			 
			//System.out.println("Mot : " + key + " | Nombre d'occurences (corpus) : " + table.get(key) + " | Source : " + talb.get(key));
			temp= new HashMap<Integer, ArrayList<Integer>>();
			temp.put(table.get(key), talb.get(key));
			result.put(key, temp);
		}
		return result;
	}
	
	public static ArrayList<String> termTooFrequent(double thresholdCorpus, double thresholdNbDoc, Hashtable<String, HashMap<Integer, ArrayList<Integer>>> frequence){
		ArrayList<String> termeFrequent=new ArrayList<String>();
		
		Set keys=frequence.keySet();	
		String key;
		HashMap<Integer, ArrayList<Integer>> temp;
		
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
		    key=iter.next().toString();
		    temp= frequence.get(key);
		    if((temp.keySet().iterator().next()>= thresholdCorpus)||(temp.get(temp.keySet().iterator().next()).size()>=thresholdNbDoc)){
		    	termeFrequent.add(key);
		    }
		}
		
		return termeFrequent;
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


	public static HashMap<String, Integer> countWordLematise(File file, Set reference){
		HashMap<String, Integer> map=new HashMap();
		String text[];
		String word;
		//lecture du fichier texte	
		InputStream ips=null;
		try {
			ips = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		InputStreamReader ipsr=null;
		try {
			ipsr = new InputStreamReader(ips, "ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(ipsr);
		String line;
		try {
			//initialisation des mots
			String[] mots=Arrays.copyOf(reference.toArray(), reference.toArray().length, String[].class);
			for(int i=0;i<mots.length;i++ ){
				map.put(mots[i], 0);
			}

			while ((line = br.readLine())!=null)
			{
				text= line.split("\t");
				word=text[2];
				word=normalize(word);

				if (map.containsKey(word) ) {
					map.replace(word, map.get(word), (Integer)map.get(word)+1);
				} else {
					map.put(word, 1);
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 


		return map;
	}


	public static HashMap<String, Double> convertFrequenceToIDF(HashMap<String, Integer> matFrequence, HashMap<String, Integer> nbIterationsTotal){
		int NB_DOC=123;
		HashMap<String, Double> matIDF=new HashMap<String, Double>();
		Set mots=matFrequence.keySet();
		ArrayList<String> cles=new ArrayList<String>(Arrays.asList(Arrays.copyOf(mots.toArray(), mots.toArray().length, String[].class)));
		for(int i=0; i<cles.size();i++){
			matIDF.put(cles.get(i), matFrequence.get(cles.get(i))*Math.log10(NB_DOC/nbIterationsTotal.get(cles.get(i))));
		}
		return matIDF;
	}

	public static HashMap<String, Integer> nombreDocumentsContenantTerme(File folder){
		HashMap<String, Integer> result=new HashMap<String, Integer>();
		HashMap<String, Boolean> temp=new HashMap<String, Boolean>();
		for (final File fileEntry : folder.listFiles()) {
			temp=new HashMap<String, Boolean>();
			if (fileEntry.isDirectory()) {
				// listFilesForFolder(fileEntry);
			} else {
				String text[];
				String word;
				//lecture du fichier texte	
				InputStream ips=null;
				try {
					ips = new FileInputStream(fileEntry);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} 
				InputStreamReader ipsr=null;
				try {
					ipsr = new InputStreamReader(ips, "ISO-8859-1");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				BufferedReader br = new BufferedReader(ipsr);
				String line;
				try {
					while (((line = br.readLine())!=null) )
					{
						text= line.split("\t");
						word=text[2];
						word=normalize(word);
						if(temp.containsKey(word)==false){
							if (result.containsKey(word) ) {
								result.replace(word, result.get(word), (Integer)result.get(word)+1);
							} else {
								result.put(word, 1);
							}
						}
						temp.put(word, true);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					br.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
		}
		return result;
	}



}
