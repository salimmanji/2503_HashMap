import java.util.Scanner;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;

/**
 * COMP 2503 Winter 2020 Assignment 4, April 3rd, 2020.
 * 
 * This program reads a text file and compiles a list of the Tokens together
 * with the frequency of the Tokens. Using a HashMap from Java collections, a
 * framework for storing Tokens is created. Tokens from a standard list of stop
 * Tokens are then deleted. Then, new TreeMaps with ordering based on frequency
 * (descending), word length and by alphabetical order are populated to provide
 * the required output.
 * 
 * @author Salim Manji
 */

public class A4 {

	private HashMap<String, Token> tokens = new HashMap<>();
	private TreeMap<Token, Token> wordsByNaturalOrder = new TreeMap<>();
	private TreeMap<Token, Token> wordsByLength = new TreeMap<>(new LengthDesc());
	private TreeMap<Token, Token> wordsByFreqDesc = new TreeMap<>(new FreqDesc());
	private static final int NUM_WORDS_TO_PRINT = 10;

	private String[] stopTokens = { "a", "about", "all", "am", "an", "and", "any", "are", "as", "at", "be", "been",
			"but", "by", "can", "cannot", "could", "did", "do", "does", "else", "for", "from", "get", "got", "had",
			"has", "have", "he", "her", "hers", "him", "his", "how", "i", "if", "in", "into", "is", "it", "its", "like",
			"more", "me", "my", "no", "now", "not", "of", "on", "one", "or", "our", "out", "said", "say", "says", "she",
			"so", "some", "than", "that", "thats", "the", "their", "them", "then", "there", "these", "they", "this",
			"to", "too", "us", "upon", "was", "we", "were", "what", "with", "when", "where", "which", "while", "who",
			"whom", "why", "will", "you", "your", "up", "down", "left", "right", "man", "woman", "would", "should",
			"dont", "after", "before", "im", "men" };

	private int totalTokencount = 0;
	private int stopTokencount = 0;

	public static void main(String[] args) {
		A4 a4 = new A4();
		a4.run();
	}

	/* Run the program. */
	private void run() {
		readFile();
		removeStop();
		createFreqLists();
		printResults();
	}

	/**
	 * Instructions to determine what information to output to a .txt file.
	 * Try-catch blocks are used to avoid null pointer exceptions.
	 */
	private void printResults() {
		int numUniqueWords = wordsByNaturalOrder.size();
		int loopEnd = Math.min(NUM_WORDS_TO_PRINT, numUniqueWords);

		System.out.println("Total Words: " + totalTokencount);
		System.out.println("Unique Words: " + tokens.size());
		System.out.println("Stop Words: " + stopTokencount);
		System.out.println();
		System.out.println("10 Most Frequent");
		printQuery(wordsByFreqDesc, loopEnd);
		System.out.println();
		System.out.println("10 Longest");
		printQuery(wordsByLength, loopEnd);
		System.out.println();

		try {
			System.out.println("The longest word is " + returnLongestWord(wordsByLength));
		} catch (Exception e) {
			System.out.println("The longest word is None");
		}

		try {
			System.out.println("The shortest word is " + returnShortestWord(wordsByLength));
		} catch (Exception e) {
			System.out.println("The shortest word is None");
		}

		try {
			System.out.println("The average word length is " + avgLength());
		} catch (Exception e) {
			System.out.println("The average word length is 0");
		}

		System.out.println();
		System.out.println("All");
		printQuery(wordsByNaturalOrder, numUniqueWords);
	}

	/**
	 * If at least one word has been added to the HashMap tokens, this method will
	 * go through each object stored and sum the number of letters in each word,
	 * then divide by the number of words (number of elements) to find the average
	 * length of each word.
	 * 
	 * @return the average length of each word.
	 */
	private int avgLength() {
		int words = 0;
		int chars = 0;

		if (tokens.size() > 0) {
			words = tokens.size();
			for (String curr : tokens.keySet()) {
				chars += curr.length();
			}
		}
		return chars / words;
	}

	/**
	 * Finds the left most node in the TreeMap (word with the shortest word length).
	 * 
	 * @param map the TreeMap to search through.
	 * @return the shortest word.
	 */
	private String returnShortestWord(TreeMap<Token, Token> map) {
		return map.get(map.lastKey()).getWord();
	}

	/**
	 * Finds the right most node in the TreeMap (word with the longest word length).
	 * 
	 * @param map the TreeMap to search through.
	 * @return the longest word.
	 */
	private String returnLongestWord(TreeMap<Token, Token> map) {
		return map.get(map.firstKey()).getWord();
	}

	/*
	 * Reads the file and add words to the tree.
	 */
	private void readFile() {
		Scanner input = new Scanner(System.in);
		boolean stopWordStatus = false;

		while (input.hasNext()) {
			String currentWord = getWord(input);

			if (isString(currentWord)) {
				totalTokencount++;
				if (!isUnique(currentWord)) {
					increaseTally(currentWord);
				} else {
					if (isStopWord(currentWord)) {
						stopWordStatus = true;
						stopTokencount++;
					}
					Token currentToken = new Token(currentWord, stopWordStatus);
					tokens.put(currentWord, currentToken);
				}
			}
			stopWordStatus = false;
		}
		input.close();
	}

	/**
	 * When called, this method removes all the stop words from the tokens HashMap.
	 */
	private void removeStop() {
		if (tokens.size() > 0) {

			Iterator<Map.Entry<String, Token>> it = tokens.entrySet().iterator();

			while (it.hasNext()) {
				Token currentToken = it.next().getValue();
				if (currentToken.getStop()) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Populates the wordsByNaturalOrder, wordsByFreqDesc and wordsByLength TreeMaps
	 * using entries in the tokens HashMap.
	 */
	private void createFreqLists() {
		populate(wordsByNaturalOrder);
		populate(wordsByFreqDesc);
		populate(wordsByLength);
	}

	/**
	 * Determines if the String read in by the file reader contains a value, or if
	 * it is empty.
	 * 
	 * @param curr is the current word being read in by the file reader that needs
	 *             to be checked.
	 * @return True if length is greater than 0, False if 0 chars.
	 * 
	 */
	private boolean isString(String curr) {
		return (curr.length() > 0);
	}

	/**
	 * Cycles through the array of stopWords to determine if the word read in is a
	 * stop word.
	 * 
	 * @param curr is the current word being read in by the file reader that needs
	 *             to be checked.
	 * @return True if curr is on the list of stopWords, False if not on list of
	 *         stopWords.
	 * 
	 */
	private boolean isStopWord(String curr) {
		boolean stopWordFound = false;

		for (String word : stopTokens) {
			if (word.equals(curr)) {
				stopWordFound = true;
				break;
			}
		}
		return stopWordFound;
	}

	/**
	 * Confirms the wordList size is greater than zero to avoid a null pointer
	 * exception, then cycles through all values of the TreeMap to output each word
	 * and the occurrence of that word to the output.txt file.
	 * 
	 * @param endIndex is the size of the TreeMap if less than 10 unique words are
	 *                 in tokens, 10 if the TreeMap more than 10 unique words are in
	 *                 TreeMap or the size of tokens when printing all (10 has been
	 *                 initialized to constant "NUM_WORDS_TO_PRINT").
	 */
	private void printQuery(TreeMap<Token, Token> map, int endIndex) {
		if (endIndex > 0)
			printList(map, endIndex);
	}

	/**
	 * Iterates through the TreeMap and outputs the desired toString of each object
	 * to the .txt file.
	 * 
	 * @param map      is the TreeMap to iterate through.
	 * @param endIndex Signals when to stop iterating through the tree.
	 */
	private void printList(TreeMap<Token, Token> map, int endIndex) {
		int counter = 0;
		Iterator<Map.Entry<Token, Token>> it = map.entrySet().iterator();

		while (it.hasNext() && counter < endIndex) {
			System.out.println(it.next().getValue());
			counter++;
		}
	}

	/**
	 * Determines whether the current word has already been read in by the file
	 * reader and added to the HashMap tokens.
	 * 
	 * @param toCheck is the current word being read in by the file reader that
	 *                needs to be checked.
	 * @return True if the word has not yet been read in and is unique, False if it
	 *         has already been read in and is not unique.
	 * 
	 */
	private boolean isUnique(String toCheck) {
		return (tokens.get(toCheck) == null);
	}

	/**
	 * When called, this method finds the specific token to increase using the key
	 * (String word) from the HashMap tokens, then increases the frequency counter
	 * held by the object.
	 * 
	 * @param toIncrease is the key (of type String).
	 * 
	 */
	private void increaseTally(String toIncrease) {
		Token curr = tokens.get(toIncrease);

		if (curr != null) {
			curr.increaseFrequency();
		}
	}

	/**
	 * Pulls the next word scanned from the input.txt file.
	 * 
	 * @return The word to be scanned.
	 */
	private String getWord(Scanner input) {
		return input.next().trim().toLowerCase().replaceAll("[^a-z]", "");
	}

	/**
	 * Each Token in the 'tokens' HashMap is added to the new TreeMap.
	 * 
	 * @param toPopulate the TreeMap to populate.
	 */
	private void populate(TreeMap<Token, Token> toPopulate) {

		for (Map.Entry<String, Token> curr : tokens.entrySet()) {
			toPopulate.put(curr.getValue(), curr.getValue());
		}
	}

}
