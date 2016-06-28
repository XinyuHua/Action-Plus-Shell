import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by xinyu on 6/17/2016.
 *
 * This class extracts action instance or noun from corpus
 */
public class Extraction {

    private static final Integer NOUN_DICT_ID = 4;
    private static final Integer AC_DICT_ID = 1;
    private static final String NOUN_DICT_URL = "dat/noun/noun_" + NOUN_DICT_ID + ".dict";
    private static final String AC_DICT_URL = "dat/action/action_" + AC_DICT_ID + ".dict";
    private static final String NEWS_URL = "dat/news/bing_news_sliced/";
    private static final String NEWS_PARSED_URL = "dat/news/bing_news_sliced_parsed/";
    private static final String NEWS_POSTAG_URL = "dat/news/bing_news_sliced_postag/";
    private static final String VERB_URL = "dat/action/verb/verb.dict";
    private static final String INFLECTION_URL = "dat/action/verb/inflection.dict";

    private static final String NOUN_OUTPUT = "dat/noun/extracted_" + NOUN_DICT_ID + "/";
    private static final String ACI_T_OUTPUT = "dat/action/instance_extracted/with_targeted_verbs/";
    private static final String ACI_OUTPUT = "dat/action/instance_extracted/";
    private static final String ACC_OUTPUT = "dat/action/concept_extracted/yu_10/";

    private static final String ARG_URL = "dat/argument/yu_10_filtered/";

    private int lowerBound;
    private int upperBound;
    private int offset;

    private String[] puncArray = {",", ".", "!", "-", "?"};
    private static List<String> puncList;

    List<String> verbList;
    List<Set<String>> inflectionList;

    public Extraction(int l, int u, int o){
        this.lowerBound = l;
        this.upperBound = u;
        this.offset = o;
        puncList = Arrays.asList(puncArray);
        verbList = new ArrayList<>();
        inflectionList = new ArrayList<>();
    }

      /*
    This method extracts action instances from parsed corpus, the
     */
    public void extractActionInstanceFromNewsBody()throws Exception{
        int size = upperBound - lowerBound;

        loadVerbAndInflection();
        Wordnet wn = new Wordnet(true);
        Probase.initialization();
        Probase[] pcs = new Probase[size];
        for(int i = 0; i < size; ++i){
            pcs[ i ] = new Probase();
        }

        Runnable[] eNA = new Runnable[size];
        for(int i = 0; i < size; ++i){
            eNA[ i ] = new extractActionInstanceRunner(i + lowerBound, pcs[ i ], wn, verbList,
                                                        inflectionList, puncList, ACI_OUTPUT, offset);
        }

        Thread[] tdA = new Thread[size];
        for(int i = 0; i < size; ++i){
            tdA[ i ] = new Thread(eNA[ i ]);
            tdA[ i ].start();
        }

        for(int i = 0; i < size; ++i){
            tdA[ i ].join();
        }
        return;
    }

    public void extractActionConceptFromActionInstance()throws Exception{
        int size = upperBound - lowerBound;

        HashMap<String, List<String>> verb2Subj = new HashMap<>();
        HashMap<String, List<String>> verb2Obj = new HashMap<>();
        String line;
        String[] splitted;
        File folder = new File(ARG_URL);
        for(File file : folder.listFiles()){
            String verb = file.getName().replace(".txt","");
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            List<String> subjList = new ArrayList<>();
            List<String> objList = new ArrayList<>();
            line = fileReader.readLine().trim();
            if(line.length() > 0){
                splitted = line.split("\t");
                for(String subj : splitted){
                    subjList.add(subj);
                }
            }
            verb2Subj.put(verb, subjList);

            line = fileReader.readLine();
            if(line != null){
                line = line.trim();
                if(line.length() > 0){
                    splitted = line.split("\t");
                    for(String obj : splitted){
                        objList.add(obj);
                    }
                }
            }
            verb2Obj.put(verb, objList);
            fileReader.close();
        }


        Wordnet wn = new Wordnet(true);
        Probase[] pcs = new Probase[size];
        Probase.initialization();
        for(int i = 0; i < size; ++i){
            pcs[ i ] = new Probase();
        }

        Runnable[] eNA = new Runnable[size];
        for(int i = 0; i < size; ++i){
            eNA[ i ] = new extractActionConceptRunner(i + lowerBound, offset, verb2Subj, verb2Obj,
                    pcs[ i ], wn, ACI_OUTPUT, ACC_OUTPUT);
        }

        Thread[] tdA = new Thread[size];
        for(int i = 0; i < size; ++i){
            tdA[ i ] = new Thread(eNA[ i ]);
            tdA[ i ].start();
        }

        for(int i = 0; i < size; ++i){
            tdA[ i ].join();
        }

        return;
    }

    private void loadVerbAndInflection()throws Exception{
        BufferedReader verbReader = new BufferedReader(new FileReader( VERB_URL));
        BufferedReader inflReader = new BufferedReader(new FileReader( INFLECTION_URL));
        String line;
        while((line = verbReader.readLine())!=null){
            verbList.add(line);
        }
        verbReader.close();

        while((line = inflReader.readLine())!=null){
            inflectionList.add(new HashSet(Arrays.asList(line.split("\\s+"))));
        }
        inflReader.close();

    }
}
