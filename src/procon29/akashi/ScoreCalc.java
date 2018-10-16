package procon29.akashi;

import procon29.akashi.owners.Owner;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

class ScoreCalc {
    //8近傍移動用
    static private int[] dx = {0, 1, 1, 1, 0, -1, -1, -1}, dy = {1, 1, 0, -1, -1, -1, 0, 1};

    //探索用配列初期化
    private boolean[][] used;

    private GameBoard board;

    private Owner targetTeam;

    ScoreCalc(GameBoard gameBoard, Owner targetTeam) {
        this.board = gameBoard;
        //探索用配列初期化
        this.used = new boolean[board.maker.getHeight()][board.maker.getWidth()];
        for (boolean[] arr : this.used) Arrays.fill(arr, false);
        this.targetTeam = targetTeam;
    }

    /**
     * 指定した座標から繋がっている、囲われている点の合計を返す
     *
     * @param startY y座標
     * @param startX x座標
     * @return 点の合計
     */
    int calcSurroundScore(int startY, int startX) {
        //探索済みなら帰る
        if (used[startY][startX]) return 0;

        System.err.println("called");

        //キューを作成して座標点を入れる
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startY, startX));
        //スコアを足す
        int score = Math.abs(board.getScore(startX, startY));
        //囲われていると仮定
        boolean isSurrounded = true;
        //探索済みに
        used[startY][startX] = true;

        while (!queue.isEmpty()) {//繋がっているところがあるならループ
            Point p = queue.poll();

            for (int i = 0; i < 8; i++) {
                int newY = p.x + dy[i], newX = p.y + dx[i];
                //そのチームの座標と探索済みにはいかない
                if (board.getOwn(newX, newY) == targetTeam || used[newY][newX]) continue;

                //範囲外に出たのなら囲われていない
                if (newX < 0 || newY < 0 || newX >= board.maker.getWidth() || newY >= board.maker.getHeight()) {
                    isSurrounded = false;
                    continue;
                }

                //探索開始
                used[newY][newX] = true;
                score += Math.abs(board.getScore(newX, newY));
                queue.add(new Point(newY, newX));
            }
        }

        if (isSurrounded) {
            System.err.println("surrounded");
            return score;
        } else {
            return 0;
        }
    }
}
