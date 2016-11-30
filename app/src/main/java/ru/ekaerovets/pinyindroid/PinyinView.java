package ru.ekaerovets.pinyindroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class PinyinView extends View {

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

        int ziTrivia = Color.GREEN;

        int ziReview = Color.rgb(13, 84, 0);
        int ziQueue = Color.rgb(114, 0, 95);

        p.setTextSize(60);
        p.setAntiAlias(true);

        for (int i = 0; i < 10; i++) {
            Item pin = data[i];
            if (pin == null) {
                continue;
            }
            int x = i * 70 - 80;
            if (pin.getAnswerStatus() == Difficulty.DIFFICULT) {
                p.setColor(ziDiff);
            } else if (pin.getAnswerStatus() == Difficulty.TRIVIAL) {
                p.setColor(ziTrivia);
            } else if (pin.getAnswerStatus() == Difficulty.REVIEW) {
                p.setColor(ziReview);
            } else if (pin.getAnswerStatus() == Difficulty.QUEUED) {
                p.setColor(ziQueue);
            } else {
                p.setColor(ziNormal);
            }

            canvas.drawText(pin.getKey(), x, 90, p);
        }

        p.setTextSize(24);
        p.setColor(Color.rgb(110, 110, 110));

        int from = isChars ? 4 : 0;
        for (int i = from; i < 5; i++) {
            Item item = data[i];
            if (item == null) {
                continue;
            }
            int x = i * 70 - 80;
            for (int j = 0; j < item.getValue().size(); j++) {
                String text = item.getValue().get(j);
                float w = p.measureText(text);
                canvas.drawText(text, x + 28 - w / 2, 120 + 20 * j, p);
            }
            if (item.isMark()) {
                float w = p.measureText("**");
                canvas.drawText("**", x + 28 - w / 2, 120 + 20 * item.getValue().size(), p);
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

    public void setTrivia() {
        if (data[4] != null) {
            data[4].setAnswerStatus(Difficulty.TRIVIAL);
        }
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
                Difficulty prevStatus = data[index].getAnswerStatus();
                if (prevStatus == Difficulty.REVIEW) {
                    data[index].setAnswerStatus(Difficulty.QUEUED);
                } else if (prevStatus == Difficulty.QUEUED) {
                    data[index].setAnswerStatus(Difficulty.REVIEW);
                } else {
                    data[index].setAnswerStatus(prevStatus != Difficulty.DIFFICULT ?
                            Difficulty.DIFFICULT : null);
                }
            }
        }

        invalidate();
        return true;
    }
}
