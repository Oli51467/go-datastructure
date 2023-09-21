// 定义常量
const EMPTY = 0;
const BLACK = 1;
const WHITE = 2;
const DX = [-1, 0, 1, 0];
const DY = [0, 1, 0, -1];

class Point {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    set(x, y) {
        this.x = x;
        this.y = y;
    }
}

class Group {
    constructor(x, y, size) {
        this.liberties = 0;
        this.length = 1;
        this.stones = new Set();
        this.st = Array.from({length: size + 1}, () => Array(size + 1).fill(false));
        this.reset(size);
        this.addToGroup(x, y);
    }

    addToGroup(x, y) {
        const point = new Point(x, y);
        this.stones.add(point);
    }

    reset(size) {
        for (let x = 1; x <= size; x++) {
            for (let y = 1; y <= size; y++) {
                this.st[x][y] = false;
            }
        }
    }

    isInBoard(x, y, size) {
        return x > 0 && x <= size && y > 0 && y <= size;
    }

    getGroupLength(x, y, color, size, board) {
        for (let i = 0; i < 4; i++) {
            const nx = x + DX[i];
            const ny = y + DY[i];
            if (!this.isInBoard(nx, ny, size) || this.st[nx][ny]) continue;
            if (board[nx][ny] === EMPTY) {
                this.liberties++;
                this.st[nx][ny] = true;
                continue;
            }
            if (board[nx][ny] !== color) {
                this.st[nx][ny] = true;
                continue;
            }
            this.st[nx][ny] = true;
            this.length++;
            this.addToGroup(nx, ny);
            this.getGroupLength(nx, ny, color, size, board);
        }
    }

    getGroupLengthAndLiberty(x, y, color, size, board) {
        this.reset(size);
        this.getGroupLength(x, y, color, size, board);
    }
}

class Board {
    constructor(size) {
        this.size = size;
        this.playCount = 0;
        this.board = Array.from({length: size + 2}, () => Array(size + 2).fill(EMPTY));
        this.st = Array.from({length: size + 2}, () => Array(size + 2).fill(false));
        this.blackForbidden = new Point(-1, -1);
        this.whiteForbidden = new Point(-1, -1);
        this.forbiddenList = [this.blackForbidden];
        this.gameRecord = [];
        this.steps = [this.blackForbidden];
        this.capturedStones = new Set();
        this.tmpCaptured = new Set();
        this.player = BLACK;

        // 初始化棋盘
        for (let x = 1; x <= this.size; x++) {
            for (let y = 1; y <= this.size; y++) {
                this.board[x][y] = EMPTY;
                this.st[x][y] = false;
            }
        }
        this.gameRecord.push(Array(size * size).fill(EMPTY).join(''));
    }

    getPlayCount() {
        return this.playCount;
    }

    getCapturedStones() {
        return this.capturedStones;
    }

    getBoard() {
        return this.board;
    }

    changePlayer() {
        this.player = this.player === BLACK ? WHITE : BLACK;
    }

    isInBoard(x, y) {
        return x > 0 && x <= this.size && y > 0 && y <= this.size;
    }

    reset() {
        for (let i = 1; i <= this.size; i++) {
            for (let j = 1; j <= this.size; j++) {
                this.st[i][j] = false;
            }
        }
    }

    getAllGroupsLengthAndLiberty(selfCount) {
        let count = 0;
        let countEat = 0;
        let koX = -1;
        let koY = -1;
        for (let x = 1; x <= this.size; x++) {
            for (let y = 1; y <= this.size; y++) {
                if (this.st[x][y] || this.board[x][y] === EMPTY) continue;
                this.st[x][y] = true;
                const group = new Group(x, y, this.size);
                group.getGroupLengthAndLiberty(x, y, this.board[x][y], this.size, this.board);
                for (const stone of group.stones) {
                    this.st[stone.x][stone.y] = true;
                }
                if (group.liberties === 0) {
                    countEat++;
                    this.tmpCaptured = new Set([...this.tmpCaptured, ...group.stones]);
                    for (const stone of group.stones) {
                        this.board[stone.x][stone.y] = EMPTY;
                    }
                    if (group.length === 1) {
                        count++;
                        for (const stone of group.stones) {
                            koX = stone.x;
                            koY = stone.y;
                        }
                    }
                }
            }
        }
        if (count === 1 && selfCount === 1) {
            if (this.player === BLACK) {
                this.whiteForbidden.set(koX, koY);
            } else if (this.player === WHITE) {
                this.blackForbidden.set(koX, koY);
            }
        }
        return countEat;
    }

    play(x, y) {
        if (x === null || y === null) return false;
        if (!this.isInBoard(x, y) || this.board[x][y] !== EMPTY) return false;
        if (this.player === BLACK && x === this.blackForbidden.x && y === this.blackForbidden.y) return false;
        if (this.player === WHITE && x === this.whiteForbidden.x && y === this.whiteForbidden.y) return false;
        this.board[x][y] = this.player;
        this.reset();
        const curGroup = new Group(x, y, this.size);
        curGroup.getGroupLengthAndLiberty(x, y, this.player, this.size, this.board);
        let selfCount = 0;
        for (const stone of curGroup.stones) {
            this.st[stone.x][stone.y] = true;
            selfCount++;
        }
        const eatOppoGroups = this.getAllGroupsLengthAndLiberty(selfCount);
        if (curGroup.liberties === 0 && eatOppoGroups === 0) {
            this.board[x][y] = EMPTY;
            return false;
        } else {
            if (this.player === WHITE) {
                this.whiteForbidden.set(-1, -1);
                this.forbiddenList.push(new Point(this.blackForbidden.x, this.blackForbidden.y));
            } else {
                this.blackForbidden.set(-1, -1);
                this.forbiddenList.push(new Point(this.whiteForbidden.x, this.whiteForbidden.y));
            }
            this.steps.push(new Point(x, y));
            this.playCount++;
            this.capturedStones.clear();
            this.capturedStones = new Set([...this.capturedStones, ...this.tmpCaptured]);
            this.tmpCaptured = new Set();
            this.changePlayer();
            this.saveState();
            return true;
        }
    }

    saveState() {
        let res = '';
        for (let i = 1; i <= this.size; i++) {
            for (let j = 1; j <= this.size; j++) {
                res += this.board[i][j];
            }
        }
        this.gameRecord.push(res);
    }

    regretPlay() {
        this.gameRecord.pop();
        this.steps.pop();
        this.forbiddenList.pop();
        const curState = this.gameRecord[this.gameRecord.length - 1] || '';
        if (!curState) return;
        for (let i = 1; i <= this.size; i++) {
            for (let j = 1; j <= this.size; j++) {
                this.board[i][j] = parseInt(curState[(i - 1) * this.size + j - 1]);
            }
        }
        const curForbidden = this.forbiddenList[this.forbiddenList.length - 1] || new Point(-1, -1);
        if (this.player === BLACK) {
            this.blackForbidden = curForbidden;
            this.whiteForbidden = new Point(-1, -1);
        } else {
            this.whiteForbidden = curForbidden;
            this.blackForbidden = new Point(-1, -1);
        }
        this.playCount--;
        this.changePlayer();
    }
}

// 示例用法
function main() {
    const board = new Board(19);
    const ok = board.play(4, 4);
    console.log("Can play:", ok);
    for (const row of board.getBoard()) {
        console.log(row);
    }
    console.log(board.getCapturedStones());
    board.regretPlay();
    for (const row of board.getBoard()) {
        console.log(row);
    }
    console.log(board.getPlayCount());
}

main();
