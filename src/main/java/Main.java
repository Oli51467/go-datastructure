import go.core.Board;
import go.core.Player;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        newGame(19, 0);
    }

    public static void newGame(int size, int handicap) {
        Board board = new Board(size, size, handicap);
        System.out.println("1代表黑棋 2代表白棋 输入999后退出" + "\n" + "坐标x为列 坐标y为行" + "\n" + "该黑棋下啦");
        while(true) {
            System.out.println("请输入x:");
            Scanner sc = new Scanner(System.in);
            int x = sc.nextInt();
            if (x == 999) break;
            System.out.println("请输入y:");
            sc = new Scanner(System.in);
            int y = sc.nextInt();
            Player player = board.getPlayer();
            if(board.play(x, y, player)) {
                System.out.println("正常落子！" + '\n');
                board.nextPlayer();
            }
            else System.out.println("这里不可以落子！" + '\n');
            System.out.printf(board.toString());
        }
    }
}
