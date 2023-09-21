use std::collections::{HashSet, VecDeque};
use std::fmt;

// 定义常量
const EMPTY: i32 = 0;
const BLACK: i32 = 1;
const WHITE: i32 = 2;

// 定义Point结构体
#[derive(Clone, Copy, PartialEq, Eq, Hash)]
struct Point {
    x: i32,
    y: i32,
}

impl Point {
    fn new(x: i32, y: i32) -> Self {
        Point { x, y }
    }
}

impl fmt::Debug for Point {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "({}, {})", self.x, self.y)
    }
}

// 定义Group结构体
struct Group {
    liberties: i32,
    length: i32,
    stones: HashSet<Point>,
    st: Vec<Vec<bool>>,
}

impl Group {
    fn new(x: i32, y: i32, size: i32) -> Self {
        let mut st = vec![vec![false; (size + 1) as usize]; (size + 1) as usize];
        Group {
            liberties: 0,
            length: 1,
            stones: vec![Point::new(x, y)].into_iter().collect(),
            st,
        }
    }

    fn add_to_group(&mut self, x: i32, y: i32) {
        let point = Point::new(x, y);
        self.stones.insert(point);
    }

    fn reset(&mut self, size: i32) {
        for x in 1..=size {
            for y in 1..=size {
                self.st[x as usize][y as usize] = false;
            }
        }
    }

    fn is_in_board(&self, x: i32, y: i32, size: i32) -> bool {
        x > 0 && x <= size && y > 0 && y <= size
    }

    fn get_group_length(&mut self, x: i32, y: i32, color: i32, size: i32, board: &Vec<Vec<i32>>) {
        for i in 0..4 {
            let nx = x + dx[i];
            let ny = y + dy[i];
            if !self.is_in_board(nx, ny, size) || self.st[nx as usize][ny as usize] {
                continue;
            }
            if board[nx as usize][ny as usize] == EMPTY {
                self.liberties += 1;
                self.st[nx as usize][ny as usize] = true;
                continue;
            }
            if board[nx as usize][ny as usize] != color {
                self.st[nx as usize][ny as usize] = true;
                continue;
            }
            self.st[nx as usize][ny as usize] = true;
            self.length += 1;
            self.add_to_group(nx, ny);
            self.get_group_length(nx, ny, color, size, board);
        }
    }

    fn get_group_length_and_liberty(
        &mut self,
        x: i32,
        y: i32,
        color: i32,
        size: i32,
        board: &Vec<Vec<i32>>,
    ) {
        self.reset(size);
        self.get_group_length(x, y, color, size, board);
    }
}

// 定义Board结构体
struct Board {
    size: i32,
    black_forbidden: Point,
    white_forbidden: Point,
    st: Vec<Vec<bool>>,
    board: Vec<Vec<i32>>,
    steps: VecDeque<Point>,
    game_record: Vec<String>,
    forbidden_list: VecDeque<Point>,
    player: i32,
    play_count: i32,
    captured_stones: HashSet<Point>,
    tmp_captured: HashSet<Point>,
}

impl Board {
    fn new(size: i32) -> Self {
        let mut board = vec![vec![EMPTY; (size + 2) as usize]; (size + 2) as usize];
        let mut st = vec![vec![false; (size + 2) as usize]; (size + 2) as usize];

        for x in 0..size + 2 {
            for y in 0..size + 2 {
                board[x as usize][y as usize] = EMPTY;
                st[x as usize][y as usize] = false;
            }
        }

        let black_forbidden = Point::new(-1, -1);
        let white_forbidden = Point::new(-1, -1);
        let mut forbidden_list = VecDeque::new();
        forbidden_list.push_back(black_forbidden.clone());

        let mut game_record = Vec::new();
        game_record.push("0".repeat((size * size) as usize));

        Board {
            size,
            black_forbidden,
            white_forbidden,
            st,
            board,
            steps: VecDeque::new(),
            game_record,
            forbidden_list,
            player: BLACK,
            play_count: 0,
            captured_stones: HashSet::new(),
            tmp_captured: HashSet::new(),
        }
    }

    fn change_player(&mut self) {
        self.player = if self.player == BLACK { WHITE } else { BLACK };
    }

    fn is_in_board(&self, x: i32, y: i32) -> bool {
        x > 0 && x <= self.size && y > 0 && y <= self.size
    }

    fn reset(&mut self) {
        for x in 1..=self.size {
            for y in 1..=self.size {
                self.st[x as usize][y as usize] = false;
            }
        }
    }

    fn get_all_groups_length_and_liberty(&mut self, self_count: i32) -> i32 {
        let mut count = 0;
        let mut count_eat = 0;
        let mut ko_x = -1;
        let mut ko_y = -1;

        for x in 1..=self.size {
            for y in 1..=self.size {
                if self.st[x as usize][y as usize] || self.board[x as usize][y as usize] == EMPTY {
                    continue;
                }
                self.st[x as usize][y as usize] = true;
                let mut group = Group::new(x, y, self.size);
                group.get_group_length_and_liberty(x, y, self.board[x as usize][y as usize], self.size, &self.board);

                for stone in group.stones.iter() {
                    self.st[stone.x as usize][stone.y as usize] = true;
                }

                if group.liberties == 0 {
                    count_eat += 1;
                    self.tmp_captured.extend(group.stones.clone());

                    for stone in group.stones.iter() {
                        self.board[stone.x as usize][stone.y as usize] = EMPTY;
                    }

                    if group.length == 1 {
                        count += 1;
                        for stone in group.stones.iter() {
                            ko_x = stone.x;
                            ko_y = stone.y;
                        }
                    }
                }
            }
        }

        if count == 1 && self_count == 1 {
            if self.player == BLACK {
                self.white_forbidden = Point::new(ko_x, ko_y);
            } else if self.player == WHITE {
                self.black_forbidden = Point::new(ko_x, ko_y);
            }
        }

        count_eat
    }

    fn play(&mut self, x: i32, y: i32) -> bool {
        if !self.is_in_board(x, y) || self.board[x as usize][y as usize] != EMPTY {
            return false;
        }

        if self.player == BLACK && self.black_forbidden.x == x && self.black_forbidden.y == y {
            return false;
        }

        if self.player == WHITE && self.white_forbidden.x == x && self.white_forbidden.y == y {
            return false;
        }

        self.board[x as usize][y as usize] = self.player;
        self.reset();
        let mut cur_group = Group::new(x, y, self.size);
        cur_group.get_group_length_and_liberty(x, y, self.player, self.size, &self.board);
        let mut self_count = 0;

        for stone in cur_group.stones.iter() {
            self.st[stone.x as usize][stone.y as usize] = true;
            self_count += 1;
        }

        let eat_oppo_groups = self.get_all_groups_length_and_liberty(self_count);

        if cur_group.liberties == 0 && eat_oppo_groups == 0 {
            self.board[x as usize][y as usize] = EMPTY;
            return false;
        } else {
            if self.player == WHITE {
                self.white_forbidden = Point::new(-1, -1);
                self.forbidden_list.push_back(Point::new(self.black_forbidden.x, self.black_forbidden.y));
            } else {
                self.black_forbidden = Point::new(-1, -1);
                self.forbidden_list.push_back(Point::new(self.white_forbidden.x, self.white_forbidden.y));
            }

            self.steps.push_back(Point::new(x, y));
            self.play_count += 1;
            self.captured_stones.clear();
            self.captured_stones.extend(self.tmp_captured.clone());
            self.tmp_captured.clear();
            self.change_player();
            self.save_state();
            return true;
        }
    }

    fn save_state(&mut self) {
        let mut res = String::new();
        for x in 1..=self.size {
            for y in 1..=self.size {
                res.push_str(&self.board[x as usize][y as usize].to_string());
            }
        }
        self.game_record.push(res);
    }

    fn regret_play(&mut self) {
        self.game_record.pop();
        self.steps.pop_back();
        self.forbidden_list.pop_back();

        let cur_state = self.game_record.last().cloned().unwrap_or_else(|| "".to_string());
        let mut cur_forbidden = self.forbidden_list.back().cloned().unwrap_or_else(|| Point::new(-1, -1));

        if self.player == BLACK {
            self.black_forbidden = cur_forbidden;
            self.white_forbidden = Point::new(-1, -1);
        } else {
            self.white_forbidden = cur_forbidden;
            self.black_forbidden = Point::new(-1, -1);
        }

        self.play_count -= 1;
        self.change_player();

        for x in 1..=self.size {
            for y in 1..=self.size {
                self.board[x as usize][y as usize] = cur_state
                    .chars()
                    .nth((x - 1 + (y - 1) * self.size) as usize)
                    .unwrap_or('0')
                    .to_digit(10)
                    .unwrap_or(0) as i32;
            }
        }
    }

    fn get_captured_stones(&self) -> Vec<Point> {
        self.captured_stones.iter().cloned().collect()
    }

    fn get_board(&self) -> Vec<Vec<i32>> {
        self.board.clone()
    }

    fn get_play_count(&self) -> i32 {
        self.play_count
    }
}

// 示例用法
fn main() {
    let mut board = Board::new(19);
    let ok = board.play(4, 4);
    println!("Can play: {:?}", ok);
    for row in board.get_board().iter() {
        println!("{:?}", row);
    }
    println!("{:?}", board.get_captured_stones());
    board.regret_play();
    for row in board.get_board().iter() {
        println!("{:?}", row);
    }
    println!("{:?}", board.get_play_count());
}
