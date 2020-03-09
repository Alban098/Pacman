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

    public int getScore() {
        return score;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void addCount(int nb) {
        count += nb;
    }
}
