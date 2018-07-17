package game;

import java.util.ArrayList;
import java.util.Objects;

// 迷路のマス目を管理するクラス
class Cell {
    int x, y;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        Cell c = (Cell)obj;
        return this.x==c.x && this.y==c.y;
    }

    public int hashCode() {
        return Objects.hash(x, y);
    }

    // そのセルが壁かどうか調べる
    public boolean isWall(boolean[][] wall) {
        return wall[y][x];
    }

    // 自分の4近傍のセルを{左右下上}につめたリストを返すメソッド
    public ArrayList<Cell> neighbor4() {
        final int[] dx = {-1, 1, 0, 0}, dy = {0, 0, -1, 1};

        ArrayList<Cell> list = new ArrayList<>();
        for(int i=0; i<4; i++) {
            list.add(i, new Cell(this.x +dx[i], this.y +dy[i]));
        }

        return list;
    }
}
