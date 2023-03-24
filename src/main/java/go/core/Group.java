package go.core;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import static go.core.Board.*;
import static go.core.common.Constants.*;

@Data
public class Group {

    private int liberties;
    private int length;
    public Set<Point> stones;
    private Boolean[][] st;

    public Group(int x, int y) {
        this.liberties = 0;
        this.length = 1;
        stones = new HashSet<>();
        st = new Boolean[WIDTH + 1][WIDTH + 1];
        reset();
        add2Group(x, y);
    }

    private void add2Group(int x, int y) {
        Point point = new Point(x, y);
        stones.add(point);
    }

    private void reset() {
        for (int x = 1; x <= WIDTH; x++) {
            for (int y = 1; y <= WIDTH; y++) {
                st[x][y] = Boolean.FALSE;
            }
        }
    }

    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= WIDTH && y > 0 && y <= WIDTH);
    }

    /***
     * 从一个位置开始 递归查找与该棋子相邻的同色所有棋子
     * @param x 位置横坐标
     * @param y 位置纵坐标
     * @param color 黑棋或白棋
     */
    private void getGroupLength(int x, int y, int color) {
        for (int i = 0; i < 4; i ++ ) {
            int nx = x + dx[i], ny = y + dy[i];
            if (!isInBoard(nx, ny) || st[nx][ny]) continue;
            if (board[nx][ny] == EMPTY) {
                this.liberties ++;
                st[nx][ny] = Boolean.TRUE;
                continue;
            }
            if (board[nx][ny] != color) {
                st[nx][ny] = Boolean.TRUE;
                continue;
            }
            st[nx][ny] = Boolean.TRUE;
            this.length ++;
            add2Group(nx, ny);
            // 递归判断
            getGroupLength(nx, ny, color);
        }
    }

    // 从一个点开始 遍历从这个点延伸出去的组的长度和气
    public void getGroupLengthAndLiberty(int x, int y, int color) {
        reset();
        getGroupLength(x, y, color);
    }
}
