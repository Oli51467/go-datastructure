from typing import List, Set

from constants import EMPTY, BLACK, WHITE
from group import Group
from point import Point


def is_in_board(x: int, y: int, size: int) -> bool:
    return 1 <= x <= size and 1 <= y <= size


class Board:
    def __init__(self, size: int):
        self.size = size
        self.play_count = 0
        self.board = [[EMPTY for _ in range(size + 2)] for _ in range(size + 2)]
        self.st = [[False for _ in range(size + 2)] for _ in range(size + 2)]
        self.black_forbidden = Point(-1, -1)
        self.white_forbidden = Point(-1, -1)
        self.forbidden_list = [self.black_forbidden]  # 每一步走完后对方的禁入点
        self.game_record = []  # 每一步棋走完后的局面
        self.steps = [self.black_forbidden]  # 记录每一步
        self.captured_stones = set()  # 吃子集合
        self.tmp_captured = set()
        # 初始化棋盘
        tmp = []
        for x in range(1, self.size + 1):
            for y in range(1, self.size + 1):
                self.board[x][y] = EMPTY
                self.st[x][y] = False
                tmp.append(EMPTY)
        self.game_record.append("".join(map(str, tmp)))
        self.player = BLACK

    def get_play_count(self) -> int:
        return self.play_count

    def get_captured_stones(self) -> Set[Point]:
        return self.captured_stones

    def get_board(self) -> List[List[int]]:
        return self.board

    def change_player(self):
        self.player = WHITE if self.player == BLACK else BLACK

    def is_in_board(self, x: int, y: int) -> bool:
        return 1 <= x <= self.size and 1 <= y <= self.size

    def reset(self):
        for i in range(1, self.size + 1):
            for j in range(1, self.size + 1):
                self.st[i][j] = False

    def get_all_groups_length_and_liberty(self, self_count: int) -> int:
        count = 0
        count_eat = 0
        ko_x = -1
        ko_y = -1
        for x in range(1, self.size + 1):
            for y in range(1, self.size + 1):
                if self.st[x][y] or self.board[x][y] == EMPTY:
                    continue
                self.st[x][y] = True
                group = Group(x, y, self.size)
                group.get_group_length_and_liberty(x, y, self.board[x][y], self.size, self.board)
                for stone in group.stones:
                    self.st[stone.x][stone.y] = True
                if group.liberties == 0:
                    count_eat += 1
                    self.tmp_captured.update(group.stones)
                    for stone in group.stones:
                        self.board[stone.x][stone.y] = EMPTY
                    if group.length == 1:
                        count += 1
                        if group.stones:
                            first_stone = list(group.stones)[0]  # 转换为列表后取第一个元素
                            ko_x = first_stone.x
                            ko_y = first_stone.y
                        else:
                            ko_x = -1
                            ko_y = -1
        if count == 1 and self_count == 1:
            if self.player == BLACK:
                self.white_forbidden.x = ko_x
                self.white_forbidden.y = ko_y
            elif self.player == WHITE:
                self.black_forbidden.x = ko_x
                self.black_forbidden.y = ko_y
        return count_eat

    def play(self, x: int, y: int) -> bool:
        if x is None or y is None:
            return False
        if not self.is_in_board(x, y) or self.board[x][y] != EMPTY:
            return False
        if self.player == BLACK and (x == self.black_forbidden.x and y == self.black_forbidden.y):
            return False
        if self.player == WHITE and (x == self.white_forbidden.x and y == self.white_forbidden.y):
            return False
        self.board[x][y] = self.player
        self.reset()
        cur_group = Group(x, y, self.size)
        cur_group.get_group_length_and_liberty(x, y, self.player, self.size, self.board)
        self_count = 0
        for stone in cur_group.stones:
            self.st[stone.x][stone.y] = True
            self_count += 1
        eat_oppo_groups = self.get_all_groups_length_and_liberty(self_count)
        if cur_group.liberties == 0 and eat_oppo_groups == 0:
            self.board[x][y] = EMPTY
            return False
        else:
            if self.player == WHITE:
                self.white_forbidden.x = -1
                self.white_forbidden.y = -1
                self.forbidden_list.append(Point(self.black_forbidden.x, self.black_forbidden.y))
            else:
                self.black_forbidden.x = -1
                self.black_forbidden.y = -1
                self.forbidden_list.append(Point(self.white_forbidden.x, self.white_forbidden.y))
            self.steps.append(Point(x, y))
            self.play_count += 1
            self.captured_stones.clear()
            self.captured_stones.update(self.tmp_captured)
            self.tmp_captured.clear()
            self.change_player()
            self.save_state()
            return True

    def save_state(self):
        res = ""
        for i in range(1, self.size + 1):
            for j in range(1, self.size + 1):
                res += str(self.board[i][j])
        self.game_record.append(res)

    def regret_play(self):
        self.game_record.pop()
        self.steps.pop()
        self.forbidden_list.pop()
        cur_state = self.game_record[-1] if self.game_record else ""
        if not cur_state:
            return
        for i in range(1, self.size + 1):
            for j in range(1, self.size + 1):
                self.board[i][j] = int(cur_state[(i - 1) * self.size + j - 1])
        cur_forbidden = self.forbidden_list[-1] if self.forbidden_list else Point(-1, -1)
        if self.player == BLACK:
            self.black_forbidden = cur_forbidden
            self.white_forbidden = Point(-1, -1)
        else:
            self.white_forbidden = cur_forbidden
            self.black_forbidden = Point(-1, -1)
        self.play_count -= 1
        self.change_player()
