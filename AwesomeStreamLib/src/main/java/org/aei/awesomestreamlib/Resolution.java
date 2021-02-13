package org.aei.awesomestreamlib;

public class Resolution {
    private final int height,width;

    public Resolution(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
