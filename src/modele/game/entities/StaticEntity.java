package modele.game.entities;

public enum StaticEntity {
    EMPTY(0),
    WALL(0),
    GATE(0),
    GUM(10),
    GHOST_HOME(0),
    GHOST_SPAWN(0),
    PLAYER_SPAWN(0),
    ITEM_SPAWN(0),
    SUPER_GUM(50),
    CHERRY(100),
    STRAWBERRY(300),
    ORANGE(500),
    APPLE(700),
    MELON(1000),
    GALAXIAN_BOSS(2000),
    BELL(3000),
    KEY(5000);

    private int score;
    private int count;

    StaticEntity(int score) {
        this.score = score;
        this.count = 0;
    }

    /**
     * Return the score of the entity when walked over by the player
     * @return the entity's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Set the count of the entity
     * @param count the new count of entities
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Return the count of the entity
     * @return the count of entities
     */
    public int getCount() {
        return count;
    }

    /**
     * Add a number of entities to the total count
     * @param nb the number of entities added
     */
    public void addCount(int nb) {
        count += nb;
    }
}
