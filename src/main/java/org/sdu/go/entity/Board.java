package org.sdu.go.entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static org.sdu.go.common.Constants.*;

// 棋盘
public class Board {

    private final int size;
    private Point blackForbidden;
    private Point whiteForbidden;
    private boolean[][] st;
    private int[][] board;
    public Deque<Point> steps;
    public Deque<String> gameRecord;
    public Deque<Point> forbiddenList;

    private int player, playCount;
    private Set<Point> capturedStones, tmpCaptured;

    public Board(int size) {
        this.size = size;
        this.playCount = 0;
        board = new int[this.size + 2][this.size + 2];
        st = new boolean[this.size + 2][this.size + 2];
        blackForbidden = new Point(-1, -1);
        whiteForbidden = new Point(-1, -1);
        forbiddenList = new ArrayDeque<>();      // 每一步走完后对方的禁入点
        forbiddenList.push(blackForbidden); // 初始黑棋没有打劫禁入点
        gameRecord = new ArrayDeque<>();         // 每一步棋走完后的局面
        steps = new ArrayDeque<>();              // 记录每一步
        steps.push(blackForbidden);         // 初始为空
        capturedStones = new HashSet<>();   // 吃子集合
        tmpCaptured = new HashSet<>();
        // 初始化棋盘
        StringBuilder tmp = new StringBuilder();
        for (int x = 1; x <= this.size; x++) {
            for (int y = 1; y <= this.size; y++) {
                board[x][y] = EMPTY;
                st[x][y] = Boolean.FALSE;
                tmp.append(EMPTY);
            }
        }
        gameRecord.push(tmp.toString());
        player = BLACK;
    }

    public int getPlayCount() {
        return playCount;
    }

    public Set<Point> getCapturedStones() {
        return capturedStones;
    }

    public int[][] getBoard() {
        return board;
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
                group.getGroupLengthAndLiberty(x, y, board[x][y], size, board);
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

    public boolean play(Integer x, Integer y) {
        if (null == x || null == y) return false;
        if (!isInBoard(x, y) || board[x][y] != EMPTY) return false; // 如果落子棋盘外或者落在已有棋子的位置 非法
        if (player == BLACK && blackForbidden.getX() == x && blackForbidden.getX() == y) return false;  // 如果黑棋落在禁入点 非法
        if (player == WHITE && whiteForbidden.getX() == x && whiteForbidden.getY() == y) return false;  // 如果白棋落在禁入点 非法
        board[x][y] = player;
        reset();
        Group curGroup = new Group(x, y, size);
        curGroup.getGroupLengthAndLiberty(x, y, player, size, board);
        // 记录组的长度
        int selfCount = 0;
        for (Point stone : curGroup.stones) {
            st[stone.getX()][stone.getY()] = true;
            selfCount ++;
        }
        int eatOppoGroups = getAllGroupsLengthAndLiberty(selfCount);    // 查找该步落下后，对手是否被吃
        // 如果自己没气了 并且也没有吃掉对方 则是自杀 落子无效
        if (curGroup.getLiberties() == 0 && eatOppoGroups == 0) {
            board[x][y] = EMPTY;
            return false;
        } else {
            if (player == WHITE) {
                whiteForbidden.set(-1, -1);
                forbiddenList.push(new Point(blackForbidden.getX(), blackForbidden.getY()));  // 这里一定要new新的 否则传进去的值会被修改
            }
            else {
                blackForbidden.set(-1, -1);
                forbiddenList.push(new Point(whiteForbidden.getX(), whiteForbidden.getY()));
            }
            steps.push(new Point(x, y));
            playCount ++;
            capturedStones.clear();
            capturedStones.addAll(tmpCaptured);
            tmpCaptured.clear();
            changePlayer();
            saveState();
            return true;
        }
    }

    private void saveState() {
        StringBuilder res = new StringBuilder();
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= this.size; j++) {
                res.append(board[i][j]);
            }
        }
        gameRecord.push(res.toString());
    }

    /**
     * 悔棋方法
     */
    public void regretPlay() {
        this.gameRecord.pop();
        this.steps.pop();
        this.forbiddenList.pop();
        // 1. 恢复棋盘状态
        String curState = gameRecord.peek();
        if (null == curState || curState.equals("")) return;
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= this.size; j++) {
                board[i][j] = Integer.parseInt(curState.substring((i - 1) * this.size + j - 1, (i - 1) * this.size + j));
            }
        }
        // 2.恢复禁入点
        Point curForbidden = this.forbiddenList.peek();     // 存的是自己的禁入点
        if (player == BLACK) {
            this.blackForbidden = curForbidden;
            this.whiteForbidden = new Point(-1, -1);
        } else {
            this.whiteForbidden = curForbidden;
            this.blackForbidden = new Point(-1, -1);
        }
        // 3. 还原落子方
        playCount --;
        changePlayer();
    }
}
