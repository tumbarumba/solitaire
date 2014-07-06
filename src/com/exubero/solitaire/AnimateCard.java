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
package com.exubero.solitaire;

import android.graphics.Canvas;

public class AnimateCard {

    private static final float PPF = 40;

    protected SolitaireView view;
    private Card[] cards;
    private CardAnchor cardAnchor;
    private int count;
    private int frames;
    private float dx;
    private float dy;
    private boolean isAnimating;
    private Runnable callback;

    public AnimateCard(SolitaireView view) {
        this.view = view;
        this.isAnimating = false;
        this.cards = new Card[104];
        this.callback = null;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (isAnimating) {
            for (int j = 0; j < count; j++) {
                cards[j].movePosition(-dx, -dy);
            }
            for (int i = 0; i < count; i++) {
                drawMaster.drawCard(canvas, cards[i]);
            }
            frames--;
            if (frames <= 0) {
                isAnimating = false;
                finish();
            }
        }
    }

    public void moveCards(Card[] cardsToMove, CardAnchor anAnchor, int cardCount, Runnable aCallback) {
        float x = anAnchor.getX();
        float y = anAnchor.getNewY();
        cardAnchor = anAnchor;
        callback = aCallback;
        isAnimating = true;

        for (int i = 0; i < cardCount; i++) {
            cards[i] = cardsToMove[i];
        }
        count = cardCount;
        move(cards[0], x, y);
    }

    public void moveCard(Card theCard, CardAnchor theAnchor) {
        float x = theAnchor.getX();
        float y = theAnchor.getNewY();
        cardAnchor = theAnchor;
        callback = null;
        isAnimating = true;

        cards[0] = theCard;
        count = 1;
        move(theCard, x, y);
    }

    private void move(Card card, float x, float y) {
        float distanceX = x - card.getX();
        float distanceY = y - card.getY();

        frames = Math.round((float) Math.sqrt(distanceX * distanceX + distanceY * distanceY) / PPF);
        if (frames == 0) {
            frames = 1;
        }
        dx = distanceX / frames;
        dy = distanceY / frames;

        view.startAnimating();
        if (!isAnimating) {
            finish();
        }
    }

    private void finish() {
        for (int i = 0; i < count; i++) {
            cardAnchor.addCard(cards[i]);
            cards[i] = null;
        }
        cardAnchor = null;
        view.drawBoard();
        if (callback != null) {
            callback.run();
        }
    }

    public void cancel() {
        if (isAnimating) {
            for (int i = 0; i < count; i++) {
                cardAnchor.addCard(cards[i]);
                cards[i] = null;
            }
            cardAnchor = null;
            isAnimating = false;
        }
    }
}
