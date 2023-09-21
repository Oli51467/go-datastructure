package main

import (
	"errors"
	"fmt"
)

// 定义常量
const (
	EMPTY = 0
	BLACK = 1
	WHITE = 2
)

// Point 结构体
type Point struct {
	X, Y int
}

// NewPoint 创建一个新的 Point 实例
func NewPoint(x, y int) Point {
	return Point{X: x, Y: y}
}

// Group 结构体
type Group struct {
	Liberties int
	Length    int
	Stones    map[Point]bool
	st        [][]bool
}

// NewGroup 创建一个新的 Group 实例
func NewGroup(x, y, size int) *Group {
	group := &Group{
		Liberties: 0,
		Length:    1,
		Stones:    make(map[Point]bool),
		st:        make([][]bool, size+1),
	}

	for i := range group.st {
		group.st[i] = make([]bool, size+1)
	}

	group.reset(size)
	group.addToGroup(x, y)
	return group
}

// addToGroup 将点添加到组中
func (g *Group) addToGroup(x, y int) {
	point := NewPoint(x, y)
	g.Stones[point] = true
}

// reset 重置辅助数组
func (g *Group) reset(size int) {
	for x := 1; x <= size; x++ {
		for y := 1; y <= size; y++ {
			g.st[x][y] = false
		}
	}
}

// isInBoard 检查点是否在棋盘内
func (g *Group) isInBoard(x, y, size int) bool {
	return x > 0 && x <= size && y > 0 && y <= size
}

// getGroupLength 从一个位置开始递归查找与该棋子相邻的同色所有棋子
func (g *Group) getGroupLength(x, y, color, size int, board [][]int) {
	dx := []int{-1, 0, 1, 0}
	dy := []int{0, 1, 0, -1}

	for i := 0; i < 4; i++ {
		nx, ny := x+dx[i], y+dy[i]
		if !g.isInBoard(nx, ny, size) || g.st[nx][ny] {
			continue
		}
		if board[nx][ny] == EMPTY {
			g.Liberties++
			g.st[nx][ny] = true
			continue
		}
		if board[nx][ny] != color {
			g.st[nx][ny] = true
			continue
		}
		g.st[nx][ny] = true
		g.Length++
		g.addToGroup(nx, ny)
		g.getGroupLength(nx, ny, color, size, board)
	}
}

// getGroupLengthAndLiberty 从一个点开始遍历从这个点延伸出去的组的长度和气
func (g *Group) getGroupLengthAndLiberty(x, y, color, size int, board [][]int) {
	g.reset(size)
	g.getGroupLength(x, y, color, size, board)
}

// Board 结构体
type Board struct {
	size           int
	blackForbidden Point
	whiteForbidden Point
	st             [][]bool
	board          [][]int
	steps          []Point
	gameRecord     []string
	forbiddenList  []Point
	player         int
	playCount      int
	capturedStones map[Point]bool
	tmpCaptured    map[Point]bool
}

// NewBoard 创建一个新的 Board 实例
func NewBoard(size int) *Board {
	board := &Board{
		size:           size,
		blackForbidden: NewPoint(-1, -1),
		whiteForbidden: NewPoint(-1, -1),
		st:             make([][]bool, size+2),
		board:          make([][]int, size+2),
		steps:          make([]Point, 0),
		gameRecord:     make([]string, 0),
		forbiddenList:  make([]Point, 0),
		player:         BLACK,
		playCount:      0,
		capturedStones: make(map[Point]bool),
		tmpCaptured:    make(map[Point]bool),
	}

	for i := range board.st {
		board.st[i] = make([]bool, size+2)
	}

	for i := range board.board {
		board.board[i] = make([]int, size+2)
	}

	tmp := ""
	for x := 1; x <= size; x++ {
		for y := 1; y <= size; y++ {
			board.board[x][y] = EMPTY
			board.st[x][y] = false
			tmp += "0"
		}
	}
	board.gameRecord = append(board.gameRecord, tmp)
	return board
}

// ChangePlayer 切换落子方
func (b *Board) ChangePlayer() {
	if b.player == BLACK {
		b.player = WHITE
	} else {
		b.player = BLACK
	}
}

// IsInBoard 判断一步落子是否在棋盘内
func (b *Board) IsInBoard(x, y int) bool {
	return x > 0 && x <= b.size && y > 0 && y <= b.size
}

// Reset 重置辅助数组
func (b *Board) Reset() {
	for x := 1; x <= b.size; x++ {
		for y := 1; y <= b.size; y++ {
			b.st[x][y] = false
		}
	}
}

// GetAllGroupsLengthAndLiberty 得到某一步落下后对方被吃的棋子的数量
func (b *Board) GetAllGroupsLengthAndLiberty(selfCount int) int {
	count := 0
	countEat := 0
	koX := -1
	koY := -1

	dx := []int{-1, 0, 1, 0}
	dy := []int{0, 1, 0, -1}

	for x := 1; x <= b.size; x++ {
		for y := 1; y <= b.size; y++ {
			if b.st[x][y] || b.board[x][y] == EMPTY {
				continue
			}
			b.st[x][y] = true
			group := NewGroup(x, y, b.size)
			group.getGroupLengthAndLiberty(x, y, b.board[x][y], b.size, b.board)

			for point := range group.Stones {
				b.st[point.X][point.Y] = true
			}

			if group.Liberties == 0 {
				countEat++
				for point := range group.Stones {
					b.tmpCaptured[point] = true
					b.board[point.X][point.Y] = EMPTY
				}
				if group.Length == 1 {
					count++
					for point := range group.Stones {
						koX = point.X
						koY = point.Y
					}
				}
			}
		}
	}

	if count == 1 && selfCount == 1 {
		if b.player == BLACK {
			b.whiteForbidden = NewPoint(koX, koY)
		} else if b.player == WHITE {
			b.blackForbidden = NewPoint(koX, koY)
		}
	}

	return countEat
}

// Play 落子方法
func (b *Board) Play(x, y int) (bool, error) {
	if x < 0 || y < 0 || x > b.size || y > b.size {
		return false, errors.New("坐标超出范围")
	}

	if b.board[x][y] != EMPTY {
		return false, errors.New("该位置已经有棋子")
	}

	if (b.player == BLACK && b.blackForbidden.X == x && b.blackForbidden.Y == y) ||
		(b.player == WHITE && b.whiteForbidden.X == x && b.whiteForbidden.Y == y) {
		return false, errors.New("禁入点位置非法")
	}

	b.board[x][y] = b.player
	b.Reset()
	curGroup := NewGroup(x, y, b.size)
	curGroup.getGroupLengthAndLiberty(x, y, b.player, b.size, b.board)
	selfCount := 0

	for point := range curGroup.Stones {
		b.st[point.X][point.Y] = true
		selfCount++
	}

	eatOppoGroups := b.GetAllGroupsLengthAndLiberty(selfCount)

	if curGroup.Liberties == 0 && eatOppoGroups == 0 {
		b.board[x][y] = EMPTY
		return false, errors.New("自杀，落子无效")
	}

	if b.player == WHITE {
		b.whiteForbidden = NewPoint(-1, -1)
		b.forbiddenList = append(b.forbiddenList, NewPoint(b.blackForbidden.X, b.blackForbidden.Y))
	} else {
		b.blackForbidden = NewPoint(-1, -1)
		b.forbiddenList = append(b.forbiddenList, NewPoint(b.whiteForbidden.X, b.whiteForbidden.Y))
	}

	b.steps = append(b.steps, NewPoint(x, y))
	b.playCount++
	b.capturedStones = make(map[Point]bool)
	for point := range b.tmpCaptured {
		b.capturedStones[point] = true
	}
	b.tmpCaptured = make(map[Point]bool)
	b.ChangePlayer()
	b.saveState()
	return true, nil
}

// SaveState 保存当前棋盘状态
func (b *Board) saveState() {
	var res string
	for x := 1; x <= b.size; x++ {
		for y := 1; y <= b.size; y++ {
			res += fmt.Sprint(b.board[x][y])
		}
	}
	b.gameRecord = append(b.gameRecord, res)
}

// RegretPlay 悔棋方法
func (b *Board) RegretPlay() {
	if len(b.gameRecord) <= 1 {
		return
	}

	b.gameRecord = b.gameRecord[:len(b.gameRecord)-1]
	b.steps = b.steps[:len(b.steps)-1]
	b.forbiddenList = b.forbiddenList[:len(b.forbiddenList)-1]
	curState := b.gameRecord[len(b.gameRecord)-1]
	var curForbidden Point
	if b.player == BLACK {
		curForbidden = b.forbiddenList[len(b.forbiddenList)-1]
		b.blackForbidden = curForbidden
		b.whiteForbidden = NewPoint(-1, -1)
	} else {
		curForbidden = b.forbiddenList[len(b.forbiddenList)-1]
		b.whiteForbidden = curForbidden
		b.blackForbidden = NewPoint(-1, -1)
	}

	b.playCount--
	b.ChangePlayer()

	for x := 1; x <= b.size; x++ {
		for y := 1; y <= b.size; y++ {
			b.board[x][y] = int(curState[(x-1)+(y-1)*b.size]) - '0'
		}
	}
}

// GetCapturedStones 获取被吃子的集合
func (b *Board) GetCapturedStones() []Point {
	var captured []Point
	for point := range b.capturedStones {
		captured = append(captured, point)
	}
	return captured
}

// GetBoard 获取棋盘状态
func (b *Board) GetBoard() [][]int {
	boardCopy := make([][]int, b.size+2)
	for i := range boardCopy {
		boardCopy[i] = make([]int, b.size+2)
		copy(boardCopy[i], b.board[i])
	}
	return boardCopy
}

// GetPlayCount 获取当前回合数
func (b *Board) GetPlayCount() int {
	return b.playCount
}

func main() {
	board := NewBoard(19)
	ok, err := board.Play(4, 4)
	if err != nil {
		fmt.Println("Error:", err)
	} else {
		fmt.Println("Can play:", ok)
		fmt.Println(board.GetBoard())
		fmt.Println(board.GetCapturedStones())
		board.RegretPlay()
		fmt.Println(board.GetBoard())
		fmt.Println(board.GetPlayCount())
	}
}