package noeuds;

import java.util.ArrayList;
import java.util.HashMap;

public class NoeudTerminal extends Noeud{
	
	//varaible contenant le nombre de noeuds terminaux crées
	private static int nbNoeudTerminaux=0;
	
	//Hashmap contenant l'ensemble des documents (id) contenant le terme avec l'index des positions
	
	private HashMap<Integer, ArrayList<Integer>> indexPositions;
	//private String terme;
	private int frequenceCorpus;
	
	public NoeudTerminal(Noeud noeudPere) {
		super(noeudPere);
		this.nbNoeudTerminaux++;
		//this.terme = this.recupererTerme();
		this.frequenceCorpus=0;
		indexPositions=new HashMap<Integer, ArrayList<Integer>>();
	}
	public HashMap<Integer, ArrayList<Integer>> getIndexPositions() {
		return indexPositions;
	}
	public void setIndexPositions(HashMap<Integer, ArrayList<Integer>>indexPositions) {
		this.indexPositions = indexPositions;
	}
	
	// récupère le terme du noeud terminal en remontant dans l'arbre
	public String recupererTerme(){
		Noeud noeud = this.getNoeudPere();
		String terme = "";
		terme = noeud.getLettre() + terme;
		while(noeud.getNoeudPere()!=null){
			noeud = noeud.getNoeudPere();
			terme = noeud.getLettre() + terme;
		}
		return terme;
	}
	
	public String getTerme() {
		return this.recupererTerme();
	}
	
	// calcule l'index des positions
	public HashMap<String, ArrayList<Integer>> calculIndexPositions(){
		HashMap<String, ArrayList<Integer>> indexPositions = new HashMap<String, ArrayList<Integer>>();
		//TODO
		return indexPositions;
	}
	
	@Override
	public char getLettre() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getFrequenceCorpus() {
		return frequenceCorpus;
	}
	
	public void setFrequenceCorpus(int frequenceCorpus) {
		this.frequenceCorpus = frequenceCorpus;
	}

	public static int getNbNoeudTerminaux() {
		return nbNoeudTerminaux;
	}
	
	public static void setNbNoeudTerminaux(int nbNoeudTerminaux) {
		NoeudTerminal.nbNoeudTerminaux = nbNoeudTerminaux;
	}
	
}
