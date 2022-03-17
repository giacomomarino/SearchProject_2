import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Indexer extends Stemmer {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new File("TestWiki.xml"));

    NodeList idList = document.getElementsByTagName("id");

    NodeList titleList = document.getElementsByTagName("title");

    NodeList textList = document.getElementsByTagName("text");

    //ids
    List<Integer> idListInt = new LinkedList<>();

    //titles
    List<String> titleListString = new LinkedList<>();

    //text
    List<String> textListString = new LinkedList<>();

    //id and title hashmap
    HashMap<Integer, String> idTitleHashmap = new HashMap<>();

    //(tf) id and hashmap of words to their frequency for that id
    HashMap<String, HashMap<Integer, Double>> tf = new HashMap<>();

    //word and idf of word
    HashMap<String, Double> idf = new HashMap<>();

    List<List<String>> textListRegex = new LinkedList<>();

    List<String> tempList;

    //links for each doc
    HashMap<Integer,List<String>> idLinks = new HashMap<>();

    //doc id to number of times the word that appears the most appears in doc
    HashMap<Integer,Double> idMax = new HashMap<>();

    HashMap<String,Integer> titleId = new HashMap<>();





    public Indexer() throws ParserConfigurationException, IOException, SAXException {




    }


    public void index() throws ParserConfigurationException, IOException, SAXException {


        Stemmer stemmer = new Stemmer();

        StopWords stopWords = new StopWords();


        //For every document(i) in corpus
        for (int i = 0; i < idList.getLength(); i++) {

            //get ids
            idListInt.add(Integer.parseInt(idList.item(i).getTextContent().trim()));

            //get titles
            titleListString.add(titleList.item(i).getTextContent().trim());

            //get texts
            textListString.add(textList.item(i).getTextContent());

            //add id(i) and title(i) to id and title hashmap
            idTitleHashmap.put(idListInt.get(i), titleList.item(i).getTextContent().trim());

            //making opposite of idTitle hashmap
            titleId.put(titleList.item(i).getTextContent().trim().toLowerCase(), idListInt.get(i));


            //finds links [] using matcher and adds them to a list which is then added to idLinks
            Pattern pattern = Pattern.compile("\\[(.*?)\\]");
            List<String> match = new LinkedList<>();

            //goes through list of links and deletes "[]" and then substrings them to after colon/splits at
            // "|" if needed
            Matcher matcher = pattern.matcher(textListString.get(i));
            while (matcher.find()) {
                String currString = matcher.group().toLowerCase(Locale.ROOT).replaceAll("]", "").replaceAll("\\[", "");
                match.add(currString.substring(currString.indexOf(":") + 1).trim().toLowerCase(Locale.ROOT));
            }

            List<String> match2 = new ArrayList<>();
            for (String str : match) {
                match2.addAll(Arrays.asList(str.split("\\|")));
            }

            match = match2;


            //adds list of links for this doc (i) to big idLinks list
            idLinks.put(i, match);

            textListRegex.add(Arrays.stream(textListString.get(i).trim().toLowerCase().replaceAll("[\\[\\](){}]", "").
                            split("([{\\s+<>,\".:;'?#!=*|}])")).map(s -> (String) s.trim()).
                    filter(s -> !s.equals("") && !stopWords.stopWord(s)).toList());


            tempList = new LinkedList<>();

            //goes through list of words of document(i) in textListRegex
            for (int x = 0; x < textListRegex.get(i).size(); x++) {

                //STEMMER
                //goes through each letter adds each char to stemmer then stems
                for (int y = 0; y < textListRegex.get(i).get(x).length(); y++) {

                    stemmer.add(textListRegex.get(i).get(x).charAt(y));
                }

                stemmer.stem();
                tempList.add(stemmer.toString());

            }

            double max = 0;

            for (String word : tempList) {

                if (tf.containsKey(word)) {

                    if (tf.get(word).containsKey(i)) {

                        tf.get(word).put(i, tf.get(word).get(i) + 1);
                        if (tf.get(word).get(i) > max) {
                            max = tf.get(word).get(i);
                        }
                    } else {

                        tf.get(word).put(i, 1.0);

                        //idf
                        if (idf.containsKey(word)) {

                            idf.put(word, idf.get(word) + 1);
                        } else {

                            idf.put(word, 1.0);
                        }
                    }
                } else {

                    HashMap<Integer, Double> hash = new HashMap<>();
                    hash.put(i, 1.0);

                    tf.put(word, hash);

                    //idf
                    if (idf.containsKey(word)) {

                        idf.put(word, idf.get(word) + 1);

                    } else {

                        idf.put(word, 1.0);
                    }
                }
            }

            idMax.put(i, max);
        }

    }


    public void rank() {


        HashMap<Integer, List<Double>> idToWeights = new HashMap<>();
        int numOfDocs = idListInt.size();

        for (Map.Entry<Integer, String> doc1 : idTitleHashmap.entrySet()) {
                List<Integer> links;
                List<Double> weights = new LinkedList<>();
                links = idLinks.get(doc1.getKey()).stream().map(elem -> titleId.get(elem)).toList();
            for (Map.Entry<Integer, String> doc2 : idTitleHashmap.entrySet()) {


                if (Objects.equals(doc1.getKey(), doc2.getKey())) {

                    weights.add(0.15/numOfDocs);

                }   else if (links.contains(doc2.getKey())) {
                    weights.add((0.15/numOfDocs) + (1 - 0.15)*(1.0/links.size()));
                }   else if (links.isEmpty()) {

                    weights.add((0.15/numOfDocs) + (1 - 0.15)*(1.0/(numOfDocs - 1)));

                }  else {

                    weights.add(0.15/numOfDocs);
                }


            }
            idToWeights.put(doc1.getKey(),weights);

            }

        // 1: .049, .475, .475
        // 2: .475, .049, .475
        // 3: .9, .049, .049










        /*
        for (Map.Entry<Integer, List<Double>> entry : idToWeights.entrySet()) {

            System.out.println((entry.getKey() + 1) + "::");
            for (int i = 0; i < entry.getValue().size(); i++) {
                System.out.println(entry.getValue().get(i));
            }
        }*/


        //calc ranks

        double check = 0.001;


        Double[] secondaryRanks = new Double[idToWeights.size()];
        Arrays.fill(secondaryRanks, 0.0);


        Double[] primaryRanks = new Double[idToWeights.size()];
        Arrays.fill(primaryRanks, 1.0 / idToWeights.size());

        while (true) {

            System.arraycopy(primaryRanks, 0, secondaryRanks, 0, idToWeights.size());

            for (int j = 0; j < primaryRanks.length; j++) {

                primaryRanks[j] = 0.0;

                for (int k = 0; k < primaryRanks.length; k++) {

                    primaryRanks[j] += idToWeights.get(k).get(j) * secondaryRanks[k];
                }
            }


            double dist = 0.0;

            for (int i = 0; i < primaryRanks.length; i++) {

                dist += Math.pow(primaryRanks[i] - secondaryRanks[i], 2);

            }

            if (dist < check) {
                break;
            }
        }

        HashMap<Integer, Double> documentRanks = new HashMap<>();

        for (int i = 0; i < primaryRanks.length; i++) {

            documentRanks.put(i, primaryRanks[i]);
            System.out.println(i + ": " + documentRanks.get(i));
        }

    }

    public void writeFile() {


        //PUTTING DATA OF HASHES INTO FILE

        // new file object
        File IdTitle = new File("IdTitle.txt");
        File Tf = new File("Tf.txt");
        File Idf = new File("Idf.txt");
        File IdLinks = new File("IdLinks.txt");
        BufferedWriter bf = null;

        //ID TITLE
        try {
                // create new BufferedWriter for the output file
                bf = new BufferedWriter(new FileWriter(IdTitle));

                // iterate map entries
                for (HashMap.Entry<Integer,String> entry : idTitleHashmap.entrySet()) {

                    // put key and value separated by a colon
                    bf.write(entry.getKey() + ":" + entry.getValue());

                    bf.newLine();

                }
                bf.flush();

        }
        catch (IOException e) {
                e.printStackTrace();
            }
        finally {

                try {
                    // always close the writer
                    bf.close();
                }
                catch (Exception e) {}
            }


        //TF
        try {
                // create new BufferedWriter for the output file
                bf = new BufferedWriter(new FileWriter(Tf));

            bf.newLine();

            // iterate map entries
                for (Map.Entry<String, HashMap<Integer, Double>> entry : tf.entrySet()) {

                    bf.write(entry.getKey() + "::");
                    bf.newLine();

                    for (Map.Entry<Integer,Double> entry2 : entry.getValue().entrySet()) {

                        bf.write(entry2.getKey() + ":" + entry2.getValue()/idMax.get(entry2.getKey()));
                        bf.newLine();

                    }

                    bf.newLine();
                }

                bf.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            try {

                // always close the writer
                bf.close();
            }
            catch (Exception e) {
            }
        }

        //IDLINKS
        try {
            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(IdLinks));

            bf.newLine();

            // iterate map entries
            for (Map.Entry<Integer, List<String>> entry : idLinks.entrySet()) {

                bf.write(entry.getKey() + "::");
                bf.newLine();

                for (String s:entry.getValue()) {

                    bf.write(s);
                    bf.newLine();
                }

                bf.newLine();
            }

            bf.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            try {
                // always close the writer
                bf.close();
            }
            catch (Exception e) {
            }
        }

        //IDF

        try {
            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(Idf));

            // iterate map entries
            for (HashMap.Entry<String,Double> entry : idf.entrySet()) {

                // put key and value separated by a colon
                bf.write(entry.getKey() + ":" + Math.log(idListInt.size()/entry.getValue()));

                // new line
                bf.newLine();
            }
            bf.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            try {
                // always close the writer
                bf.close();
            }
            catch (Exception e) {}
        }


    }


    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        Indexer indexer1 = new Indexer();
        indexer1.index();
        indexer1.rank();
        indexer1.writeFile();

    }
}
