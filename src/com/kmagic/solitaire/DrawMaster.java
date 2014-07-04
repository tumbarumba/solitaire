/*
  Copyright 2008 Google Inc.
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.kmagic.solitaire;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;


public class DrawMaster {

    // Card stuff
    private final Paint suitPaint = new Paint();
    private Context context;

    // Background
    private int screenWidth;
    private int screenHeight;
    private Paint bgPaint;
    private Bitmap[] cardBitmaps;
    private Bitmap cardHidden;

    private Paint emptyAnchorPaint;
    private Paint doneEmptyAnchorPaint;
    private Paint shadePaint;
    private Paint lightShadePaint;

    private Paint timePaint;
    private int lastSeconds;
    private String timeString;

    private Bitmap boardBitmap;
    private Canvas boardCanvas;

    public DrawMaster(Context theContext) {

        context = theContext;
        // Default to this for simplicity
        screenWidth = 480;
        screenHeight = 295;

        // Background
        bgPaint = new Paint();
        bgPaint.setARGB(255, 0, 128, 0);

        shadePaint = new Paint();
        shadePaint.setARGB(200, 0, 0, 0);

        lightShadePaint = new Paint();
        lightShadePaint.setARGB(100, 0, 0, 0);

        // Card related stuff
        emptyAnchorPaint = new Paint();
        emptyAnchorPaint.setARGB(255, 0, 64, 0);
        doneEmptyAnchorPaint = new Paint();
        doneEmptyAnchorPaint.setARGB(128, 255, 0, 0);

        timePaint = new Paint();
        timePaint.setTextSize(18);
        timePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        timePaint.setTextAlign(Paint.Align.RIGHT);
        timePaint.setAntiAlias(true);
        lastSeconds = -1;

        cardBitmaps = new Bitmap[52];
        drawCards(false);
        boardBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        boardCanvas = new Canvas(boardBitmap);
    }

    public int getWidth() {
        return screenWidth;
    }

    public int getHeight() {
        return screenHeight;
    }

    public Canvas getBoardCanvas() {
        return boardCanvas;
    }

    public void drawCard(Canvas canvas, Card card) {
        float x = card.getX();
        float y = card.getY();
        int idx = card.getSuit() * 13 + (card.getValue() - 1);
        canvas.drawBitmap(cardBitmaps[idx], x, y, suitPaint);
    }

    public void drawHiddenCard(Canvas canvas, Card card) {
        float x = card.getX();
        float y = card.getY();
        canvas.drawBitmap(cardHidden, x, y, suitPaint);
    }

    public void drawEmptyAnchor(Canvas canvas, float x, float y, boolean done) {
        RectF pos = new RectF(x, y, x + Card.WIDTH, y + Card.HEIGHT);
        if (!done) {
            canvas.drawRoundRect(pos, 4, 4, emptyAnchorPaint);
        } else {
            canvas.drawRoundRect(pos, 4, 4, doneEmptyAnchorPaint);
        }
    }

    public void drawBackground(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, bgPaint);
    }

    public void drawShade(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, shadePaint);
    }

    public void drawLightShade(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, lightShadePaint);
    }

    public void drawLastBoard(Canvas canvas) {
        canvas.drawBitmap(boardBitmap, 0, 0, suitPaint);
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        boardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        boardCanvas = new Canvas(boardBitmap);
    }

    public void drawCards(boolean bigCards) {
        if (bigCards) {
            drawBigCards(context.getResources());
        } else {
            drawCards(context.getResources());
        }
    }

    private void drawBigCards(Resources r) {

        Paint cardFrontPaint = new Paint();
        Paint cardBorderPaint = new Paint();
        Bitmap[] bigSuit = new Bitmap[4];
        Bitmap[] suit = new Bitmap[4];
        Bitmap[] blackFont = new Bitmap[13];
        Bitmap[] redFont = new Bitmap[13];
        Canvas canvas;
        int width = Card.WIDTH;
        int height = Card.HEIGHT;

        Drawable drawable = r.getDrawable(R.drawable.cardback);

        cardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                Bitmap.Config.ARGB_4444);
        canvas = new Canvas(cardHidden);
        drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.suits);
        for (int i = 0; i < 4; i++) {
            suit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(suit[i]);
            drawable.setBounds(-i * 10, 0, -i * 10 + 40, 10);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.bigsuits);
        for (int i = 0; i < 4; i++) {
            bigSuit[i] = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bigSuit[i]);
            drawable.setBounds(-i * 25, 0, -i * 25 + 100, 25);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.bigblackfont);
        for (int i = 0; i < 13; i++) {
            blackFont[i] = Bitmap.createBitmap(18, 15, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(blackFont[i]);
            drawable.setBounds(-i * 18, 0, -i * 18 + 234, 15);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.bigredfont);
        for (int i = 0; i < 13; i++) {
            redFont[i] = Bitmap.createBitmap(18, 15, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(redFont[i]);
            drawable.setBounds(-i * 18, 0, -i * 18 + 234, 15);
            drawable.draw(canvas);
        }

        cardBorderPaint.setARGB(255, 0, 0, 0);
        cardFrontPaint.setARGB(255, 255, 255, 255);
        RectF pos = new RectF();
        for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
            for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
                cardBitmaps[suitIdx * 13 + valueIdx] = Bitmap.createBitmap(
                        width, height, Bitmap.Config.ARGB_4444);
                canvas = new Canvas(cardBitmaps[suitIdx * 13 + valueIdx]);
                pos.set(0, 0, width, height);
                canvas.drawRoundRect(pos, 4, 4, cardBorderPaint);
                pos.set(1, 1, width - 1, height - 1);
                canvas.drawRoundRect(pos, 4, 4, cardFrontPaint);

                if ((suitIdx & 1) == 1) {
                    canvas.drawBitmap(redFont[valueIdx], 3, 4, suitPaint);
                } else {
                    canvas.drawBitmap(blackFont[valueIdx], 3, 4, suitPaint);
                }


                canvas.drawBitmap(suit[suitIdx], width - 14, 4, suitPaint);
                canvas.drawBitmap(bigSuit[suitIdx], width / 2 - 12, height / 2 - 13, suitPaint);
            }
        }
    }

    private void drawCards(Resources r) {

        Paint cardFrontPaint = new Paint();
        Paint cardBorderPaint = new Paint();
        Bitmap[] suit = new Bitmap[4];
        Bitmap[] revSuit = new Bitmap[4];
        Bitmap[] smallSuit = new Bitmap[4];
        Bitmap[] revSmallSuit = new Bitmap[4];
        Bitmap[] blackFont = new Bitmap[13];
        Bitmap[] revBlackFont = new Bitmap[13];
        Bitmap[] redFont = new Bitmap[13];
        Bitmap[] revRedFont = new Bitmap[13];
        Bitmap redJack;
        Bitmap redRevJack;
        Bitmap redQueen;
        Bitmap redRevQueen;
        Bitmap redKing;
        Bitmap redRevKing;
        Bitmap blackJack;
        Bitmap blackRevJack;
        Bitmap blackQueen;
        Bitmap blackRevQueen;
        Bitmap blackKing;
        Bitmap blackRevKing;
        Canvas canvas;
        int width = Card.WIDTH;
        int height = Card.HEIGHT;
        int fontWidth;
        int fontHeight;
        float[] faceBox = {9, 8, width - 10, 8,
                width - 10, 8, width - 10, height - 9,
                width - 10, height - 9, 9, height - 9,
                9, height - 8, 9, 8
        };
        Drawable drawable = r.getDrawable(R.drawable.cardback);

        cardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                Bitmap.Config.ARGB_4444);
        canvas = new Canvas(cardHidden);
        drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.suits);
        for (int i = 0; i < 4; i++) {
            suit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
            revSuit[i] = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(suit[i]);
            drawable.setBounds(-i * 10, 0, -i * 10 + 40, 10);
            drawable.draw(canvas);
            canvas = new Canvas(revSuit[i]);
            canvas.rotate(180);
            drawable.setBounds(-i * 10 - 10, -10, -i * 10 + 30, 0);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.smallsuits);
        for (int i = 0; i < 4; i++) {
            smallSuit[i] = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_4444);
            revSmallSuit[i] = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(smallSuit[i]);
            drawable.setBounds(-i * 5, 0, -i * 5 + 20, 5);
            drawable.draw(canvas);
            canvas = new Canvas(revSmallSuit[i]);
            canvas.rotate(180);
            drawable.setBounds(-i * 5 - 5, -5, -i * 5 + 15, 0);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.medblackfont);
        fontWidth = 7;
        fontHeight = 9;
        for (int i = 0; i < 13; i++) {
            blackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
            revBlackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(blackFont[i]);
            drawable.setBounds(-i * fontWidth, 0, -i * fontWidth + 13 * fontWidth, fontHeight);
            drawable.draw(canvas);
            canvas = new Canvas(revBlackFont[i]);
            canvas.rotate(180);
            drawable.setBounds(-i * fontWidth - fontWidth, -fontHeight, -i * fontWidth + (12 * fontWidth), 0);
            drawable.draw(canvas);
        }

        drawable = r.getDrawable(R.drawable.medredfont);
        for (int i = 0; i < 13; i++) {
            redFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
            revRedFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(redFont[i]);
            drawable.setBounds(-i * fontWidth, 0, -i * fontWidth + 13 * fontWidth, fontHeight);
            drawable.draw(canvas);
            canvas = new Canvas(revRedFont[i]);
            canvas.rotate(180);
            drawable.setBounds(-i * fontWidth - fontWidth, -fontHeight, -i * fontWidth + (12 * fontWidth), 0);
            drawable.draw(canvas);
        }

        int faceWidth = width - 20;
        int faceHeight = height / 2 - 9;
        drawable = r.getDrawable(R.drawable.redjack);
        redJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        redRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(redJack);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(redRevJack);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.redqueen);
        redQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        redRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(redQueen);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(redRevQueen);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.redking);
        redKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        redRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(redKing);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(redRevKing);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.blackjack);
        blackJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        blackRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(blackJack);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(blackRevJack);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.blackqueen);
        blackQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        blackRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(blackQueen);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(blackRevQueen);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        drawable = r.getDrawable(R.drawable.blackking);
        blackKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        blackRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(blackKing);
        drawable.setBounds(0, 0, faceWidth, faceHeight);
        drawable.draw(canvas);
        canvas = new Canvas(blackRevKing);
        canvas.rotate(180);
        drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
        drawable.draw(canvas);

        cardBorderPaint.setARGB(255, 0, 0, 0);
        cardFrontPaint.setARGB(255, 255, 255, 255);
        RectF pos = new RectF();
        for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
            for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
                cardBitmaps[suitIdx * 13 + valueIdx] = Bitmap.createBitmap(
                        width, height, Bitmap.Config.ARGB_4444);
                canvas = new Canvas(cardBitmaps[suitIdx * 13 + valueIdx]);
                pos.set(0, 0, width, height);
                canvas.drawRoundRect(pos, 4, 4, cardBorderPaint);
                pos.set(1, 1, width - 1, height - 1);
                canvas.drawRoundRect(pos, 4, 4, cardFrontPaint);

                if ((suitIdx & 1) == 1) {
                    canvas.drawBitmap(redFont[valueIdx], 2, 4, suitPaint);
                    canvas.drawBitmap(revRedFont[valueIdx], width - fontWidth - 2, height - fontHeight - 4,
                            suitPaint);
                } else {
                    canvas.drawBitmap(blackFont[valueIdx], 2, 4, suitPaint);
                    canvas.drawBitmap(revBlackFont[valueIdx], width - fontWidth - 2, height - fontHeight - 4,
                            suitPaint);
                }
                if (fontWidth > 6) {
                    canvas.drawBitmap(smallSuit[suitIdx], 3, 5 + fontHeight, suitPaint);
                    canvas.drawBitmap(revSmallSuit[suitIdx], width - 7, height - 11 - fontHeight,
                            suitPaint);
                } else {
                    canvas.drawBitmap(smallSuit[suitIdx], 2, 5 + fontHeight, suitPaint);
                    canvas.drawBitmap(revSmallSuit[suitIdx], width - 6, height - 11 - fontHeight,
                            suitPaint);
                }

                if (valueIdx >= 10) {
                    canvas.drawBitmap(suit[suitIdx], 10, 9, suitPaint);
                    canvas.drawBitmap(revSuit[suitIdx], width - 21, height - 20,
                            suitPaint);
                }

                int[] suitX = {9, width / 2 - 5, width - 20};
                int[] suitY = {7, 2 * height / 5 - 5, 3 * height / 5 - 5, height - 18};
                int suitMidY = height / 2 - 6;
                switch (valueIdx + 1) {
                    case 1:
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, suitPaint);
                        break;
                    case 2:
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], suitPaint);
                        break;
                    case 3:
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], suitPaint);
                        break;
                    case 4:
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], suitPaint);
                        break;
                    case 5:
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], suitPaint);
                        break;
                    case 6:
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], suitPaint);
                        break;
                    case 7:
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY + suitY[0]) / 2, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], suitPaint);
                        break;
                    case 8:
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, suitPaint);
                        canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY + suitY[0]) / 2, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3] + suitMidY) / 2, suitPaint);
                        break;
                    case 9:
                        for (int i = 0; i < 4; i++) {
                            canvas.drawBitmap(suit[suitIdx], suitX[(i % 2) * 2], suitY[i / 2], suitPaint);
                            canvas.drawBitmap(revSuit[suitIdx], suitX[(i % 2) * 2], suitY[i / 2 + 2], suitPaint);
                        }
                        canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, suitPaint);
                        break;
                    case 10:
                        for (int i = 0; i < 4; i++) {
                            canvas.drawBitmap(suit[suitIdx], suitX[(i % 2) * 2], suitY[i / 2], suitPaint);
                            canvas.drawBitmap(revSuit[suitIdx], suitX[(i % 2) * 2], suitY[i / 2 + 2], suitPaint);
                        }
                        canvas.drawBitmap(suit[suitIdx], suitX[1], (suitY[1] + suitY[0]) / 2, suitPaint);
                        canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3] + suitY[2]) / 2, suitPaint);
                        break;

                    case Card.JACK:
                        canvas.drawLines(faceBox, cardBorderPaint);
                        if ((suitIdx & 1) == 1) {
                            canvas.drawBitmap(redJack, 10, 9, suitPaint);
                            canvas.drawBitmap(redRevJack, 10, height - faceHeight - 9, suitPaint);
                        } else {
                            canvas.drawBitmap(blackJack, 10, 9, suitPaint);
                            canvas.drawBitmap(blackRevJack, 10, height - faceHeight - 9, suitPaint);
                        }
                        break;
                    case Card.QUEEN:
                        canvas.drawLines(faceBox, cardBorderPaint);
                        if ((suitIdx & 1) == 1) {
                            canvas.drawBitmap(redQueen, 10, 9, suitPaint);
                            canvas.drawBitmap(redRevQueen, 10, height - faceHeight - 9, suitPaint);
                        } else {
                            canvas.drawBitmap(blackQueen, 10, 9, suitPaint);
                            canvas.drawBitmap(blackRevQueen, 10, height - faceHeight - 9, suitPaint);
                        }
                        break;
                    case Card.KING:
                        canvas.drawLines(faceBox, cardBorderPaint);
                        if ((suitIdx & 1) == 1) {
                            canvas.drawBitmap(redKing, 10, 9, suitPaint);
                            canvas.drawBitmap(redRevKing, 10, height - faceHeight - 9, suitPaint);
                        } else {
                            canvas.drawBitmap(blackKing, 10, 9, suitPaint);
                            canvas.drawBitmap(blackRevKing, 10, height - faceHeight - 9, suitPaint);
                        }
                        break;
                }
            }
        }
    }

    public void drawTime(Canvas canvas, int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = millis / 60000;
        if (seconds != lastSeconds) {
            lastSeconds = seconds;
            // String.format is insanely slow (~15ms)
            if (seconds < 10) {
                timeString = minutes + ":0" + seconds;
            } else {
                timeString = minutes + ":" + seconds;
            }
        }
        timePaint.setARGB(255, 20, 20, 20);
        canvas.drawText(timeString, screenWidth - 9, screenHeight - 9, timePaint);
        timePaint.setARGB(255, 0, 0, 0);
        canvas.drawText(timeString, screenWidth - 10, screenHeight - 10, timePaint);
    }

    public void drawRulesString(Canvas canvas, String score) {
        timePaint.setARGB(255, 20, 20, 20);
        canvas.drawText(score, screenWidth - 9, screenHeight - 29, timePaint);
        if (score.charAt(0) == '-') {
            timePaint.setARGB(255, 255, 0, 0);
        } else {
            timePaint.setARGB(255, 0, 0, 0);
        }
        canvas.drawText(score, screenWidth - 10, screenHeight - 30, timePaint);

    }
}
