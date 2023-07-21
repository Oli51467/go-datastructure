package org.sdu.go;

import org.sdu.go.entity.Board;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Board board = new Board(19);
        boolean ok = board.play(4, 4);
        System.out.println("Can play:" + ok);
        System.out.println(Arrays.deepToString(board.getBoard()));
        System.out.println(board.getCapturedStones());
        board.regretPlay();
        System.out.println(Arrays.deepToString(board.getBoard()));
        System.out.println(board.getPlayCount());
    }
}