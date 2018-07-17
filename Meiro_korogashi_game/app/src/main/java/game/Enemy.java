package game;

/*
        2018/07/12 Tetsuya Hori @ MobileComputing
 */

import android.graphics.Canvas;
import android.graphics.Paint;

// この物体は視界を持ち、ぶつかるか見つかるとゲーム終了
public class Enemy extends Character {
    private Paint paint;

    public Enemy(double speed, int color) {
        super(speed);
        (paint = new Paint()).setColor(color);
    }

    @Override
    public void draw(Canvas canvas, double cellSize, double v_margin) {
        canvas.drawCircle((float)(this.x*cellSize), (float)(this.y*cellSize + v_margin),
                (float)(cellSize*0.45), paint);
    }

}
