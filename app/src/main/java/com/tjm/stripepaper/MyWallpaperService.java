package com.tjm.stripepaper;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class MyWallpaperService extends WallpaperService {

    private final Handler mHandler = new Handler();
    public static MyWallpaperEngine INSTANCE;

    @Override
    public Engine onCreateEngine() {
        INSTANCE = new MyWallpaperEngine();
        return INSTANCE;
    }

    public class MyWallpaperEngine extends Engine {
        private List<Stripe> stripes;
        private Paint paint = new Paint();
        private SharedPreferences prefs;
        private boolean mVisible;
        private TypedArray colors;
        private String currentTheme;
        private float width;
        private float widthRange;
        private float speed;
        private float speedRange;
        private int maxNumber = 200;
        private final Runnable mDraw = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        public MyWallpaperEngine() {
            prefs = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
            stripes = new ArrayList<Stripe>();
            paint.setStyle(Paint.Style.FILL);
            checkForChanges();
            refreshFrame();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDraw);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                checkForChanges();
                drawFrame();
            } else {
                mHandler.removeCallbacks(mDraw);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDraw);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            boolean touchEnabled = prefs.getBoolean("touch", true);

            if (touchEnabled && event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float s = Double.valueOf(speed + (Math.random()>0.5 ? -speedRange : speedRange)).floatValue();
                s *= (Math.random()>0.5 ? -1 : 1);
                float w = Double.valueOf(width + (Math.random()>0.5 ? -widthRange : widthRange)).floatValue();
                stripes.add(new Stripe(w, x, getRandomColor(), s));

                if (stripes.size() >= maxNumber) {
                    int difference = stripes.size() - maxNumber;
                    stripes = stripes.subList(difference, stripes.size());
                }
                super.onTouchEvent(event);
            }
        }

        private void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    checkForChanges();
                    for (Stripe point : stripes) {
                        point.x += point.speed;
                        if (point.x > c.getWidth()) {
                            point.x = c.getWidth();
                            float s = Double.valueOf(speed + (Math.random() > 0.5 ? -speedRange : speedRange)).floatValue();
                            if (s > 0) {
                                s *= -1;
                            }
                            point.speed = s;
                        } else if (point.x < 0) {
                            point.x = 0;
                            float s = Double.valueOf(speed + (Math.random() > 0.5 ? -speedRange : speedRange)).floatValue();
                            if (s < 0) {
                                s += -1;
                            }
                            point.speed = s;
                        }
                    }
                    drawStripes(c, stripes);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            // Reschedule the next redraw
            mHandler.removeCallbacks(mDraw);
            if (mVisible) {
                mHandler.postDelayed(mDraw, 5);
            }
        }

        private void refreshFrame() {
            stripes.clear();
            for(int i = 0; i < maxNumber; i++) {
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                float x = Double.valueOf(Math.random()*display.getWidth()).floatValue();
                float s = Double.valueOf(speed + (Math.random()>0.5 ? -speedRange : speedRange)).floatValue();
                s *= (Math.random()>0.5 ? -1 : 1);
                float w = Double.valueOf(width + (Math.random()>0.5 ? -widthRange : widthRange)).floatValue();
                stripes.add(new Stripe(w, x, getRandomColor(), s));
            }
        }

        private void drawStripes(Canvas canvas, List<Stripe> stripes) {
            canvas.drawColor(Color.BLACK);
            for (Stripe stripe : stripes) {
                paint.setColor(stripe.color);
                float screenHeight = canvas.getHeight();
                canvas.drawRect(stripe.x - stripe.size, 0, stripe.x + stripe.size, screenHeight, paint);
            }
        }

        private int getRandomColor() {
            double val = Math.round(Math.random() * (colors.length()-1));
            return colors.getColor((int)val, 0);
        }

        private void checkForChanges() {
            boolean change = false;
            String theme = prefs.getString("theme", "algeria");
            if(!theme.equalsIgnoreCase(currentTheme)) {
                currentTheme = theme;
                int id = getResources().getIdentifier(currentTheme, "array", getPackageName());
                colors = getResources().obtainTypedArray(id);
                change = true;
            }
            float w = Float.parseFloat(prefs.getString("width", "50"));
            if(w != width) {
                width = w;
                change = true;
            }
            float wR = Float.parseFloat(prefs.getString("widthRange", "20"));
            if(wR != widthRange) {
                widthRange = wR;
                change = true;
            }
            float s = Float.parseFloat(prefs.getString("speed", "1")) / 50;
            if(s != speed) {
                speed = s;
                change = true;
            }
            float sR = Float.parseFloat(prefs.getString("speedRange", "2")) / 50;
            if(sR != speedRange) {
                speedRange = sR;
                change = true;
            }

            if(change) {
                refreshFrame();
            }
        }
    }
}