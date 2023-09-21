from typing import List

from constants import DX, DY, EMPTY
from point import Point


def is_in_board(x: int, y: int, size: int) -> bool:
    return 1 <= x <= size and 1 <= y <= size


class Group:
    def __init__(self, x: int, y: int, size: int):
        self.liberties = 0
        self.length = 1
        self.stones = set()
        self.st = [[False for _ in range(size + 1)] for _ in range(size + 1)]
        self.reset(size)
        self.add_to_group(x, y)

    def add_to_group(self, x: int, y: int):
        self.stones.add(Point(x, y))

    def reset(self, size: int):
        for x in range(1, size + 1):
            for y in range(1, size + 1):
                self.st[x][y] = False

    def get_group_length(self, x: int, y: int, color: int, size: int, board: List[List[int]]):
        for i in range(4):
            nx, ny = x + DX[i], y + DY[i]
            if not is_in_board(nx, ny, size) or self.st[nx][ny]:
                continue
            if board[nx][ny] == EMPTY:
                self.liberties += 1
                self.st[nx][ny] = True
                continue
            if board[nx][ny] != color:
                self.st[nx][ny] = True
                continue
            self.st[nx][ny] = True
            self.length += 1
            self.add_to_group(nx, ny)
            self.get_group_length(nx, ny, color, size, board)

    def get_group_length_and_liberty(self, x: int, y: int, color: int, size: int, board: List[List[int]]):
        self.reset(size)
        self.get_group_length(x, y, color, size, board)
