package ru.ekaerovets.pinyindroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class PinyinView extends View {

    public static final double DIFF_MIN = 0.0015;
    public static final double DIFF_MAX = 2;

    DataHolder holder;
    ShowItemCallback showCallback;

    Item[] data = new Item[10];

    Paint p = new Paint();

    float touchX;
    float touchY;

    boolean isChars;

    public PinyinView(Context context) {
        super(context);
    }

    public PinyinView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinyinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIsChars(boolean isChars) {
        this.isChars = isChars;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int ziNormal = Color.rgb(60, 60, 60);
        int ziDiff = Color.RED;

        p.setTextSize(60);
        p.setAntiAlias(true);

        for (int i = 0; i < 10; i++) {
            Item pin = data[i];
            if (pin == null) {
                continue;
            }
            int x = i * 70 - 80;
            p.setColor(pin.getAnswerStatus() == 1 ? ziDiff : ziNormal);
            canvas.drawText(pin.getKey(), x, 90, p);
        }

        p.setTextSize(24);
        p.setColor(Color.rgb(110, 110, 110));

        int from = isChars ? 4 : 0;
        for (int i = from; i < 5; i++) {
            Item pin = data[i];
            if (pin == null) {
                continue;
            }
            int x = i * 70 - 80;
            for (int j = 0; j < pin.getValue().size(); j++) {
                String text = pin.getValue().get(j);
                float w = p.measureText(text);
                canvas.drawText(text, x + 28 - w / 2, 120 + 20 * j, p);
            }
            if (pin.isMark()) {
                float w = p.measureText("**");
                canvas.drawText("**", x + 28 - w / 2, 120 + 20 * pin.getValue().size(), p);
            }
        }

    }

    public void setShowCallback(ShowItemCallback callback) {
        this.showCallback = callback;
    }

    public void attachDataHolder(DataHolder holder) {
        this.holder = holder;
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                data[i] = null;
            } else {
                data[i] = holder.fetchItem();
            }
        }
        invalidate();
    }

    public double randNear() {
        double r = Math.random();
        return 0.97 + 0.06 * r;
    }

    public void freeDataHolder() {
        if (holder != null) {
            for (int i = 0; i < 10; i++) {
                Item p = data[i];
                if (p == null) {
                    continue;
                }
                if (i < 5) {
                    updateDiff(p, p.getAnswerStatus() == 1);
                }
                holder.freeChar(p);
            }
        }
    }

    public void toggle() {
        if (data[4] != null) {
            data[4].setAnswerStatus(1 - data[4].getAnswerStatus());
            invalidate();
        }
    }

    private void updateDiff(Item p, boolean isDiff) {
        if (isDiff) {
            p.setDiff(p.getDiff() + 0.4);
            if (p.getDiff() > 2) {
                p.setDiff(2);
            }
        } else {
            p.setDiff(p.getDiff() / 3.0);
            if (p.getDiff() < (1 / 2500.0)) {
                p.setDiff(-1);
                p.setStage(1);
            }
        }
    }

    public void shiftLeft() {
        Item p = data[0];
        if (p != null) {
            updateDiff(p, p.getAnswerStatus() == 1);
            p.setAnswerStatus(0);
            holder.freeChar(p);
        }
        System.arraycopy(data, 1, data, 0, 9);
        data[9] = holder.fetchItem();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action != MotionEvent.ACTION_DOWN) {
            return true;
        }
        touchX = event.getX();
        touchY = event.getY();

        if (touchY < 30 || touchY > 220) {
            return true;
        }

        boolean bottom = touchY > 120;
        int index = (int) ((touchX + 80) / 70);
        if (index > 4) {
            return true;
        }

        if (bottom && data[index] != null) {
            showCallback.show(data[index]);
        } else {
            if (data[index] != null) {
                data[index].setAnswerStatus(1 - data[index].getAnswerStatus());
            }
        }

        invalidate();
        return true;
    }
}
