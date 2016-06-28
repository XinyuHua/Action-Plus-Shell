import edu.mit.jwi.item.POS;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

import org.apache.commons.io.FileUtils;

/*
 * Author: xinyu
 * Last Modified: 2016-06-20
 * 
 * Description: This class deals with:
 *  1). Build Noun list
 * 	2). Extract Action instance from news body
 *  3). Extract noun concept from news title
 *  4). Build Action-noun map
 *  5). Process news data
 */

public class Main {

	private static String VERB_DICT_URL = "dat/verb/verb.dict";
	private static String INFL_DICT_URL = "dat/verb/inflection.dict";

	private static List<String> puncList;
	final private static String[] VERB_TAG = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
	private static String[] DUMMY_VERBS = {"be","am","is","are","was","were","have","has","had","get","got","gets"};
	private static List<String[]> actionList;
	private static List<String> verbList;
	private static List<Set<String>> inflectionList;
	private static HashSet<String> inflectionSet;
	private static List<HashSet<String>> synonymList;
	private static HashSet<String> synsetSet;
	private static List<String> dummyVerbs;
	private static HashMap<String, List<String>> verb2subj;
	private static HashMap<String, List<String>> verb2obj;
	private static int offset = 55914;

	public static void main(String args[]) throws Exception{

		Date rightNow;
		Locale currentLocale;
		DateFormat timeFormatter;
		DateFormat dateFormatter;
		String timeOutput;
		String dateOutput;

		long startTime = System.nanoTime();

        Integer lower = Integer.parseInt( args[ 0 ] );
        Integer upper = Integer.parseInt( args[ 1 ] );
		extractActionConcept(lower , upper);
		//extractActionConcept(80, 90);
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Time elapsed:" + duration/1e9 + " sec");

		rightNow = new Date();
		currentLocale = new Locale("en");

		timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, currentLocale);
		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, currentLocale);

		timeOutput = timeFormatter.format(rightNow);
		dateOutput = dateFormatter.format(rightNow);

		System.out.println(timeOutput);
		System.out.println(dateOutput);

	}

	public static void extractActionConcept(int l, int u)throws Exception{
		Extraction e = new Extraction(l, u, offset);
		e.extractActionConceptFromActionInstance();
	}
    
    public static void extractActionInstance(int l, int u)throws Exception{
        Extraction e = new Extraction(l, u, offset);
        e.extractActionInstanceFromNewsBody();
    }
}

