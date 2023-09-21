# 示例用法
from board import Board


def main():
    board = Board(19)
    board.play(4, 4)
    board.play(4, 5)
    board.play(3, 5)
    board.play(3, 4)
    board.play(3, 3)
    board.play(4, 3)
    board.play(2, 4)
    board.play(5, 4)
    board.play(5, 5)
    board.play(3, 4)
    ok = board.play(4, 4)
    print(ok)
    for row in board.get_board():
        print(row)
    print(board.get_play_count())


if __name__ == "__main__":
    main()