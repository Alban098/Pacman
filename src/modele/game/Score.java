package modele.game;

public class Score implements Comparable<Score> {

    private int score;
    private String name;

    public Score(int score, String name) {
        this.score = score;
        this.name = name;
    }

    /**
     * Return the value of the score
     * @return the value of the score
     */
    public int getScore() {
        return score;
    }

    /**
     * Get the username associated with that score
     * @return the username associated with the score
     */
    public String getName() {
        return name;
    }

    /**
     * Compare the current Score to another
     * @param o the score to be compared to
     * @return a value less than 0 is the argument is greater, 0 if equals and a value greater than 0 otherwise
     */
    @Override
    public int compareTo(Score o) {
        Integer s = score;
        return s.compareTo(o.score);
    }
}
