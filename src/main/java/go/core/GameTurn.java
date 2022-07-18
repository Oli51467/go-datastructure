package go.core;

import java.util.Arrays;
import java.util.Set;

public class GameTurn {
    private final int[][] BoardState;
    private final int x, y;
    private final int hashCode;
    private int passCount;

    public GameTurn(int width, int height) {
        BoardState = new int[width][height];
        passCount = 0;

        x = -1;
        y = -1;

        hashCode = Arrays.deepHashCode(BoardState);
    }

    private GameTurn(GameTurn prev, int X, int Y, int playerId, int handicap, Set<Point> freedPoint) {
        int width = prev.BoardState.length;
        int height = prev.BoardState[0].length;

        BoardState = new int[width][height];
        for (int i = 0; i < width; i++) {
            BoardState[i] = prev.BoardState[i].clone();
        }
        x = X;
        y = Y;

        if (x >= 0 && y >= 0) {
            BoardState[x][y] = playerId;
            passCount = 0;
        } else {
            passCount = prev.passCount + 1;
        }

        for (Point freedpoint : freedPoint) {
            BoardState[freedpoint.getX()][freedpoint.getY()] = 0;
        }
        hashCode = Arrays.deepHashCode(BoardState);
    }

    public GameTurn toNext(int X, int Y, int playerId, int handicap, Set<Point> freedPoint) {
        return new GameTurn(this, X, Y, playerId, handicap, freedPoint);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        GameTurn castedObj = (GameTurn) obj;

        return hashCode == castedObj.hashCode && Arrays.deepEquals(this.BoardState, castedObj.BoardState);
    }
}
