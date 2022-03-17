import java.util.Arrays;

public class StopWords {

    String[] wordsArray = {
 "a", "about", "above", "after", "against", "all", "am", "an",
         "and", "any", "are", "aren't", "as", "at", "be", "because", "been",
         "before", "be", "below", "between", "both", "but", "by", "can't",
         "cannot", "could", "couldn't", "did", "didn't", "does", "doesn't",
         "do", "don't", "down", "each", "few", "for", "from", "further",
         "had", "hadn't", "hasn't", "have", "haven't", "he", "he'd", "he'll",
         "her", "here", "herself", "him", "himself", "hi", "how", "i", "i'd",
         "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's",
         "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
         "myself", "no", "nor", "not", "of", "off", "on", "or", "other",
         "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
         "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't",
         "so", "some", "such", "than", "that", "that's", "the", "their", "them",
         "themselves", "then", "there", "there's", "these", "their", "they'd",
         "they'll", "they're", "they've", "this", "those", "through", "to", "too",
         "under", "until", "up", "was", "wasn't", "we", "we'd", "we'll", "we're",
         "we've", "were", "weren't", "what", "what's", "when", "when's", "where",
         "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with",
         "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
         "your", "yourself", "yourselves", "follow"};

        //returns true - word is a stop word
        public boolean stopWord(String word) {

            return Arrays.stream(wordsArray).toList().contains(word);

        }


}
