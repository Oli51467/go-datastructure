package org.sdu.go.entity;

public interface Board {

    void changePlayer();

    boolean play(Integer x, Integer y);

    boolean isInBoard(int x, int y);

    void saveState();

    void regretPlay();
}
