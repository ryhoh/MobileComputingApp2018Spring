package game;

/*
        2018/07/10 Tetsuya Hori @ MobileComputing
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.common.BitArray;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.pdf417.encoder.BarcodeMatrix;
//import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import jp.ac.kansai_u.k013084.meiro_korogashi_game.R;
import jp.ac.kansai_u.k013084.meiro_korogashi_game.MainActivity;

// ゲームの環境を構築し、ゲームを進行させるクラス
public class Environment {
    private int height, width, disp_height, disp_width;
    private double cellSize, v_margin;
    private Cell start, goal;
    private Random r;

    // 外周1マスは道扱い、ゲームで実際に使うのは1ベースの範囲だけ 壁ならtrue 道ならfalse
    private boolean[][] wall;

    private Paint wallColor, roadColor, startColor, goalColor, playerColor;
    Bitmap texture = BitmapFactory.decodeResource(MainActivity.getRsr(), R.drawable.bgs);

    // QRコード用（中止）
//    private BitMatrix bitMatrix;
//    private int offset_qr;

    // 登場するもの
    private Player player;
    private ArrayList<Character> characters;

    private final int[] dx = {-1, 1, 0, 0}, dy = {0, 0, -1, 1}; // 4近傍確認用


    public Environment(int height, int width, int disp_height, int disp_width) {
        // 生成アルゴリズムの都合上、長さは奇数に
        if (height%2 == 0) height++;
        if (width%2 == 0) width++;

        this.height = height;
        this.width = width;
        this.disp_height = disp_height;
        this.disp_width = disp_width;

        // リストの初期化
        characters = new ArrayList<>();

        setPrintOptions();
    }

    // QRコードをベースに迷路を作る（あまり迷路としてよくないので中止）
//    public Environment(String txt, int size, int disp_height, int disp_width) {
//        // 外部ライブラリ使用
//        int right_bottom_h = 0;
//        try {
//            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//            bitMatrix = barcodeEncoder.encode(txt, BarcodeFormat.QR_CODE, 0, 0);
//            for(int h=0; h<bitMatrix.getHeight(); h++) {
//                for(int w=0; w<bitMatrix.getHeight(); w++) {
//                    if(bitMatrix.get(w, h)) {
//                        right_bottom_h = h;
//                        break;
//                    }
//                }
//            }
//        } catch (WriterException e) {
//            throw new AndroidRuntimeException("QR error ", e);
//        }
//
//        offset_qr = bitMatrix.getHeight() - right_bottom_h -2;
//        this.width = this.height = right_bottom_h - offset_qr + 2;
//        this.disp_height = disp_height;
//        this.disp_width = disp_width;
//
//        // リストの初期化
//        characters = new ArrayList<>();
//
//        setPrintOptions();
//    }

    public void makeNewGame() {
        // シード値を決める
        final int seed = (int)(Math.random() * Integer.MAX_VALUE);
        r = new Random(seed);

        // マップ生成
        wall = new boolean[height+2][width+2];
        for(int i=0; i<height+2; i++)
            Arrays.fill(wall[i], true); // 壁で埋める

        // 穴掘り法で迷路作成
        dig();

        // ゴールまでのルートを複数設けるために，壁を破壊する
        breakWall();

        // スタートとゴールを設定
        setStartGoal();

        // スタートにプレイヤーを配置
        player = new Player(start.x, start.y, playerColor);
    }

//    public void makeNewQRGame() {
//        // シード値を決める
//        final int seed = (int)(Math.random() * Integer.MAX_VALUE);
//        r = new Random(seed);
//
//        // マップ生成
//        int qr_size = this.height;
//        wall = new boolean[qr_size+2][qr_size+2];
//        Arrays.fill(wall[0], true);
//        for(int h=1; h<qr_size+1; h++) {
//            wall[h][0] = wall[h][qr_size+1] = true;
//            for(int w=1; w<qr_size+1; w++) {
//                wall[h][w] = bitMatrix.get(w-1+offset_qr, h-1+offset_qr);
//            }
//        }
//        Arrays.fill(wall[qr_size+1], true);
//
//        // スタートとゴールを設定
//        setStartGoal();
//
//        // スタートにプレイヤーを配置
//        player = new Player(start.x, start.y, playerColor);
//    }

    private void dig() { // 穴掘り法
        // 外周1マスを道で埋める
        Arrays.fill(wall[0], false); Arrays.fill(wall[height+1], false); // 上下
        for(int i=0; i<height+2; i++) { wall[i][0] = wall[i][width+1] = false; } // 左右

        ArrayList<Cell> openCell = new ArrayList<>(); // 枝分かれする（可能性のある）マスをここに詰め込む

        // 内側(width-1)*(height-1)の範囲の中からそれぞれ偶数である開始点を1つ選ぶ
        int selected_x = r.nextInt((width-1)/2)*2+2, selected_y = r.nextInt((height-1)/2)*2+2;

        // 開始地点をpushして穴掘り開始
        Cell here = new Cell(selected_x, selected_y);
        openCell.add(here);
        wall[here.y][here.x] = false; // 現在地を道にする
        while(openCell.size() != 0) {
            // 4近傍のどれかに進みたい．ランダムな順番に見ていく
            ArrayList<Integer> nexts = new ArrayList<>();
            for(int i=0; i<4; i++) nexts.add(i);
            boolean found = false; // どこにも進めない場合false
            while(!nexts.isEmpty()) {
                // 残っている近傍から1つ選ぶ
                int selected = r.nextInt(nexts.size());
                int next_i = nexts.get(selected);
                nexts.remove(selected);

                int next1_x = here.x + dx[next_i], next1_y = here.y + dy[next_i]; // 1つ隣
                int next2_x = here.x + 2*dx[next_i], next2_y = here.y + 2*dy[next_i]; // 2つ隣

                if(!wall[next1_y][next1_x]) // さっきいた場所に戻りたくない，隣が道だといけない
                    continue;

                if(wall[next1_y][next1_x] && wall[next2_y][next2_x]) { // 2マス先まで壁なら
                    wall[next1_y][next1_x] = wall[next2_y][next2_x] = false; // 道にする
                    here = new Cell(next2_x, next2_y); // 次の開始地点
                    openCell.add(here);
                    found = true;
                    break;
                }
            }
            if(!found) { // 次の地点をopenCellからランダムに1つ選ぶ
                int next_i = r.nextInt(openCell.size());
                here = openCell.get(next_i);
                openCell.remove(next_i); // 選んだら削除
            }
        }
    }

    private void breakWall() {
        // 破壊する壁の個数
        int rest = (int)(0.03 * width*height);

        while(rest > 0) {
            // 破壊対象をランダムに1つ選ぶ
            int brk_x = r.nextInt(width-2)+2, brk_y = r.nextInt(height-2)+2;
            if(wall[brk_y][brk_x]) { // 破壊対象は壁か
                if((!wall[brk_y-1][brk_x]&&!wall[brk_y+1][brk_x]&&wall[brk_y][brk_x-1]&&wall[brk_y][brk_x+1])
                    || (wall[brk_y-1][brk_x]&&wall[brk_y+1][brk_x]&&!wall[brk_y][brk_x-1]&&!wall[brk_y][brk_x+1])) {
                    // 上下が道で左右が壁，または上下が壁で左右が道なら
                    wall[brk_y][brk_x] = false; // 破壊して道にする
                    rest--;
                }
            }
        }
    }

    private void setStartGoal() {
        // ランダムに1つ道を選ぶ
        int init_x, init_y;
        while(true) {
            init_x = r.nextInt(width-2)+2; init_y = r.nextInt(height-2)+2;
            if(!wall[init_y][init_x])
                break;
        }

        // その点から最も遠い場所をBFSで見つけて，そこをゴールにして，そこから最も遠い位置にスタートを置く
        goal = BFSfar(new Cell(init_x, init_y));
        start = BFSfar(goal);
    }

    // fromから最も遠い位置を見つけるBFS（幅優先探索）
    private Cell BFSfar(Cell from) {
        ArrayDeque<Cell> que = new ArrayDeque<>(); // 見る場所を登録
        HashSet<Cell> set = new HashSet<>(); // 見た場所を登録
        que.addLast(from);
        Cell result = from;
        while(!que.isEmpty()) {
            Cell here = que.poll();
            if(set.contains(here)) continue; // 既に見たところはもう見ない

            set.add(here);

            for(int i=0; i<4;i++) { // 隣の道を追加
                int next_x = here.x +dx[i], next_y = here.y +dy[i];
                if (!wall[next_y][next_x])
                    que.addLast(new Cell(next_x, next_y));
            }

            result = here; // 結果を更新
        }
        return result; // 一番最後に代入されたものが最遠のもの
    }

    public void putCharacter(Character ch) {
        // ランダムに（スタート地点よりある程度離れている）道を1つ選び，その道の中心にキャラクターをセット
        while(true) {
            Cell cand = new Cell(r.nextInt(width-2)+2, r.nextInt(height-2)+2);
            if(!wall[cand.y][cand.x] && !closeIn(10, cand, start)) {
                ch.setX(cand.x);
                ch.setY(cand.y);

                // 移動可能性を設定
                int[][] possibility = new int[height+2][width+2];
                for(int h=0; h<height+2; h++)
                    for(int w=0; w<width+2; w++) {
                        if (wall[h][w]) possibility[h][w] = 0;
                        else possibility[h][w] = 10;
                    }
                possibility[cand.y][cand.x] = 1; // 今いる場所を1にする
                ch.setPossibility(possibility);
                break;
            }
        }
        characters.add(ch);
    }

    // DFSでfromからtoまでの距離がrange以内か調べる
    private boolean closeIn(int range, Cell from, Cell to) {
        if(range < 0) return false;
        if(from.equals(to)) return true;

        for(int i=0; i<4;i++) { // 隣の道を探索
            if (!wall[from.y +dy[i]][from.x +dx[i]])
                if(closeIn(range-1, new Cell(from.x +dx[i], from.y +dy[i]), to))
                    return true;
        }

        return false;
    }

    // ゲームを1フレーム進める
    // ゲーム終了で値を返す
    // 1: クリア   2: ゲームオーバ
    public int step() {
        for(Character chr: characters) {
            chr.advance(wall, r);
        }
        player.advance(wall);

        // ゴールに着いたか
        if(player.isIn(goal)) return 1;

        // 敵とぶつかったか
        for(Character chr: characters) // しきい値は敵の半径とプレイヤーの半径の和
            if(Math.abs(player.getX()-chr.getX()) < 0.7
                    && Math.abs(player.getY()-chr.getY()) < 0.7) return 2;

        return 0;
    }

    private void setPrintOptions() {
        // 1セルあたりの横幅の長さを計算し，それを1辺とする正方形で表現
        cellSize = disp_width / width;

        // 縦には，2つの余白を作るようにして，中央に表示する
        // 余白の長さ
        v_margin = (disp_height - cellSize * height) / 2;

        // 色
        (wallColor = new Paint()).setColor(Color.TRANSPARENT);
        (roadColor = new Paint()).setColor(Color.WHITE);
        (goalColor = new Paint()).setColor(Color.GREEN);
        (startColor = new Paint()).setColor(Color.YELLOW);
        (playerColor = new Paint()).setColor(Color.BLACK);
    }

    public void print(Canvas canvas) {
        // 背景（フリー素材使用）
        // https://free-texture.net/seamless-pattern/hemp-cloth-pattern-set.html
        for(int left=0; left<disp_width; left+=texture.getWidth())
            for(int top=0; top<disp_height; top+=texture.getHeight())
                canvas.drawBitmap(texture, left, top, new Paint());
        printMaze(canvas);
        printCharacters(canvas);
    }

    private void printMaze(Canvas canvas) {
        // きたない
        for(int h=1; h<height+1; h++) {
            for(int w=1; w<width; w++) {
                if(wall[h][w])
                    canvas.drawRect((float)((w-1)*cellSize), (float)((h-1)*cellSize + v_margin),
                            (float)(w*cellSize), (float)(h*cellSize + v_margin),
                            wallColor);
                else
                    canvas.drawRect((float)((w-1)*cellSize), (float)((h-1)*cellSize + v_margin),
                            (float)(w*cellSize), (float)(h*cellSize + v_margin),
                            roadColor);
            }
            // 横の最後の一個（スキマができないようにする）
            if(wall[h][width])
                canvas.drawRect((float)((width-1)*cellSize), (float)((h-1)*cellSize + v_margin),
                        (float)disp_width, (float)(h*cellSize + v_margin),
                        wallColor);
            else
                canvas.drawRect((float)((width-1)*cellSize), (float)((h-1)*cellSize + v_margin),
                        (float)disp_width, (float)(h*cellSize + v_margin),
                        roadColor);
        }

        // ゴールに着色
        canvas.drawRect((float)((goal.x-1)*cellSize), (float)((goal.y-1)*cellSize + v_margin),
                (float)(goal.x * cellSize), (float)((goal.y * cellSize) + v_margin), goalColor);

        // （デバッグ用）スタートに着色
//        canvas.drawRect((float)((start.x-1)*cellSize), (float)((start.y-1)*cellSize + v_margin),
//                (float)(start.x * cellSize), (float)((start.y * cellSize) + v_margin), startColor);
    }

    private void printCharacters(Canvas canvas) {
        for(Character chr: characters) {
            chr.draw(canvas, cellSize, v_margin); // 表示方法は各オブジェクトによる（ポリモーフィズム）
        }
        player.draw(canvas, cellSize, v_margin);
    }
}
