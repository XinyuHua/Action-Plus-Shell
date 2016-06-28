package main;

import knowledgebase.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xinyu on 5/18/2016.
 * Last modified: 5/24/2016.
 */
public class getConceptFromInstance implements Runnable {
    final private static String INSTANCE_OCCURRENCE_URL = "dat/tmp/instance_occurrence_tmp/";
    final private static String AC_OCCURRENCE_URL = "dat/tmp/concept_occurrence_tmp/";
    private ProbaseClient pb;
    private int part;
    private int offset;
    private static HashMap<String, List<String>> verb2subj;
    private static HashMap<String, List<String>> verb2obj;
    private static List<String> specialList;
    private static Wordnet wordnet;
    public getConceptFromInstance(int part, int offset, HashMap<String, List<String>> v2s,
                                  HashMap<String, List<String>> v2o, ProbaseClient pb, Wordnet wn){
        this.part = part;
        this.offset = offset;
        this.verb2obj = v2o;
        this.verb2subj = v2s;
        this.pb = pb;
        this.wordnet = wn;
        specialList = new ArrayList<>();
        specialList.add("person");
        specialList.add("thing");
        specialList.add("percent");
        specialList.add("money");

    }

    @Override
    public void run() {
        try{
            BufferedReader br = new BufferedReader(new FileReader(INSTANCE_OCCURRENCE_URL + part + ".txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter(AC_OCCURRENCE_URL + part + ".txt"));
            String line;
            int curLineNumber = 0;
            int acFound = 0;
            while((line = br.readLine())!=null) {
                if(curLineNumber % 100 == 0){
                    System.out.println("file id:" + part + "\tline num:" + curLineNumber + "\tac found:" + acFound);
                }

                curLineNumber++;
                String[] lineSplit = line.split("\t");
                String globalIdx = lineSplit[0];
                String outputBuffer = "";

                for(int k = 1; k < lineSplit.length; ++k){
                    String instance = lineSplit[ k ];

                    String[] instanceSplit = instance.split("_");
                    String verb = "",obj = "",subj = "";
                    if(instanceSplit.length == 3){
                        obj = instanceSplit[2].trim();
                       // obj = wordnet.stemNounFirst(obj);
                    }
                    subj = instanceSplit[ 0 ].trim();
                   // if(!subj.equals("")) subj = wordnet.stemNounFirst(subj);
                    verb = instanceSplit[ 1 ];

                    if(!verb2subj.containsKey(verb))continue;

                    List<String> subjListFromVerb = verb2subj.get(verb);
                    List<String> objListFromVerb = verb2obj.get(verb);

                    List<String> subjConcept = new ArrayList<>();
                    List<String> objConcept = new ArrayList<>();
                    int i = 0;
                    for(String subjc : subjListFromVerb) {
                        String objc = objListFromVerb.get(i);
                        // subjc and objc are correspondent arguments for the current verb
                        i++;
                        if(pb.getPop(subjc, subj) > 10 && pb.getPop(objc, obj) > 10){
                            subjConcept.add(subjc);
                            objConcept.add(objc);
                            break;
                        }
                    }

                    if(subjConcept.size() > 0) {
                        int n = 0;
                        for (String subjc : subjConcept) {
                            String objc = objConcept.get(n);
                            n++;
                            acFound++;
                            outputBuffer += "\t" + subjc + "_" + verb + "_" + objc;
                        }
                    }
                }

                if(outputBuffer.length() > 0){
                    bw.append(globalIdx + outputBuffer);
                    bw.newLine();
                    bw.flush();
                }

            }
            br.close();
            bw.close();
            pb.disconnect();
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Error opening file!");
        }
    }
}
