import org.junit.Test;
import procon29.akashi.scores.ScoreFromRandom;
import procon29.akashi.scores.ScoreMaker;

import java.util.Arrays;

import static org.junit.Assert.fail;

public class ScoreFromRandomTest {
    ScoreMaker maker;

    @Test(expected = IllegalArgumentException.class)
    public void heightOver12FailTest() {
        maker = new ScoreFromRandom(13, 11, true, true, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthOver12FailTest() {
        maker = new ScoreFromRandom(11, 13, true, true, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void heightUnder0FailTest() {
        maker = new ScoreFromRandom(0, 5, true, true, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthUnder0FailTest() {
        maker = new ScoreFromRandom(5, 0, true, true, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthMultipulHeightUnder80FailTest() {
        maker = new ScoreFromRandom(8, 9, true, true, 1);
    }

    @Test
    public void canNewScoreMaker() {
        maker = new ScoreFromRandom(10, 9, true, true, 1);
    }

    @Test
    public void makeV_SymTrue_And_H_SymTrueTest() {
        maker = new ScoreFromRandom(9, 10, true, true, 1);
        int[][] arr = maker.getMap();
        VSymmetryCheck(arr);
        HSymmetryCheck(arr);
    }

    /**
     * 水平な中央線で線対称になっているか判定するヘルパーメソッド
     *
     * @param arr 検証したい2次元配列
     */
    private void HSymmetryCheck(int[][] arr) {
        for (int up = 0, down = arr.length - 1; up < arr.length; up++, down--) {
            if (!Arrays.equals(arr[up], arr[down])) {
                fail();
            }
        }
    }

    /**
     * 垂直な中央線で線対称になっているか判定するヘルパーメソッド
     *
     * @param arr 検証したい2次元配列
     */
    private void VSymmetryCheck(int[][] arr) {
        for (int[] line : arr) {
            for (int left = 0, right = line.length - 1; left < line.length; left++, right--) {
                if (line[left] != line[right]) {
                    fail();
                }
            }
        }
    }

}