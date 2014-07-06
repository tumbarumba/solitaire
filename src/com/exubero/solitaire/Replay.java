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

import android.util.Log;

import java.util.Stack;

public class Replay implements Runnable {
    private Stack<Move> moveStack;
    private SolitaireView view;
    private AnimateCard animateCard;
    private CardAnchor[] cardAnchor;
    private boolean isPlaying;

    private Card[] sinkCards;
    private int sinkCount;
    private CardAnchor sinkAnchor;
    private CardAnchor sinkFrom;
    private boolean sinkUnhide;

    public Replay(SolitaireView theView, AnimateCard animCard) {
        view = theView;
        animateCard = animCard;
        isPlaying = false;
        moveStack = new Stack<Move>();
        sinkCards = new Card[104];
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlaying() {
        isPlaying = false;
    }

    public void startReplay(Stack<Move> history, CardAnchor[] anchor) {
        cardAnchor = anchor;
        moveStack.clear();
        while (!history.empty()) {
            Move move = history.peek();
            if (move.getToBegin() != move.getToEnd()) {
                for (int i = move.getToEnd(); i >= move.getToBegin(); i--) {
                    moveStack.push(new Move(move.getFrom(), i, 1, false, false));
                }
            } else {
                moveStack.push(move);
            }
            view.undo();
        }
        view.drawBoard();
        isPlaying = true;
        playNext();
    }

    public void playNext() {
        if (!isPlaying || moveStack.empty()) {
            isPlaying = false;
            view.stopAnimating();
            return;
        }
        Move move = moveStack.pop();

        if (move.getToBegin() == move.getToEnd()) {
            sinkCount = move.getCount();
            sinkAnchor = cardAnchor[move.getToBegin()];
            sinkUnhide = move.getUnhide();
            sinkFrom = cardAnchor[move.getFrom()];

            if (move.getInvert()) {
                for (int i = 0; i < sinkCount; i++) {
                    sinkCards[i] = sinkFrom.popCard();
                }
            } else {
                for (int i = sinkCount - 1; i >= 0; i--) {
                    sinkCards[i] = sinkFrom.popCard();
                }
            }
            animateCard.moveCards(sinkCards, sinkAnchor, sinkCount, this);
        } else {
            Log.e("Replay.java", "Invalid move encountered, aborting.");
            isPlaying = false;
        }
    }

    public void run() {
        if (isPlaying) {
            if (sinkUnhide) {
                sinkFrom.unhideTopCard();
            }
            playNext();
        }
    }
}
