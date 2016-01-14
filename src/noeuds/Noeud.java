package noeuds;

import java.util.ArrayList;


public abstract class Noeud {
	private static int nbNoeud=0;
	private Noeud noeudPere;
	protected ArrayList<Noeud> noeudsFils;
	
	public Noeud(Noeud noeudPere) {
		super();
		nbNoeud++;
		this.noeudPere = noeudPere;
		this.noeudsFils=new ArrayList<Noeud>();
	}

	public Noeud getNoeudPere() {
		return noeudPere;
	}
	
	public abstract char getLettre();
	
	public ArrayList<Noeud> getNoeudsFils() {
		return noeudsFils;
	}
	
	public static int getNbNoeud() {
		return nbNoeud;
	}
	
	public static void setNbNoeud(int nbNoeud) {
		Noeud.nbNoeud = nbNoeud;
	}
	

}
