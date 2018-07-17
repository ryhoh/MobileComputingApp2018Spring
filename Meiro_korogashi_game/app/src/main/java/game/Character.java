package game;

/*
        2018/07/11 Tetsuya Hori @ MobileComputing
 */

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

// 環境に配置するキャラクターの親クラス
// キャラクターは位置を持ち、移動する
public abstract class Character {
    protected double x, y, speed;
    private int direction; // 移動先が{左右下上}のどこにあるかを{0,1,2,3}で表す dxdyの添字になる
    private double trg_x, trg_y; // 移動する先 （x,yをセットするとき，目標地点にも同じ値をセットしておく
    private double angle; // 現在向いている角度

    // 各セルへの移動可能性 道は10，壁は0で初期化し，訪れた直後のセルは1にする
    private int[][] possibility; // 1セル移動するたび，すべてのセルの値w_ijを min(10, w_ij +1) で更新

    Character(double speed) {
        this.speed = speed;
    }

    public void setX(double x) {
        this.x = trg_x = x;
    }

    public void setY(double y) {
        this.y = trg_y = y;
    }

    // 配列の添字で指定された場合, セルの間を表現できるよう小数に変換して格納
    public void setX(int x) {
        this.x = trg_x = ((double)x-0.5); // セルの中央に配置
    }

    public void setY(int y) {
        this.y = trg_y = ((double)y-0.5); // セルの中央に配置
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // どのセルに属するか，添字で返す
//    public Cell getXY() {
//        return new Cell((int)Math.floor(x + 0.5), (int)Math.floor(y + 0.5));
//    }

    public void setPossibility(int[][] possibility) {
        this.possibility = possibility;
    }

    // 自分自身を迷路上に（自分の方法で）表示する
    public abstract void draw(Canvas canvas, double cellSize, double v_margin);

    public void advance(boolean[][] wall, Random r) {
        final int[] dx = {-1, 1, 0, 0}, dy = {0, 0, -1, 1};

        // 移動先に到着した場合，次の移動先を決定する
        // 小数同士の計算での誤差を考慮，一定量を許容すべき
        if(Math.abs(x-trg_x)<=0.05 && Math.abs(y-trg_y)<=0.05) {
            choiceTarget(wall, r);

//            String txt = "";
//            for(int a:ruiseki) txt = txt + a + " ";
//            Log.d("ruiseki", txt);
//            Log.d("slct", String.valueOf(slct));
        }
        // 移動
        x += dx[direction] * speed;
        y += dy[direction] * speed;
    }

    // 次の行き先を決める
    private void choiceTarget(boolean[][] wall, Random r) {
        // 4近傍を見る
        Cell here = new Cell((int)(trg_x+0.5), (int)(trg_y+0.5));
        ArrayList<Cell> neighbors = here.neighbor4();

        // 一様分布で1つ選ぶ
//            int trg_i = r.nextInt(neighbors.size());
//            Cell trg = neighbors.get(trg_i);
//            trg_x = trg.x-0.5; trg_y = trg.y-0.5;
//            direction = directions.get(trg_i)-16;

        // 移動可能性を利用して，同じところは避ける
        int[] ruiseki = new int[neighbors.size()]; // ルーレットのしきい値 = 累積和
        ruiseki[0] = possibility[neighbors.get(0).y][neighbors.get(0).x];
        for(int i=1; i<4; i++)
            ruiseki[i] = ruiseki[i-1] + possibility[neighbors.get(i).y][neighbors.get(i).x];

        int slct = r.nextInt(ruiseki[3]); // ルーレット選択
        for(int i=0; i<4; i++) {
            if(slct < ruiseki[i]) {
                trg_x = neighbors.get(i).x-0.5; trg_y = neighbors.get(i).y-0.5;
                direction = i;
                break;
            }
        }

        // 移動可能性の更新
        for(int h=2; h<possibility.length-2; h++)
            for(int w=2; w<possibility[h].length-2; w++)
                if(!wall[h][w]) // 道なら
                    possibility[h][w] = Math.min(10, possibility[h][w]+1); // 値の更新
        possibility[here.y][here.x] = 1; // 訪れた場所を1にする
    }

}
