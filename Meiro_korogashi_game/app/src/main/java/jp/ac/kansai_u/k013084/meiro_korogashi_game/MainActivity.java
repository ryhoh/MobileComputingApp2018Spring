package jp.ac.kansai_u.k013084.meiro_korogashi_game;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

import game.Enemy;
import game.Environment;

class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread thread;
    private Context context;

    private Environment env;

    // クリアするかゲームオーバーになるまでは、中断しても再開できるようにしたい
    private boolean playing = false;

    public DrawSurfaceView(Context context) {
        super(context);

        this.context = context;
        // registration of callback
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // pass
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    @Override
    public void run() {
        if(!playing) {
            // 環境の初期化
            // 本当はどの端末でも正しく動くようにしたいが、とりあえずタブレットで動くサイズにする
//            env = new Environment(21, 37, getHeight(), getWidth());
            env = new Environment(33, 21, getHeight(), getWidth()); //スマホ用
            env.makeNewGame();
//            env = new Environment("https://www.google.com", 32, getHeight(), getWidth());
//            env.makeNewQRGame();
            for (int i = 0; i < 15; i++)
                env.putCharacter(new Enemy(0.05, Color.MAGENTA));
            playing = true;
        }

        int mode = 0;
        while(thread != null) {
            // 環境を1フレーム進める
            if((mode=env.step()) != 0) break;

            // 1フレームの処理を行う
            Canvas canvas = getHolder().lockCanvas();
            env.print(canvas);
            getHolder().unlockCanvasAndPost(canvas);

            try { // 次のフレームまで待機
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        switch (mode) {
            case 1:
                Looper.prepare();
                Toast.makeText(context, "Congratulations!", Toast.LENGTH_LONG).show();
                Looper.loop();
                playing = false;
                break;

            case 2:
                Looper.prepare();
                Toast.makeText(context, "Game Over...", Toast.LENGTH_LONG).show();
                Looper.loop();
                playing = false;
                break;

            default:
                break;
        }
    }
}

public class MainActivity extends Activity  implements SensorEventListener {
    private SensorManager manager;
    private static Resources rsr;
    private static double sx, sy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawSurfaceView ds = new DrawSurfaceView(this);
        setContentView(ds);
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        rsr = getResources();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 横画面で実行
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensors.size() > 0) {
            Sensor s = sensors.get(0);
            manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sx = event.values[SensorManager.DATA_X];
        sy = event.values[SensorManager.DATA_Y];
    }

    public static double getSx() {
        return sx;
    }

    public static double getSy() {
        return sy;
    }

    public static Resources getRsr() {
        return rsr;
    }
}
