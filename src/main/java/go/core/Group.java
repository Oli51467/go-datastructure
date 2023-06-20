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

    public Group(int x, int y, int size) {
        this.liberties = 0;
        this.length = 1;
        stones = new HashSet<>();
        st = new Boolean[size + 1][size + 1];
        reset(size);
        add2Group(x, y);
    }

    private void add2Group(int x, int y) {
        Point point = new Point(x, y);
        stones.add(point);
    }

    private void reset(int size) {
        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                st[x][y] = Boolean.FALSE;
            }
        }
    }

    public boolean isInBoard(int x, int y, int size) {
        return (x > 0 && x <= size && y > 0 && y <= size);
    }

    /***
     * 从一个位置开始 递归查找与该棋子相邻的同色所有棋子
     * @param x 位置横坐标
     * @param y 位置纵坐标
     * @param color 黑棋或白棋
     */
    private void getGroupLength(int x, int y, int color, int size) {
        for (int i = 0; i < 4; i ++ ) {
            int nx = x + dx[i], ny = y + dy[i];
            if (!isInBoard(nx, ny, size) || st[nx][ny]) continue;
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
            getGroupLength(nx, ny, color, size);
        }
    }

    // 从一个点开始 遍历从这个点延伸出去的组的长度和气
    public void getGroupLengthAndLiberty(int x, int y, int color, int size) {
        reset(size);
        getGroupLength(x, y, color, size);
    }
}
