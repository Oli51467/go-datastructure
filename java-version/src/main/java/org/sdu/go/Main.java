package org.sdu.go;

import org.sdu.go.entity.GoBoard;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        GoBoard goBoard = new GoBoard(19, 0);
        boolean ok = goBoard.play(4, 4);
        System.out.println("Can play:" + ok);
        System.out.println(Arrays.deepToString(goBoard.getBoard()));
        System.out.println(goBoard.getCapturedStones());
        goBoard.regretPlay();
        System.out.println(Arrays.deepToString(goBoard.getBoard()));
        System.out.println(goBoard.getPlayCount());
    }
}