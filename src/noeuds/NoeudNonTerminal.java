package noeuds;

import java.util.ArrayList;

public class NoeudNonTerminal extends Noeud{
	private static int nbNoeudNonTerminaux=0;
	private char lettre;
	
	public NoeudNonTerminal(char lettre, Noeud noeudPere) {
		super(noeudPere);
		nbNoeudNonTerminaux++;
		this.lettre = lettre;
	}


	public char getLettre() {
		return lettre;
	}
	
	@Override
	public String toString() {
		return String.valueOf(lettre);
	}
	
	public static int getNbNoeudNonTerminaux() {
		return nbNoeudNonTerminaux;
	}
}
