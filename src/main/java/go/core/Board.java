package go.core;

import java.util.HashSet;
import java.util.Set;

import static go.core.common.Constants.*;

// 棋盘
public class Board {

    private final int size;
    private final Point blackForbidden;
    private final Point whiteForbidden;
    private final Boolean[][] st;

    public static Integer[][] board;

    private int player, playCount;
    public Set<Point> capturedStones, tmpCaptured;
    public StringBuilder sgfRecord;

    public Board(int size, int handicap) {
        this.size = size;
        this.playCount = 0;
        board = new Integer[this.size + 1][this.size + 1];
        st = new Boolean[this.size + 1][this.size + 1];
        blackForbidden = new Point(-1, -1);
        whiteForbidden = new Point(-1, -1);
        capturedStones = new HashSet<>();
        tmpCaptured = new HashSet<>();
        // 初始化棋盘
        for (int x = 1; x <= this.size; x++) {
            for (int y = 1; y <= this.size; y++) {
                board[x][y] = EMPTY;
                st[x][y] = Boolean.FALSE;
            }
        }
        if (handicap == 0) player = BLACK;
        else player = WHITE;
    }

    /**
     * 当一方落子有效后 切换落子方
     */
    private void changePlayer() {
        player = player == BLACK ? WHITE : BLACK;
    }

    /**
     * 判断一步落子是否在棋盘内
     * @param x 横坐标
     * @param y 纵坐标
     * @return 是否在棋盘内
     */
    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= size && y > 0 && y <= size);
    }

    /**
     * 重置辅助数组
     */
    private void reset() {
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= this.size; j++) {
                st[i][j] = Boolean.FALSE;
            }
        }
    }

    /**
     * 得到某一步落下后 对方被吃的棋子的数量
     * @param selfCount 某一步连带的棋子的长度 如果长度为1并且判定被吃 则为可能是打劫
     * @return 对方被吃的棋子的数量
     */
    private int getAllGroupsLengthAndLiberty(int selfCount) {
        // countEat为吃掉别人组的数量
        int count = 0, countEat = 0, koX = -1, koY = -1;
        for (int x = 1; x <= this.size; x++) {
            for (int y = 1; y <= this.size; y++) {
                if (st[x][y] || board[x][y] == EMPTY) continue;
                st[x][y] = Boolean.TRUE;
                // 这里的（x, y）一定是一个新的group
                Group group = new Group(x, y, size);
                group.getGroupLengthAndLiberty(x, y, board[x][y], size);
                // 一次性把该组都加入到辅助数组中
                for (Point stone : group.stones) {
                    st[stone.getX()][stone.getY()] = Boolean.TRUE;
                }
                // 这里只可能是对方没气
                if (group.getLiberties() == 0) {
                    countEat++;
                    // 把死子移除
                    tmpCaptured.addAll(group.stones);
                    for (Point stone : group.stones) {
                        board[stone.getX()][stone.getY()] = EMPTY;
                    }
                    if (group.getLength() == 1) {
                        count++;
                        for (Point stone : group.stones) {
                            koX = stone.getX();
                            koY = stone.getY();
                        }
                    }
                }
            }
        }
        // 该局部形成打劫，更新禁入点的位置即为被提吃的位置
        if (count == 1 && selfCount == 1) {
            if (player == BLACK) whiteForbidden.set(koX, koY);
            else if (player == WHITE) blackForbidden.set(koX, koY);
        }
        return countEat;
    }

    public boolean play(int x, int y, int side) {
        if (!isInBoard(x, y) || board[x][y] != EMPTY) return false; // 如果落子棋盘外或者落在已有棋子的位置 非法
        if (side != player) return false;
        if (player == BLACK && blackForbidden.getX() == x && blackForbidden.getX() == y) return false;  // 如果黑棋落在禁入点 非法
        if (player == WHITE && whiteForbidden.getX() == x && whiteForbidden.getY() == y) return false;  // 如果白棋落在禁入点 非法
        board[x][y] = player;
        reset();
        // 记录组的长度
        int selfCount = 0;
        Group curGroup = new Group(x, y, size);
        curGroup.getGroupLengthAndLiberty(x, y, player, size);
        for (Point stone : curGroup.stones) {
            st[stone.getX()][stone.getY()] = Boolean.TRUE;
            selfCount ++;
        }
        int eatOppoGroups = getAllGroupsLengthAndLiberty(selfCount);    // 查找该步落下后，对手是否被吃
        // 如果自己没气了 并且也没有吃掉对方 则是自杀 落子无效
        if (curGroup.getLiberties() == 0 && eatOppoGroups == 0) {
            board[x][y] = EMPTY;
            return false;
        } else {
            if (player == WHITE) whiteForbidden.set(-1, -1);
            else blackForbidden.set(-1, -1);
            if (player == BLACK) sgfRecord.append('B');
            else sgfRecord.append('W');
            sgfRecord.append('[').append(x).append(',').append(y).append(']');
            changePlayer();
            playCount ++;
            capturedStones.clear();
            capturedStones.addAll(tmpCaptured);
            tmpCaptured.clear();
            return true;
        }
    }
}
