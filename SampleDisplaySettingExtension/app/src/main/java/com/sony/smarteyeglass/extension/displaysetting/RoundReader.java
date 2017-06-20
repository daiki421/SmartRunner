package com.sony.smarteyeglass.extension.displaysetting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by daiki on 2017/06/06.
 */

public class RoundReader {
    private final BufferedReader reader;

    public RoundReader(final String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
    }

    public Round read() throws IOException {
        String title = reader.readLine();
        String point;
        Round round;
        String[] points;
        ArrayList<Round.Point> roundPoints = new ArrayList<Round.Point>();
        while((point = reader.readLine()) != null) {
            points = point.split(",");
            roundPoints.add(new Round.Point(Double.valueOf(points[0]), Long.valueOf(points[1])));
        }
        round = new Round(title, roundPoints.toArray(new Round.Point[roundPoints.size()]));
        return round;
    }
}
