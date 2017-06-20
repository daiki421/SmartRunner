package com.sony.smarteyeglass.extension.displaysetting;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by daiki on 2017/06/07.
 */

public class RoundWriter {

    private final BufferedWriter writer;
    public RoundWriter(final String filename) throws IOException {
        writer = new BufferedWriter(new FileWriter(filename));
    }

    public void writeTitle(String title) throws IOException {
        writer.write(title);
        writer.newLine();
    }

    public void write(double distance, long time, double longitude, double latitude) throws IOException {
        writer.write(Double.toString(distance)+",");
        writer.write(Long.toString(time)+",");
        writer.write(Double.toString(longitude)+",");
        writer.write(Double.toString(latitude));
        writer.newLine();
    }

    public void flush() throws IOException {
        writer.flush();
    }
}
