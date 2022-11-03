package go.core;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private final Set<Point> stones;
    private final Set<Point> liberties;
    private final Player owner;

    public Group(Set<Point> stones, Set<Point> liberties, Player owner) {
        this.stones = stones;
        this.liberties = liberties;
        this.owner = owner;
    }

    public Group(Point Point, Player owner) {
        stones = new HashSet<>();
        stones.add(Point);
        this.owner = owner;
        liberties = new HashSet<>(Point.getEmptyNeighbors());
    }

    public Group(Group Group) {
        this.stones = new HashSet<>(Group.stones);
        this.liberties = new HashSet<>(Group.liberties);
        this.owner = Group.owner;
    }

    public Player getOwner() {
        return owner;
    }

    public Set<Point> getLiberties() {
        return liberties;
    }

    public Set<Point> getStones() {
        return stones;
    }

    public void add(Group Group, Point playedStone) {
        this.stones.addAll(Group.stones);
        this.liberties.addAll(Group.liberties);
        this.liberties.remove(playedStone);
    }

    public void removeLiberty(Point playedStone) {
        Group newGroup = new Group(stones, liberties, owner);
        newGroup.liberties.remove(playedStone);
    }

    public void die() {
        for (Point rollingStone : this.stones) {
            rollingStone.setGroup(null);
            Set<Group> adjacentGroups = rollingStone.getAdjacentGroups();
            for (Group Group : adjacentGroups) {
                Group.liberties.add(rollingStone);
            }
        }
    }
}
