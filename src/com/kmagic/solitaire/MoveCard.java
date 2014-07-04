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

import android.graphics.Canvas;
import android.graphics.PointF;


class MoveCard {

    private static final int MAX_CARDS = 13;

    private boolean isValid;
    private Card[] cards;
    private int cardCount;
    private CardAnchor cardAnchor;
    private PointF originalPoint;

    public MoveCard() {
        cards = new Card[MAX_CARDS];
        originalPoint = new PointF(1, 1);
        clear();
    }

    public CardAnchor getAnchor() {
        return cardAnchor;
    }

    public int getCount() {
        return cardCount;
    }

    public Card getTopCard() {
        return cards[0];
    }

    public void setAnchor(CardAnchor anchor) {
        cardAnchor = anchor;
    }

    public void draw(DrawMaster drawMaster, Canvas canvas) {
        for (int i = 0; i < cardCount; i++) {
            drawMaster.drawCard(canvas, cards[i]);
        }
    }

    private void clear() {
        isValid = false;
        cardCount = 0;
        cardAnchor = null;
        for (int i = 0; i < MAX_CARDS; i++) {
            cards[i] = null;
        }
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

    public void addCard(Card card) {
        if (cardCount == 0) {
            originalPoint.set(card.getX(), card.getY());
        }
        cards[cardCount++] = card;
        isValid = true;
    }

    public void movePosition(float dx, float dy) {
        for (int i = 0; i < cardCount; i++) {
            cards[i].movePosition(dx, dy);
        }
    }

    public Card[] dumpCards() {
        return dumpCards(true);
    }

    public Card[] dumpCards(boolean unhide) {
        Card[] ret = null;
        if (isValid) {
            isValid = false;
            if (unhide) {
                cardAnchor.unhideTopCard();
            }
            ret = new Card[cardCount];
            for (int i = 0; i < cardCount; i++) {
                ret[i] = cards[i];
            }
            clear();
        }
        return ret;
    }

    public void initFromSelectCard(SelectCard selectCard, float x, float y) {
        int count = selectCard.getCount();
        cardAnchor = selectCard.getAnchor();
        Card[] cards = selectCard.dumpCards();

        for (int i = 0; i < count; i++) {
            cards[i].setPosition(x - Card.WIDTH / 2, y - Card.HEIGHT / 2 + 15 * i);
            addCard(cards[i]);
        }
        isValid = true;
    }

    public void initFromAnchor(CardAnchor cardAnchor, float x, float y) {
        this.cardAnchor = cardAnchor;
        Card[] cards = cardAnchor.getCardStack();

        for (int i = 0; i < cards.length; i++) {
            cards[i].setPosition(x, y + 15 * i);
            addCard(cards[i]);
        }
        isValid = true;
    }

    public boolean hasMoved() {
        float x = cards[0].getX();
        float y = cards[0].getY();

        if (x >= originalPoint.x - 2 && x <= originalPoint.x + 2 &&
                y >= originalPoint.y - 2 && y <= originalPoint.y + 2) {
            return false;
        }
        return true;
    }
}

