package com.sony.smarteyeglass.extension.displaysetting;

/**
 * Created by daiki on 2017/06/07.
 */

public class Round {
    // csvファイルにタイトルと一巡ごとのデータを格納

    public final String title;
    public final Point[] points;

    public Round(String title, Point[] points) {
        this.title = title;
        this.points = points;
    }

    public static class Point {
        public final double distance;
        public final long time;

        public Point(double distance, long time) {
            this.distance = distance;
            this.time = time;
        }
    }
}
