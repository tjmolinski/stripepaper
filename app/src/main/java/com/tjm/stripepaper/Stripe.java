package com.tjm.stripepaper;

/**
 * Created by cymak on 7/9/14.
 */
public class Stripe {
    public float size;
    public int color;
    public float speed;
    public float x;

    public Stripe(float size, float x, int color, float speed) {
        this.size = size;
        this.x = x;
        this.color = color;
        this.speed = speed;
    }
}