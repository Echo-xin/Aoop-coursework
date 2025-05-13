import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Model implements IModel {
    private List<String> validWords;
    private String startWord;
    private String targetWord;
    private String lastValidWord;
    private boolean gameWon;
    private int attempts;
    private static final int WORD_LENGTH = 4;
    private boolean isCLI;
    private List<GameObserver> observers;

    public Model(boolean isCLI) {
        this.isCLI = isCLI;
        this.validWords = loadDictionary();
        this.observers = new ArrayList<>();
        if (validWords.isEmpty()) {
            throw new IllegalStateException("Dictionary failed to load or is empty");
        }
        this.gameWon = false;
        this.attempts = 0;
        selectWords();
        lastValidWord = startWord;
    }

    @Override
    public void addGameObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeGameObserver(GameObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyGameObservers(String message) {
        for (GameObserver observer : observers) {
            observer.onGameUpdate(message);
        }
    }

    private List<String> loadDictionary() {
        List<String> dictionary = new ArrayList<>();
        String path = "dictionary.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.length() == WORD_LENGTH && line.matches("[a-z]+")) {
                    dictionary.add(line);
                }
            }
        } catch (IOException e) {
            if (isCLI) {
                System.err.println("Unable to load dictionary file: " + path);
            }
            e.printStackTrace();
        }
        return dictionary;
    }

    private void selectWords() {
        Random rand = new Random();
        startWord = validWords.get(rand.nextInt(validWords.size()));
        do {
            targetWord = validWords.get(rand.nextInt(validWords.size()));
        } while (targetWord.equals(startWord));

        // Reset game state
        lastValidWord = startWord;
        gameWon = false;
        attempts = 0;
    }

    @Override
    public boolean isOneLetterDifferent(String word1, String word2) {
        if (word1 == null || word2 == null || word1.length() != word2.length()) {
            return false;
        }

        int diffCount = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diffCount++;
                if (diffCount > 1)
                    return false;
            }
        }
        return diffCount == 1;
    }

    @Override
    public boolean isValidWord(String word) {
        return word != null &&
                word.length() == WORD_LENGTH &&
                word.matches("[a-z]+") &&
                validWords.contains(word.toLowerCase());
    }

    @Override
    public void processWord(String input) {
        if (gameWon) {
            notifyWithMessage("Game already over. You won!");
            return;
        }

        // Basic validation
        if (input == null || input.isEmpty()) {
            notifyWithMessage("Error: Please enter a word");
            return;
        }

        input = input.toLowerCase().trim();

        if (input.length() != WORD_LENGTH) {
            notifyWithMessage("Error: Word must be 4 letters");
            return;
        }

        if (!input.matches("[a-z]+")) {
            notifyWithMessage("Error: Only lowercase letters allowed");
            return;
        }

        // Dictionary validation
        if (!isValidWord(input)) {
            notifyWithMessage("Error: Word not in dictionary");
            return;
        }

        // Check if only one letter is different
        if (!isOneLetterDifferent(input, lastValidWord)) {
            notifyWithMessage("Error: Only one letter can be changed at a time");
            return;
        }

        attempts++;
        lastValidWord = input;

        // Check if won
        if (input.equals(targetWord)) {
            gameWon = true;
            notifyWithMessage("Game Won!");
            return;
        }

        // Generate feedback
        String feedback = generateFeedback(input);
        notifyWithMessage("Feedback: " + feedback);
    }

    private String generateFeedback(String input) {
        StringBuilder feedback = new StringBuilder();
        char[] targetChars = targetWord.toCharArray();
        char[] inputChars = input.toCharArray();

        // 检查每个字母的匹配情况
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (inputChars[i] == targetChars[i]) {
                feedback.append("G"); // 字母正确且位置正确
            } else {
                feedback.append("X"); // 字母不匹配或位置不正确
            }
        }

        return feedback.toString();
    }

    @Override
    public String getStartWord() {
        return startWord;
    }

    @Override
    public String getTargetWord() {
        return targetWord;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    private void notifyWithMessage(String message) {
        if (isCLI) {
            if (message.startsWith("Error:")) {
                System.err.println(message);
            } else {
                System.out.println(message);
            }
        }
        notifyGameObservers(message);
    }
}
