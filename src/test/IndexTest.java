package test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import index.Index;
import index.TermTooFrequent;
import noeuds.Noeud;

public class IndexTest {

	@Test
	public final void testDeleteTerm() throws IOException{

		String path="Doc/lemmatisation";
		Index index=new Index(path);
		TermTooFrequent frequent=new TermTooFrequent(path,0.2f);
		index.setDebutTerme(Index.InitialiserIndex(path, frequent));
		

		Noeud temp=index.getDebutTerme().get(2);
		temp=temp.getNoeudsFils().get(0);
		temp=temp.getNoeudsFils().get(0);
		assertEquals(2, temp.getNoeudsFils().size()); //pour des objets ou des longs
		
		temp=temp.getNoeudsFils().get(1);
		while(temp.getNoeudsFils().size()>0){
			temp=temp.getNoeudsFils().get(0);
		}
		
		File file = new File(path);
		index.deleteTerm("avocat");
		temp=index.getDebutTerme().get(2);
		temp=temp.getNoeudsFils().get(0);
		temp=temp.getNoeudsFils().get(0);
		assertEquals(1, temp.getNoeudsFils().size()); //pour des objets ou des longs
		while(temp.getNoeudsFils().size()>0){
			temp=temp.getNoeudsFils().get(0);
		}
	}

}
