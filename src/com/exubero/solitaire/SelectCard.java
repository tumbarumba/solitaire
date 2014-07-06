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

class SelectCard {

    private static final int MAX_CARDS = 13;

    private boolean isValid;
    private int selected;
    private Card[] cards;
    private int cardCount;
    private CardAnchor cardAnchor;
    private float leftEdge;
    private float rightEdge;
    private int height;

    public SelectCard() {
        height = 1;
        cards = new Card[MAX_CARDS];
        clear();
    }

    private void clear() {
        isValid = false;
        selected = -1;
        cardCount = 0;
        leftEdge = -1;
        rightEdge = -1;
        cardAnchor = null;
        for (int i = 0; i < MAX_CARDS; i++) {
            cards[i] = null;
        }
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public CardAnchor getAnchor() {
        return cardAnchor;
    }

    public int getCount() {
        if (selected == -1)
            return cardCount;
        return cardCount - selected;
    }

    public void draw(DrawMaster drawMaster, Canvas canvas) {
        drawMaster.drawLightShade(canvas);
        for (int i = 0; i < cardCount; i++) {
            drawMaster.drawCard(canvas, cards[i]);
        }
    }

    public void initFromAnchor(CardAnchor cardAnchor) {
        isValid = true;
        selected = -1;
        this.cardAnchor = cardAnchor;
        Card[] card = cardAnchor.getCardStack();
        for (int i = 0; i < card.length; i++) {
            cards[i] = card[i];
        }
        cardCount = card.length;

        int mid = cardCount / 2;
        if (cardCount % 2 == 0) {
            mid--;
        }
        float x = cards[0].getX();
        float y = cards[mid].getY();
        if (y - mid * (Card.HEIGHT + 5) < 0) {
            mid = 0;
            y = 5;
        }

        for (int i = 0; i < cardCount; i++) {
            cards[i].setPosition(x, y + (i - mid) * (Card.HEIGHT + 5));
        }

        leftEdge = cardAnchor.getLeftEdge();
        rightEdge = cardAnchor.getRightEdge();
    }

    public boolean tap(float x, float y) {
        float left = leftEdge == -1 ? cards[0].getX() : leftEdge;
        float right = rightEdge == -1 ? cards[0].getX() + Card.WIDTH : rightEdge;
        selected = -1;
        if (x >= left && x <= right) {
            for (int i = 0; i < cardCount; i++) {
                if (y >= cards[i].getY() && y <= cards[i].getY() + Card.HEIGHT) {
                    selected = i;
                    return true;
                }
            }
        }
        return false;
    }

    public void release() {
        if (isValid) {
            isValid = false;
            for (int i = 0; i < cardCount; i++) {
                cardAnchor.addCard(cards[i]);
            }
            clear();
        }
    }

    public Card[] dumpCards() {
        Card[] ret = null;
        if (isValid) {
            isValid = false;
            if (selected > 0) {
                for (int i = 0; i < cardCount; i++) {
                    if (i < selected) {
                        cardAnchor.addCard(cards[i]);
                    } else if (i == selected) {
                        for (int j = 0; i < cardCount; i++, j++) {
                            cards[j] = cards[i];
                        }
                        break;
                    }
                }
            }

            ret = new Card[getCount()];
            for (int i = 0; i < getCount(); i++) {
                ret[i] = cards[i];
            }
            clear();
        }
        return ret;
    }

    public void scroll(float dy) {
        float x, y;
        for (int i = 0; i < cardCount; i++) {
            x = cards[i].getX();
            y = cards[i].getY() - dy;
            cards[i].setPosition(x, y);
        }
    }

    public boolean isOnCard() {
        return selected != -1;
    }
}


