package main;
import knowledgebase.*;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

/*
 * Author: xinyu
 * Last Modified: 5/24/2016 
 * 
 * Description: This class deals with: 
 *  1). Find action concepts from action instances 
 */

public class Main {

	final private static String ACTION_TMP_URL = "dat/tmp/action_occurrence_tmp/";
	private static String INSTANCE_TMP_URL = "dat/tmp/instance_occurrence_tmp/";
	final static private String ACTION_NOUN_MAP_FILTERED_URL = "dat/concept_dict/1_filtered_100/";

	private static Wordnet wordnet;
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
		
        
        
        getConceptFromInstanceRunner(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		
        
        
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

	/*
         * This method get action concepts from action instances
    */
	public static void getConceptFromInstanceRunner(int fileBase, int size)throws Exception{

		// Load action concept
		loadSubjAndObjFromFiltered();

		wordnet = new Wordnet(true);
		// Start searching for nouns in news title
		Runnable[] gNA = new Runnable[size];
		for(int i = 0; i < size; ++i){
			gNA[ i ] = new getConceptFromInstance(i + fileBase, offset, verb2subj, verb2obj, new ProbaseClient(4400 + i), wordnet);
		}

		Thread[] tdA = new Thread[size];
		for(int i = 0; i < size; ++i){
			tdA[ i ] = new Thread(gNA[ i ]);
			tdA[ i ].start();
		}

		for(int i = 0; i < size; ++i){
			tdA[ i ].join();
		}
		return;
	}

	private static void loadSubjAndObjFromFiltered()throws Exception{
			verb2obj = new HashMap<>();
		verb2subj = new HashMap<>();
		ProbaseClient tmpPb = new ProbaseClient(4400);
		File folder = new File(ACTION_NOUN_MAP_FILTERED_URL);
		String line = null;
		System.out.println("Start loading arguments by verb...");
		for(File file  : folder.listFiles()) {
			String actionName = file.getName().replaceAll(".txt","");
			String subj = "", verb= "", obj = "";
			String[] splitted = actionName.split("_");
			if(splitted.length == 3){
				obj = splitted[2];
			}
			subj = splitted[0];
			verb = splitted[1];
			if(!verb2subj.containsKey(verb)){
				verb2subj.put(verb, new ArrayList<>());
			}
			verb2subj.get(verb).add(subj);

			if(!verb2obj.containsKey(verb)){
				verb2obj.put(verb, new ArrayList<>());
			}
			verb2obj.get(verb).add(obj);

		}

		for(String verb : verb2subj.keySet()){
			List<String> subjList = verb2subj.get(verb);
			List<String> objList = verb2obj.get(verb);
			List<Argument> argumentList = new ArrayList<>();
			for(String concept : subjList) {
				int hypoNum = 0;
				if(!concept.equals("")){
					hypoNum = tmpPb.getHypoNumber(concept);
				}
				argumentList.add(new Argument(concept, hypoNum));
			}

			Collections.sort(argumentList);
			List<String> sortedSubjList = new ArrayList<>();
			List<String> correspondentObjList = new ArrayList<>();

			for(Argument subj : argumentList){
				String subjName = subj.getName();
				sortedSubjList.add(subjName);
				correspondentObjList.add(objList.get( subjList.indexOf(subjName) ));
			}
			verb2subj.put(verb, sortedSubjList);
			verb2obj.put(verb, correspondentObjList);
		}
		System.out.println("Arguments loaded");
		tmpPb.disconnect();
}
}



class Argument implements Comparable{
	String name;
	int instanceNum;

	public Argument(String name, int num){
		this.name = name;
		this.instanceNum = num;
	}

	public String getName(){
		return name;
	}

	@Override
	public int compareTo(Object o) {
		int oNum = ((Argument)o).instanceNum;
		return this.instanceNum - oNum;
	}
}
