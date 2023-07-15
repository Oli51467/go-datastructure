package com.sdu.go.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point {
    private int x;
    private int y;

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
