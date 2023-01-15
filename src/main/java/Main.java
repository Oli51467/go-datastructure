import go.core.Board;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Board board = new Board(19, 19, 0);
        while(true) {
            System.out.println("请输入x:");
            Scanner sc = new Scanner(System.in);
            int x = sc.nextInt();
            if (x == 999) break;
            System.out.println("请输入y:");
            sc = new Scanner(System.in);
            int y = sc.nextInt();
            if(board.play(x, y)) {
                System.out.println("正常落子！" + '\n');
            }
            else System.out.println("这里不可以落子！" + '\n');
            board.to2String();
        }
    }
}
