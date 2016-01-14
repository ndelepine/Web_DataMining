package search;

import index.Index;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import noeuds.NoeudTerminal;

import com.google.common.collect.HashBiMap;

public abstract class search {

	/**
	 * Retourne le tf-idf par documents et par terme recherché
	 * @param index
	 * @param terme
	 * @param path
	 * @return
	 */
	public static HashMap<Integer, ArrayList<Double>> searchTerm(Index index, String terme, String path){
		HashMap<Integer, ArrayList<Double>> idf=new HashMap<Integer, ArrayList<Double>>();
		ArrayList<Double> idf_value;

		//nombre total de documents
		int n=Index.identifiantFichier(new File(path)).size() ;

		//on récupére chacun des termes de la requête
		String text[]= terme.split(" ");
		//on y supprime les doublons
		text=removeDuplicate(text);
		
		
		NoeudTerminal temp;
		ArrayList<NoeudTerminal> list=new  ArrayList<NoeudTerminal>();
		//on récupére tous les noeuds terminaux liés à chacun des termes présent dans la requêtes
		for(int i=0;i<text.length;i++){
			temp=(NoeudTerminal)index.getNoeudTerminal(text[i]);
			if(temp != null){
				list.add(temp);
			}
			else{
				System.out.println("le terme "+text[i]+" n'est pas dans l'index");
			}
		}

		if(list.size()>0){
			Set<Integer> keys=new HashSet<Integer>();
			keys.addAll(list.get(0).getIndexPositions().keySet());
			for(int i=1;i<list.size();i++){
				keys.addAll(list.get(i).getIndexPositions().keySet());
				//keys.retainAll(list.get(i).getIndexPositions().keySet());
			}
			//pour chaque documents contenant tous les termes

			for (Iterator<Integer> it = keys.iterator(); it.hasNext(); ) {
				Integer f = it.next();
				idf_value=new ArrayList<Double>();
				//pour chaque termes de la requête
				for(int j=0;j<list.size();j++){
					if(list.get(j).getIndexPositions().get(f)==null){
						idf_value.add((double)0);
					}
					else{
						idf_value.add(((list.get(j).getIndexPositions().get(f).size())/Math.sqrt(Index.getSizeFile().get(f)))*Math.log10(n/list.get(j).getIndexPositions().size()));
					}
					//idf_value=idf_value+(list.get(j).getIndexPositions().get(f).size())*Math.log10(n/list.get(j).getIndexPositions().size());
				}
				idf.put(f, idf_value);
			}
		}
		return idf;
	}
	
	public static HashMap<Integer, Double> searchTermNear(Index index, String terme, String path, int position){
		HashMap<Integer, Double> idf=new HashMap<Integer, Double>();
		double idf_value;
		//nombre total de documents
		int n=Index.identifiantFichier(new File(path)).size() ;
		String text[]= terme.split(" ");
		NoeudTerminal temp;
		ArrayList<NoeudTerminal> list=new  ArrayList<NoeudTerminal>();
		//on récupére tous les noeuds terminaux liés à chacun des termes présent dans la requêtes
		for(int i=0;i<text.length;i++){
			temp=(NoeudTerminal)index.getNoeudTerminal(text[i]);
			if(temp != null){
				list.add(temp);
			}
			else{
				System.out.println("le terme "+text[i]+" n'est pas dans l'index");
			}
		}

		if(list.size()>0){
			Set<Integer> keys=list.get(0).getIndexPositions().keySet();
			for(int i=1;i<list.size();i++){
				keys.retainAll(list.get(i).getIndexPositions().keySet());
			}
			//pour chaque documents contenant tous les termes
			
			// 1 - on ne garde que les documents ayant les termes rapprochés de X position
			int DIFMAX = 5;
			int difMax;
			int dif;
			// pour chaque document
			for (Iterator<Integer> it = keys.iterator(); it.hasNext(); ) {
				difMax=999999;
				Integer f = it.next();
				// on regarde dans tous les noeuds terminaux si il y au moins une différence inférieure à X
				for(int i=0;i<list.size();i++){
					// pour chaque occurence du mot on compare la différence avec les occurences des autres mots
					for(int j=0;j<list.get(i).getIndexPositions().get(f).size();j++){
						// on parcourt chaque position de chaque noeud terminal autre
						for(int k=0;k<list.size();k++){
							if(k!=i){
								for(int l=0;l<list.get(k).getIndexPositions().get(f).size();l++){
									dif=Math.abs(list.get(i).getIndexPositions().get(f).get(j)-list.get(k).getIndexPositions().get(f).get(l));
									if(dif<difMax){
										difMax=dif;
									}
								}
							}
						}
					}
				}
				
				// si la différence minimale trouvée est supérieure à X alors on retire le document
				if(difMax>DIFMAX){
					keys.remove(f);
				}
			}
			

			// 2 - On calcule les indices idf
			for (Iterator<Integer> it = keys.iterator(); it.hasNext(); ) {
				Integer f = it.next();
				idf_value=0;
				//pour chaque termes de la requête
				for(int j=0;j<list.size();j++){
					idf_value=idf_value+(list.get(j).getIndexPositions().get(f).size())*Math.log10(n/list.get(j).getIndexPositions().size());
				}
				idf.put(f, idf_value);
			}
		}
		return idf;
	}


	/**
	 * Calcule et renvoi le tf-idf moyen 
	 * @param searchTermResult
	 * @return
	 */
	public static ArrayList<Double> idfMoy(HashMap<Integer, ArrayList<Double>> searchTermResult)  {
		ArrayList<Double> result = new ArrayList<Double>();
		int k=1;
		Iterator it = searchTermResult.entrySet().iterator();
		try
		{
			HashMap.Entry<Integer, ArrayList<Double>> pair = (HashMap.Entry<Integer, ArrayList<Double>>)it.next();
			for(int i=0;i<pair.getValue().size() ;i++){
				result.add(pair.getValue().get(i));
			}
			while (it.hasNext()) {
				k++;
				pair = (HashMap.Entry)it.next();
				for(int i=0;i<pair.getValue().size() ;i++){
					result.set(i, (result.get(i)+pair.getValue().get(i)));
				}
			}
			for(int i=0; i<result.size();i++ ){
				result.set(i, (result.get(i)/k));
			}

		} catch (NoSuchElementException e)
		{
			JOptionPane.showMessageDialog(null, "Le terme recherché n'est pas dans l'index", "Le terme recherché n'est pas dans l'index",
					JOptionPane.ERROR_MESSAGE);
		}

		return result;
	}

	/**
	 * Function to return the idf value of the request
	 * @param index
	 * @param terme
	 * @param path
	 * @return
	 */
	public static ArrayList<Double> idfRequest(Index index, String terme, String path, HashMap<Integer, ArrayList<Double>> searchTermResult){
		ArrayList<Double> idf_value=new ArrayList<Double>();

		//nombre total de documents
		int n=Index.identifiantFichier(new File(path)).size() ;
		
		//on split la requête avec un tableau contenant chaque mot
		String text[]= terme.split(" ");
		//on récupére la taille de la requête
		int requestSize=text.length;
		
		//nombre d'occurence d'un mot dans la requete
		Map<String, Integer> nbOccurenceTerm=nbOccurenceRequest(text);
		//on supprime les doublons dans l'array
		text=removeDuplicate(text);
		System.out.println(nbOccurenceTerm);
		
		NoeudTerminal temp;
		ArrayList<NoeudTerminal> list=new  ArrayList<NoeudTerminal>();
		//on récupére tous les noeuds terminaux liés à chacun des termes présent dans la requêtes
		for(int i=0;i<text.length;i++){
			temp=(NoeudTerminal)index.getNoeudTerminal(text[i]);
			if(temp != null){
				list.add(temp);
			}
			else{
				System.out.println("le terme "+text[i]+" n'est pas dans l'index");
			}
		}
		

		for(int i=0;i<list.size();i++){
			idf_value.add(((nbOccurenceTerm.get(list.get(i).getTerme()))/Math.sqrt(requestSize))*Math.log10(n/list.get(i).getIndexPositions().size()));
			System.out.println(nbOccurenceTerm.get(list.get(i).getTerme()));
			System.out.println();
		}

	
	//idf_value=idf_value+(list.get(j).getIndexPositions().get(f).size())*Math.log10(n/list.get(j).getIndexPositions().size());

	return idf_value;
}
	
	/**
	 * Function to remove duplicate in request
	 * @param entree
	 * @return
	 */
	public static String[] removeDuplicate(String[] entree){
		ArrayList<String> temp=new ArrayList<String>(Arrays.asList((entree)));
		// add elements to al, including duplicates
		LinkedHashSet <String> hs = new LinkedHashSet<String>();
		hs.addAll(temp);
		temp.clear();
		temp.addAll(hs);
		String[] result = new String[temp.size()];
		result = temp.toArray(result);
		return result;
	}
	
	
	public static Map<String, Integer> nbOccurenceRequest(String[] text){
		Map<String, Integer> occurences = new HashMap<String, Integer>();

		for(String value : text)
		{
		    Integer oldValue = occurences.get(value);
		    if(oldValue == null)
		        occurences.put(value, 1);
		    else
		        occurences.put(value, oldValue + 1);
		}
		
		return occurences;
	}
	
	/**
	 * Pour comaprer les vecteurs des documents avec le vecteur de la requête
	 * @param testSearch => vecteur des tf-idf des documetns
	 * @param idfMoy => vecteur des tf-idf de la requête
	 * @return une HashMap<Valeur de la similarité, ID document>
	 */
	public static HashMap<Double, Integer> computeSimilarity(HashMap<Integer, ArrayList<Double>> testSearch, ArrayList<Double> idfMoy){
		double somme;
		double q;
		double n;
		//l'objet à retourner
		
		HashBiMap<Double, Integer> result=HashBiMap.create(testSearch.size());
		HashMap<Double, Integer> resultFinal=new HashMap<Double, Integer>();
		//ArrayList<Integer> nomFichier=new ArrayList<Integer>();
		
		// on parcours la HashMap
		Set cles = testSearch.keySet();
		Iterator<Integer> it = cles.iterator();
		while (it.hasNext()){
		   Integer cle = it.next(); 
		   ArrayList<Double> valeur = testSearch.get(cle); 
		   if(idfMoy.size()<=1){
			  // nomFichier.add(cle);
			   result.put(valeur.get(0), cle);
		   }
		   else {
			   // on compare le vecteur de chaque fichier avec le vecteur de la requête
			   somme=0;
			   q=0;
			   n=0;
			   for(int i=0;i<valeur.size();i++){
				   somme=somme+valeur.get(i)*idfMoy.get(i);
				   q=q+Math.pow(valeur.get(i), 2);
				   n=n+Math.pow(idfMoy.get(i), 2);
			   }
			   q=Math.sqrt(q);
			   n=Math.sqrt(n);
			   
			   result.put((somme/(q*n)), cle);
			   
		   }
		  
		}
		resultFinal.putAll(result);
		return resultFinal;
	}
	
	/**
	 * Pour afficher les résultats du plus similaire au moins similaire
	 */
	public static String displayOrderedFile(HashMap<Double, Integer> listFileValue, File path){
		HashBiMap<String, Integer> id=Index.identifiantFichier(path);
		String str="";
		SortedSet<Double> keys=new TreeSet<Double>(Collections.reverseOrder());
		keys = (new TreeSet<Double>(listFileValue.keySet())).descendingSet();
		for (Double key : keys) { 
		   Integer value = listFileValue.get(key);
		   str=str+"Fichier : "+id.inverse().get(value)+" ( id : "+value+" )"+" indice de similarité : "+key+"\n";
		}
		return str;
	}
	
	
}
