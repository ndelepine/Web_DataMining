package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Classe de racinisation (stemming) des mots
 * en français.
 * Modification légère du package SnowBall
 * http://snowball.tartarus.org/download.php
 * @author xtannier
 *
 */
public class FrenchStemmer extends org.tartarus.snowball.ext.frenchStemmer implements Normalizer {

	private static short REPEAT = 1;
	
	public FrenchStemmer() 
    {
		
	}

	public ArrayList<String> normalize(File file) throws IOException {		
		String text = "";
		//lecture du fichier texte	
		InputStream ips=new FileInputStream(file); 
		InputStreamReader ipsr = new InputStreamReader(ips, "ISO-8859-1");
		BufferedReader br = new BufferedReader(ipsr);
		String line;
		while ((line = br.readLine())!=null)
                {
                    text += line + " ";
		}
		br.close(); 
		
		ArrayList<String> words = (new FrenchTokenizer()).tokenize(text.toLowerCase());
		ArrayList<String> result = new ArrayList<String>();
		for (String word : words) {
			this.setCurrent(word);
			for (int i = REPEAT; i != 0; i--) {
				this.stem();
			}
			result.add(this.getCurrent());
		}
		return result;
	}

	
	public ArrayList<String> normalize(String text) 
        {
		ArrayList<String> words = (new FrenchTokenizer()).tokenize(text.toLowerCase());
		return words;
	}

	public static void main(String[] args) 
        {
	}
}
