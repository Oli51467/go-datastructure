# Go  围棋游戏的数据结构 (仅150行实现！English version below)

围棋是一种策略棋类，使用格状棋盘及黑白二色棋子进行对弈。起源于中国，中国古时有“弈”、“碁”、“手谈”等多种称谓，属琴棋书画四艺。西方称之为“Go”，是源自日语“碁”的发音。

围棋有黑白两种棋子，规定由执黑色棋子的先行，对弈双方在十九乘十九条线的棋盘网格上的交叉点交替放置黑色及白色的棋子。落子完毕后，不能悔棋。对弈过程中围地吃子，以所围“地”的大小决定胜负。

围棋规则简洁而优雅，但玩法却千变万化，欲精通其内涵需要大量的练习与钻研。

### 基本规则

下棋时，对弈双方各执一种颜色的棋子，黑先白后，轮流将一枚棋子放置于交叉点上。与棋子直线相连的空白交叉点叫做气。当这些气都被对方棋子占据后，该棋子就没有了“气”，要被从棋盘上提掉。

如果棋子的相邻（仅上下左右）直线交叉点上有了同色的棋子，则这两个棋子被叫做相连的。任意多个棋子可以以此方式联成一体，连成一体的棋子的气的数目是所有组成这块棋的单个棋子气数之和。如果这些气都被异色棋子占领，这块棋子就要被一起提掉。

劫是围棋中较特殊的一种情况。举例来说，当黑方某一手提掉对方一子，而这一个黑子恰好处于白方虎口之内，这时白方不能立即提掉这一黑子，而必须在棋盘其他地方着一手（称为“找劫材”），使黑方不得不应一手（称为“应劫”），然后白才能够提掉这个黑子。

### 禁止自杀规则

下子时，除非能令对方某些子失去所有的气，否则不得下子令自己某些子失去所有气，这亦被称为“禁止自杀规则”

### 数据结构

#### Board类
抽象地描述了一个围棋棋盘
##### 变量
```height width```：棋盘的规格，坐标系为朝右为x轴正方向，朝下为y轴正方向

```player```：当前应该落子的一方

```sgfRecord```：用于对棋局的记录(一个用于方便存储和传输的字符串，也可以使用其他形式来记录每一步)

```st```:记录一个格点是否被遍历的辅助数组

```blackForbidden ｜ whiteForbidden```: 记录一个局部形成打劫后黑方和白方的禁入点。
##### 注意：这里只记录打劫形成的禁入点，自杀的禁入点不进行记录，只进行判断。

##### 方法
```getAllGroupsLengthAndLiberty```：

使用**Flood Fill**遍历棋盘上的所有位置，如果这个位置没有棋子，则
跳过。如果有棋子，从该棋子开始遍历所有连接该棋子的同色棋子(组)，(即使组内只有一颗棋子)。

遍历完成后， 获取该组的长度和它的所有的气。根据围棋规则，当一步落下时，除了该子所属的组，不可能杀死自己的其它组，所以
这里只需要判断对方的某个或某些组是否被杀死。

一个局部形成打劫，当且仅当：**该子落下后，该子所在的组失去了所有的气并且该子所在的组只有一个棋子(即它本身)&&
该子落下后对方仅有一个组失去了所有的气且该组只有一颗棋子。** 如果该局部形成打劫，更新对方的禁入点，禁入点即为
该子落下后，对方被提吃的仅有的一颗子的位置。

```play```：判断一步棋是否可以走在棋盘上，如果可以，则更新棋盘，如果不可能，则什么都不会更新。

1. 如果该点在棋盘外或者该点的位置已经有子，则不可以落子，返回**false**
2. 如果该点在本方记录的打劫禁入点内，则不可以落子，返回**false**
3. 排除以上两种情况后，则一定可以落子(没有反例)

判断可以落子后：
1. 清空辅助数组
2. 将该子真正地更新在棋盘上(很重要)
3. 遍历该子所属的整个组，并记录该组的长度
4. 遍历棋盘，检查对方是否有一个或一串棋子被吃
5. 如果没有对方没有棋子被吃，并且自己该子落下后失去了所有的气，那么**该行为属于自杀，落子无效**，恢复该位置的棋盘落子，并返回**false**
6. 否则该位置可以落子，并清空自己的所有禁入点，因为该落子的有效意味着这是一枚劫财，之后的所有打劫处都可以落子。


#### Group类
记录了一串棋子的气和长度，以及该串棋子包括的位置信息。

##### 变量
```liberties```:气

```lenght```: 该组共包含多少枚棋子

```stones```: 该组内所有棋子的位置

```st```： Flood Fill遍历的辅助数组

##### 方法

```add2Group```: 将一枚棋子加入到该组内

```getGroupLength```: 从一颗棋子开始延伸，遍历所有联通该棋子的且与该棋子同色的所有棋子，并记录它的气和长度。
采用递归的方法遍历。

有更多问题可以尽管提出，欢迎！🌟🌟🌟

# Go  The data structure of the game of Go (only 150 lines to achieve!)

Go is a strategic game that uses a grid-like board and black and white pieces. Originated in China, in ancient China, there were many titles such as "Yi", "Qi" and "Shoutan". It belongs to the four arts of piano, chess, calligraphy and painting. It is called "Go" in the west, which is derived from the pronunciation of "颁" in Japanese.


### basic rules

When playing chess, the two players each hold a piece of one color, black first and then white, and take turns placing a piece on the intersection. 

The blank intersections connected with the pawns in a straight line are called qi. 

When all the qi is occupied by the opponent's pieces, the piece has no "qi" and will be removed from the board.


### no suicide rule

When you make a move, unless you can make some of the opponent's pieces lose all their energy, you must not make some of your own pieces lose all of your energy. This is also known as the "No Suicide Rule".


### Data Structure

#### Board Class

Abstractly depicts a Go board

##### Variable
```height width```：The specification of the chessboard, the coordinate system is the positive direction of the x-axis facing right, and the positive direction of the y-axis facing downward


```player```：The party that should be placed

```sgfRecord```：Used to record the chess game (a string for convenient storage and transmission, and other forms can also be used to record each step)

```st```:Auxiliary array to record whether a grid point is traversed

```blackForbidden ｜ whiteForbidden```: Record a partially formed black and white forbidden entry point after the robbery.

##### Note: Here only the forbidden entry points formed by robbery are recorded, and the forbidden entry points for suicide are not recorded, but only judged.


##### Methods
```getAllGroupsLengthAndLiberty```：

Use **Flood Fill** to traverse all positions on the board, if there is no chess piece in this position, then
jump over. If there is a chess piece, start from the chess piece to traverse all the same-color chess pieces (groups) connected to the chess piece, (even if there is only one chess piece in the group).

After the traversal is complete, get the length of the group and all of its gas. According to the rules of Go, when a move falls, it is impossible to kill other groups except the group to which the child belongs, so Here it is only necessary to judge whether one or some groups of the other party are killed.

A part forms a robbery if and only if: **After the piece falls, the group where the piece is located loses all Qi and the group where the piece is located has only one piece (ie itself)&&After the piece falls, the opponent has only one group that loses all the gas and this group has only one chess piece.** If the part forms a robbery, update the opponent's forbidden entry point, the forbidden entry point is After the piece falls, the opponent is lifted to the position of the only piece that has been eaten.

```play```：Determine whether a move can be made on the board, if so, update the board, if not, update nothing.

1. If the point is outside the chessboard or there is already a piece at the point, the piece cannot be placed, and return **false**
2. If the point is within the robbing forbidden point recorded by the party, it is not allowed to place a ball and return **false**
3. After excluding the above two situations, you must be able to make a move (there is no counterexample)

After the judgment can be made:
1. empty the auxiliary array
2. actually update the piece on the board (very important)
3. Iterate through the entire group that the child belongs to, and record the length of the group
4. Traverse the board, check if the opponent has a piece or a string of pieces captured
5. If there is no opponent and no pieces are taken, and you lose all your Qi after the piece is dropped, then **this behavior is suicide, and the piece is invalid**, restore the chessboard at this position, and return **false**
6. Otherwise, you can place a piece at this position and clear all your forbidden entry points, because the validity of this position means that this is a robbery, and all subsequent robberies can be placed.


#### Group Class
The qi and length of a string of chess pieces are recorded, as well as the position information included in the string of chess pieces.

##### Variable
```liberties```: determinate whether the stones can live

```lenght```: How many chess pieces are included in this group

```stones```: The positions of all pieces in the group

```st```： Auxiliary array for Flood Fill traversal

##### Methods

```add2Group```: Add a pawn to the group

```getGroupLength```: Extend from a chess piece, traverse all the chess pieces that are connected to the chess piece and of the same color as the chess piece, and record its qi and length.
Use recursive method to traverse.

Feel free to ask more questions, welcome!

 Please Star if you think its useful :) thanks!



