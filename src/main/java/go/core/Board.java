package go.core;

// 棋盘
public class Board {
    public final static int EMPTY = 0;
    public final static int BLACK = 1;
    public final static int WHITE = 2;

    private final int height;
    private final int width;
    public static Integer[][] board;
    public final static int[] dx = {-1, 0, 1, 0};
    public final static int[] dy = {0, 1, 0, -1};
    private boolean[][] st;
    private int player;
    public StringBuilder sgfRecord;
    private Point blackForbidden;
    private Point whiteForbidden;

    public Board(int width, int height, int handicap) {
        this.width = width;
        this.height = height;
        board = new Integer[width + 1][height + 1];
        st = new boolean[this.width + 1][this.height + 1];
        blackForbidden = new Point(-1, -1);
        whiteForbidden = new Point(-1, -1);
        // 初始化棋盘
        for (int x = 1; x <= this.width; x++) {
            for (int y = 1; y <= this.height; y++) {
                board[x][y] = EMPTY;
                st[x][y] = false;
            }
        }
        if (handicap == 0) player = BLACK;
        else player = WHITE;
        // 如果有让子 记录让子
        for (int x = 4; x <= 16; x += 6) {
            for (int y = 4; y <= 16; y += 6) {
                if (handicap != 0) {
                    board[x][y] = BLACK;
                    handicap--;
                }
            }
        }
    }

    /**
     * 当一方落子有效后 切换落子方
     */
    private void changePlayer() {
        if (player == BLACK) player = WHITE;
        else player = BLACK;
    }

    /**
     * 判断一步落子是否在棋盘内
     * @param x 横坐标
     * @param y 纵坐标
     * @return 是否在棋盘内
     */
    public boolean isInBoard(int x, int y) {
        return (x > 0 && x <= width && y > 0 && y <= height);
    }

    /**
     * 重置辅助数组
     */
    private void reset() {
        for (int i = 1; i <= this.height; i++) {
            for (int j = 1; j <= this.width; j++) {
                st[i][j] = false;
            }
        }
    }

    private int getAllGroupsLengthAndLiberty(int selfCount) {
        // countEat为吃掉别人组的数量
        int count = 0, countEat = 0;
        int koX = -1, koY = -1;
        for (int x = 1; x <= this.width; x++) {
            for (int y = 1; y <= this.height; y++) {
                if (st[x][y] || board[x][y] == EMPTY) continue;
                st[x][y] = true;
                // 这里的（x, y）一定是一个新的group
                Group group = new Group(x, y, board[x][y]);
                group.getGroupLengthAndLiberty(x, y, board[x][y]);
                // 一次性把该组都加入到辅助数组中
                for (Point stone : group.stones) {
                    st[stone.getX()][stone.getY()] = true;
                }
                // 这里只可能是对方没气
                if (group.getLiberties() == 0) {
                    countEat++;
                    // 把死子移除
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
            if (player == BLACK) {
                whiteForbidden.setX(koX);
                whiteForbidden.setY(koY);
            } else if (player == WHITE) {
                blackForbidden.setX(koX);
                blackForbidden.setY(koY);
            }
        }
        return countEat;
    }

    public boolean play(int x, int y) {
        if (!isInBoard(x, y) || board[x][y] != EMPTY) return false;
        if (player == BLACK) {
            if (blackForbidden.getX() == x && blackForbidden.getX() == y) {
                return false;
            }
        } else if (player == WHITE) {
            if (whiteForbidden.getX() == x && whiteForbidden.getY() == y) {
                return false;
            }
        }
        board[x][y] = player;
        reset();
        // 记录组的长度
        int selfCount = 0;
        Group curGroup = new Group(x, y, player);
        curGroup.getGroupLengthAndLiberty(x, y, player);
        for (Point stone : curGroup.stones) {
            st[stone.getX()][stone.getY()] = true;
            selfCount ++;
        }
        int eatOppoGroups = getAllGroupsLengthAndLiberty(selfCount);
        // 如果自己没气了 并且也没有吃掉对方 则是自杀 落子无效
        if (curGroup.getLiberties() == 0 && eatOppoGroups == 0) {
            board[x][y] = EMPTY;
            return false;
        } else {
            if (player == WHITE) {
                whiteForbidden.setX(-1);
                whiteForbidden.setY(-1);
            } else {
                blackForbidden.setX(-1);
                blackForbidden.setY(-1);
            }
            if (player == BLACK) {
                sgfRecord.append('B');
            } else {
                sgfRecord.append('W');
            }
            sgfRecord.append('[').append(x).append(',').append(y).append(']');
            changePlayer();
            return true;
        }
    }

    public String getSgf() {
        return sgfRecord.toString();
    }

}
