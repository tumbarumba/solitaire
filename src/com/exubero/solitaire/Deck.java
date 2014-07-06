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

import java.util.Random;


public class Deck {

    private Card[] cards;
    private int cardCount;

    public Deck(int decks) {
        init(decks, 4);
    }

    public Deck(int deckCount, int suitCount) {
        if (suitCount == 2) {
            deckCount *= 2;
        } else if (suitCount == 1) {
            deckCount *= 4;
        }
        init(deckCount, suitCount);
    }

    private void init(int deckCount, int suitCount) {
        cardCount = deckCount * 13 * suitCount;
        cards = new Card[cardCount];
        for (int deck = 0; deck < deckCount; deck++) {
            for (int suit = Card.CLUBS; suit < suitCount; suit++) {
                for (int value = 0; value < 13; value++) {
                    cards[deck * suitCount * 13 + suit * Card.KING + value] = new Card(value + 1, suit);
                }
            }
        }

        shuffle();
        shuffle();
        shuffle();
    }

    public Card popCard() {
        if (cardCount > 0) {
            return cards[--cardCount];
        }
        return null;
    }

    public boolean isEmpty() {
        return cardCount == 0;
    }

    public void shuffle() {
        int lastIdx = cardCount - 1;
        int swapIdx;
        Card swapCard;
        Random rand = new Random();

        while (lastIdx > 1) {
            swapIdx = rand.nextInt(lastIdx);
            swapCard = cards[swapIdx];
            cards[swapIdx] = cards[lastIdx];
            cards[lastIdx] = swapCard;
            lastIdx--;
        }
    }
}
