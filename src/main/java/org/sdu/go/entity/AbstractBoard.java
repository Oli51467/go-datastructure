package org.sdu.go.entity;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.sdu.go.common.Constants.*;

public abstract class AbstractBoard implements Board {

    public Integer player;
    public Integer size;
    public Integer playCount;
    public int[][] board;

    public Deque<Point> steps;
    public Deque<String> gameRecord = new ArrayDeque<>();

    public AbstractBoard(Integer size) {
        this.size = size;
        this.playCount = 0;
        this.player = BLACK;
        this.board = new int[size + 2][size + 2];
        // 初始化棋盘
        StringBuilder tmp = new StringBuilder();
        for (int x = 1; x <= this.size + 1; x++) {
            for (int y = 1; y <= this.size + 1; y++) {
                board[x][y] = EMPTY;
                tmp.append(EMPTY);
            }
        }
        gameRecord.push(tmp.toString());
        // 记录每一步,初始为空
        steps = new ArrayDeque<>();
        steps.push(new Point(-1, -1));
    }

    @Override
    public void changePlayer() {
        if (player == BLACK) player = WHITE;
        else player = BLACK;
    }

    @Override
    public boolean play(Integer x, Integer y) {
        if (null == x || null == y) return false;
        return isInBoard(x, y) && board[x][y] == EMPTY;
    }

    @Override
    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= size && y > 0 && y <= size);
    }

    @Override
    public void saveState() {
        StringBuilder res = new StringBuilder();
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= this.size; j++) {
                res.append(board[i][j]);
            }
        }
        gameRecord.push(res.toString());
    }

    @Override
    public abstract void regretPlay();

    public int[][] getBoard() {
        return board;
    }

    public int getPlayCount() {
        return playCount;
    }
}
