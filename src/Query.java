import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
import java.util.Scanner;


public class Query {

    //id and title hashmap
    HashMap<Integer, String> idTitleHashmap = new HashMap<>();

    //(tf) id and hashmap of words to their frequency for that id
    HashMap<String, HashMap<Integer, Double>> tf = new HashMap<>();

    //word and idf of word
    HashMap<String, Double> idf = new HashMap<>();


    public void readData() throws FileNotFoundException {

        // pass the path to the file as a parameter
        File file = new File("Idf.txt");
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            String[] lineSplit = line.split(":");
            idf.put(lineSplit[0], Double.parseDouble(lineSplit[1]));
        }


        file = new File("IdTitle.txt");
        sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            String[] lineSplit = line.split(":");
            idTitleHashmap.put(Integer.parseInt(lineSplit[0]), lineSplit[1]);
        }


        file = new File("Tf.txt");
        sc = new Scanner(file);

        String line = sc.nextLine();

        while (sc.hasNextLine()) {

            if (Objects.equals(line, "")) {

                if (sc.hasNextLine()) {

                    HashMap<Integer, Double> hash = new HashMap<>();

                    line = sc.nextLine();

                    String[] lineSplit = line.split("::");

                    tf.put(lineSplit[0], hash);

                    line = sc.nextLine();

                    while (!(Objects.equals(line, ""))) {

                        lineSplit = line.split(":");

                        hash.put(Integer.parseInt(lineSplit[0]), Double.parseDouble(lineSplit[1]));

                        line = sc.nextLine();
                    }


                }

            }

        }


    }


    public static void main(String[] args) throws FileNotFoundException {

        Query query = new Query();

        Scanner scanner = new Scanner(System.in);

        StopWords stopWords = new StopWords();

        Stemmer stemmer = new Stemmer();

        query.readData();

        while (true) {

            System.out.print("Search: ");


            String userInput = scanner.nextLine();

            if (Objects.equals(userInput, "")) {
                continue;
            }

            List<String> inputList = Arrays.stream(userInput.trim().toLowerCase().replaceAll("[\\[\\](){}]", "").
                            split("([{\\s+<>,\".:;'?#!=*|}])")).map(s -> (String) s.trim()).
                    filter(s -> !s.equals("") && !stopWords.stopWord(s)).toList();

            List<String> inputListStemmed = new LinkedList<>();

            for (String s : inputList) {

                for (int ch = 0; ch < s.length(); ch++) {
                    stemmer.add(s.charAt(ch));
                }

                stemmer.stem();
                inputListStemmed.add(stemmer.toString());
            }

            //document id to its rank
            HashMap<Integer, Double> documentRanks = new HashMap<>();

            for (String s : inputListStemmed) {

                System.out.println(s);
            }

            //FOR EACH WORD (if the document rank contains the id add the id rank to that if not, add id & rank to hash)
            for (String s : inputListStemmed) {

                if (!query.tf.containsKey(s)) {
                    break;
                }

                for (HashMap.Entry<Integer, Double> entry : query.tf.get(s).entrySet()) {
                    if (documentRanks.containsKey(entry.getKey())) {

                        documentRanks.put(entry.getKey(), documentRanks.get(entry.getKey()) +
                                (entry.getValue() * query.idf.get(s)));
                    } else {
                        documentRanks.put(entry.getKey(), entry.getValue() * query.idf.get(s));
                    }
                }
            }

            List<Tuple> documentRankList = new LinkedList<>();

            for (HashMap.Entry<Integer, Double> entry : documentRanks.entrySet()) {

                documentRankList.add(new Tuple(entry.getKey(),entry.getValue()));
            }

            documentRankList.sort(Comparator.comparing(Tuple::getRank));

            int resultNum = 10;

            for (int i = 0; i < resultNum; i++) {

                System.out.println((i+1) + ": " + query.idTitleHashmap.get(documentRankList.get(i).getId()));
            }

        }
    }
}