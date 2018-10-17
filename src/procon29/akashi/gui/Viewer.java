package procon29.akashi.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import procon29.akashi.GameBoard;
import procon29.akashi.owners.Owner;
import procon29.akashi.owners.OwnerToImageConverter;
import procon29.akashi.players.Player;
import procon29.akashi.scores.ScoreToImageConverter;
import procon29.akashi.selection.*;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * GameBoardを視覚化するクラス
 */
public class Viewer {
    /**
     * GUIの基底
     */
    private BorderPane root;
    /**
     * GUIのコントローラークラス
     */
    private Controller controller;
    /**
     * Viewerが視覚化する対象
     */
    private GameBoard gameBoard;
    /**
     * プレイヤーの画像をキャッシュしておく
     */
    private static Image[] images = {new Image("FriendPlayer1.png"), new Image("FriendPlayer2.png"), new Image("EnemyPlayer1.png"), new Image("EnemyPlayer2.png")};

    /**
     * 渡されたGameBoardの状態に基づいてViewerを作る
     *
     * @param gameBoard 視覚化する対象
     */
    public Viewer(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        try {
            //load FXML and load controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/root.fxml"));
            root = loader.load();
            controller = loader.getController();

            stringsUpdate();

            for (int y = 0; y < gameBoard.maker.getHeight(); y++) {
                for (int x = 0; x < gameBoard.maker.getWidth(); x++) {
                    Group group = new Group();

                    //ImageViewをクリックすると追加できるように
                    ImageView imageView1 = new ImageView(OwnerToImageConverter.convert(Owner.None));
                    int yy = y, xx = x;
                    group.setOnMouseClicked(event -> {
                        Point p = new Point(xx, yy);
                        if (!gameBoard.enemyPlayerSet.remove(p) && gameBoard.enemyPlayerSet.size() < 2) {//もう入っているなら消す、入っていないかつ2未満しか入っていないなら追加
                            gameBoard.enemyPlayerSet.add(p);
                        }
                        this.firstViewUpdate();
                    });

                    //スコアの数字を読み込む
                    ImageView imageView2 = new ImageView(ScoreToImageConverter.convert(gameBoard.getScore(x, y)));

                    group.getChildren().addAll(imageView1, imageView2);
                    controller.grid.add(group, x, y);
                }
            }

            //ボタンを押したら決定できるように
            controller.solveButton.setOnMouseClicked(event -> {
                if (gameBoard.decideEnemyPlayerPlace()) startNextPhase();
            });

            inputSide();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.firstViewUpdate();
    }

    /**
     * 敵プレイヤーの暫定位置を表示する
     */
    private void firstViewUpdate() {
        IntStream.range(0, gameBoard.maker.getHeight()).forEach(y -> IntStream.range(0, gameBoard.maker.getWidth()).forEach(x -> getTileImageViewFromGrid(x, y).setImage(OwnerToImageConverter.convert(Owner.None))));

        //プレイヤーの画像があるノードはプレイヤーの画像をImageViewごと削除
        controller.grid.getChildren().parallelStream().map(node -> (Group) node).filter(group -> group.getChildren().size() == 3).forEach(group -> group.getChildren().remove(2));

        //プレイヤーの画像を3層目にImageViewごと追加
        AtomicInteger i = new AtomicInteger();
        Stream.concat(Arrays.stream(gameBoard.players).filter(Objects::nonNull).map(Player::getNowPoint), gameBoard.enemyPlayerSet.stream()).forEach(point -> {
            ImageView playerView = new ImageView(images[i.getAndIncrement()]);
            getGroupFromGrid(point.x, point.y).getChildren().add(2, playerView);
        });

    }

    /**
     * 自分のチームが右か左か入力する
     */
    private void inputSide() {
        Scanner sc = new Scanner(System.in);
        int i;
        do {
            System.out.println("舞台側を上として、チームの位置は右か左か入力してください。");
            System.out.println("1：右    2：左");
            i = sc.nextInt();
        } while (!(i == 1 || i == 2));

        controller.rightSide.setText(i == 1 ? "味方" : "相手");
        controller.leftSide.setText(i == 1 ? "相手" : "味方");
    }

    /**
     * コントローラーのLabelを更新する
     */
    private void stringsUpdate() {
        controller.cnt1.setText("後" + gameBoard.getRemainTurnNumber() + "ターン");
        controller.cnt2.setText("今" + (gameBoard.maker.getTurnNumber() - gameBoard.getRemainTurnNumber()) + "ターン目");

        controller.winOrLose.setText("味方：" + gameBoard.getFriendScore() + "点  敵：" + gameBoard.getEnemyScore() + "点");
    }

    /**
     * ゲームのメインループに入る
     */
    private void startNextPhase() {
        gameBoard.calcScore();
        stringsUpdate();
        gameBoard.solve();
        clearAndSetEventHandler();
        reView();

        controller.solveButton.setOnMouseClicked(event1 -> {
            if (gameBoard.nextStage()) {
                stringsUpdate();
                gameBoard.solve();
            }
            clearAndSetEventHandler();
            reView();
        });
    }


    /**
     * GameBoardの所有者マップを基にimageViewの画像を変更する
     */
    private void reView() {
        //今のOwnerの画像に
        IntStream.range(0, gameBoard.maker.getHeight()).forEach(y -> IntStream.range(0, gameBoard.maker.getWidth()).forEach(x -> getTileImageViewFromGrid(x, y).setImage(OwnerToImageConverter.convert(gameBoard.getOwn(x, y)))));

        //プレイヤーの画像があるノードはプレイヤーの画像をImageViewごと削除
        controller.grid.getChildren().stream().filter(group -> ((Group) group).getChildren().size() == 3).forEach(group -> ((Group) group).getChildren().remove(2));
        //プレイヤーの画像をImageViewごと3層目に追加
        AtomicInteger i = new AtomicInteger();
        Arrays.stream(gameBoard.players).forEach(player -> {
            ImageView playerView = new ImageView(images[i.getAndIncrement()]);
            getGroupFromGrid(player.getNowPoint().x, player.getNowPoint().y).getChildren().add(2, playerView);
        });
    }

    /**
     * クリックイベントを全て削除する
     */
    private void clearAndSetEventHandler() {
        //全てのノードのクリックイベントを削除
        IntStream.range(0, gameBoard.maker.getHeight()).forEach(y -> IntStream.range(0, gameBoard.maker.getWidth()).forEach(x -> getGroupFromGrid(x, y).setOnMouseClicked(null)));

        //敵プレイヤーをクリックして行動を選べるように設定する
        Arrays.stream(gameBoard.players).skip(2L).forEach(this::setHandlerToSelect);

        //右の矢印を更新
        ImageView[] arr = {controller.fp1, controller.fp2, controller.ep1, controller.ep2};
        AtomicInteger i = new AtomicInteger();
        Arrays.stream(gameBoard.players).forEach(player -> arr[i.getAndIncrement()].setImage(PlayerSelectToImageConverter.convert(player.isFinishNextSelect() ? player.getXyDiff().toString() : "None")));
    }

    /**
     * 敵の動きを入力させるためにイベントハンドラを設定する
     */
    private void setHandlerToSelect(Player targetPlayer) {
        getGroupFromGrid(targetPlayer.getNowPoint().x, targetPlayer.getNowPoint().y).setOnMouseClicked(event1 -> {
            int[] diffX = {0, 1, 1, 0, -1, -1, -1, 0, 1}, diffY = {0, 0, 1, 1, 1, 0, -1, -1, -1};
            TransverseDiff[] diffTX = {TransverseDiff.Left, TransverseDiff.None, TransverseDiff.Right};
            LongitudinalDiff[] diffTY = {LongitudinalDiff.Down, LongitudinalDiff.None, LongitudinalDiff.Up};

            for (int i = 0; i < 9; i++) {
                int targetY = targetPlayer.getNowPoint().y + diffY[i], targetX = targetPlayer.getNowPoint().x + diffX[i];
                //範囲内なら
                if (targetY >= 0 && targetY < gameBoard.maker.getHeight() && targetX >= 0 && targetX < gameBoard.maker.getWidth()) {
                    int dy = diffY[i], dx = diffX[i];
                    getGroupFromGrid(targetX, targetY).setOnMouseClicked(event2 -> {
                        targetPlayer.select(gameBoard.getOwn(targetX, targetY) == Owner.Friend ? Selection.REMOVE : Selection.MOVE, new XYDiff(diffTY[dy + 1], diffTX[dx + 1]));
                        clearAndSetEventHandler();
                    });
                }
            }
        });
    }

    private ImageView getTileImageViewFromGrid(int x, int y) {
        return (ImageView) getGroupFromGrid(x, y).getChildren().get(0);
    }

    private ImageView getNumImageViewFromGrid(int x, int y) {
        return (ImageView) getGroupFromGrid(x, y).getChildren().get(1);
    }

    private Group getGroupFromGrid(int x, int y) {
        return (Group) controller.grid.getChildren().get(gameBoard.maker.getWidth() * y + x);
    }

    /**
     * 表示の為に現在のゲーム画面のGUIを返す
     *
     * @return GUIの基底
     */
    public Parent getView() {
        return root;
    }

}
