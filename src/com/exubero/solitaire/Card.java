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

class Card {

    public static final int CLUBS = 0;
    public static final int DIAMONDS = 1;
    public static final int SPADES = 2;
    public static final int HEARTS = 3;

    public static final int ACE = 1;
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;

    public static int WIDTH = 45;
    public static int HEIGHT = 64;

    private int value;
    private int suit;
    private float locationX;
    private float locationY;

    public Card(int theValue, int theSuit) {
        value = theValue;
        suit = theSuit;
        locationX = 1;
        locationY = 1;
    }

    public static void setSize(int type) {
        if (type == Rules.SOLITAIRE) {
            WIDTH = 51;
            HEIGHT = 72;
        } else if (type == Rules.FREECELL) {
            WIDTH = 49;
            HEIGHT = 68;
        } else {
            WIDTH = 45;
            HEIGHT = 64;
        }
    }

    public float getX() {
        return locationX;
    }

    public float getY() {
        return locationY;
    }

    public int getValue() {
        return value;
    }

    public int getSuit() {
        return suit;
    }

    public void setPosition(float x, float y) {
        locationX = x;
        locationY = y;
    }

    public void movePosition(float dx, float dy) {
        locationX -= dx;
        locationY -= dy;
    }
}


