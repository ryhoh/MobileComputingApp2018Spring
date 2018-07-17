package game;

/*
        2018/07/14 Tetsuya Hori @ MobileComputing
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import jp.ac.kansai_u.k013084.meiro_korogashi_game.MainActivity;

// ユーザが実際に操作するもの
// 練習課題におけるボールのようなもの
class Player {
    private double x, y, speed = 1.8;
    private Paint paint;
    private final double sizeByCell = 0.3; // セルに対する大きさの比

    Player(int x, int y, Paint paint) {
        this.x = (double)x - 0.5;
        this.y = (double)y - 0.5;
        this.paint = paint;
    }

    double getX() {
        return x;
    }

    double getY(){
        return y;
    }

    void advance(boolean[][] wall) {
        double dx = Math.min(MainActivity.getSx(), 5); // 加速度が大きすぎないように制限
        double dy = Math.min(MainActivity.getSy(), 5);

        // 違う制御方式
        final double norm = Math.sqrt(dx*dx + dy*dy);
        dx = speed*Math.abs(dx) * dx / norm; // 単位ベクトルにさらにスピードをかけて、
        dy = speed*Math.abs(dy) * dy / norm; // 傾けるほど速く動かす

        double trg_x = dx * 0.04, trg_y = dy * 0.04;

        // 壁でないなら進む
        if(!wall[(int)(Math.ceil(y + trg_y + Math.signum(trg_y)*sizeByCell))][(int)Math.ceil(x)])
            y += trg_y;
        if(!wall[(int)Math.ceil(y)][(int)(Math.ceil(x + trg_x + (-1)*Math.signum(trg_x)*sizeByCell))])
            x -= trg_x;
    }

    void draw(Canvas canvas, double cellSize, double v_margin) {
        canvas.drawCircle((float)(this.x*cellSize), (float)(this.y*cellSize + v_margin),
                (float)(cellSize*sizeByCell), paint);
    }

    // 場所（ゴールなど）にいるか
    boolean isIn(Cell pos) {
        return pos.x==(int)Math.ceil(x) && pos.y==(int)Math.ceil(y);
    }
}
