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


class CardAnchor {

    public static final int MAX_CARDS = 104;
    public static final int SEQ_SINK = 1;
    public static final int SUIT_SEQ_STACK = 2;
    public static final int DEAL_FROM = 3;
    public static final int DEAL_TO = 4;
    public static final int SPIDER_STACK = 5;
    public static final int FREECELL_STACK = 6;
    public static final int FREECELL_HOLD = 7;
    public static final int GENERIC_ANCHOR = 8;

    private int number;
    protected Rules rules;
    protected float locationX;
    protected float locationY;
    protected Card[] cards;
    protected int cardCount;
    protected int hiddenCount;
    protected float leftEdge;
    protected float rightEdge;
    protected float bottom;
    protected boolean isDone;

    //Variables for GenericAnchor
    protected int mSTARTSEQ;
    protected int mBUILDSEQ;
    protected int mMOVESEQ;
    protected int mBUILDSUIT;
    protected int mMOVESUIT;
    protected boolean mBUILDWRAP;
    protected boolean mMOVEWRAP;
    protected int mDROPOFF;
    protected int mPICKUP;
    protected int mDISPLAY;
    protected int mHACK;

    // ==========================================================================
    // Create a CardAnchor
    // -------------------
    public static CardAnchor createAnchor(int type, int number, Rules rules) {
        CardAnchor ret = null;
        switch (type) {
            case SEQ_SINK:
                ret = new SeqSink();
                break;
            case SUIT_SEQ_STACK:
                ret = new SuitSeqStack();
                break;
            case DEAL_FROM:
                ret = new DealFrom();
                break;
            case DEAL_TO:
                ret = new DealTo();
                break;
            case SPIDER_STACK:
                ret = new SpiderStack();
                break;
            case FREECELL_STACK:
                ret = new FreecellStack();
                break;
            case FREECELL_HOLD:
                ret = new FreecellHold();
                break;
            case GENERIC_ANCHOR:
                ret = new GenericAnchor();
                break;
        }
        ret.setRules(rules);
        ret.setNumber(number);
        return ret;
    }

    public CardAnchor() {
        locationX = 1;
        locationY = 1;
        cards = new Card[MAX_CARDS];
        cardCount = 0;
        hiddenCount = 0;
        leftEdge = -1;
        rightEdge = -1;
        bottom = -1;
        number = -1;
        isDone = false;
    }

    // ==========================================================================
    // Getters and Setters
    // -------------------
    public Card[] getCards() {
        return cards;
    }

    public int getCount() {
        return cardCount;
    }

    public int getHiddenCount() {
        return hiddenCount;
    }

    public float getLeftEdge() {
        return leftEdge;
    }

    public int getNumber() {
        return number;
    }

    public float getRightEdge() {
        return rightEdge;
    }

    public int getVisibleCount() {
        return cardCount - hiddenCount;
    }

    public int getMovableCount() {
        return cardCount > 0 ? 1 : 0;
    }

    public float getX() {
        return locationX;
    }

    public float getNewY() {
        return locationY;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setBottom(float edge) {
        bottom = edge;
    }

    public void setHiddenCount(int count) {
        hiddenCount = count;
    }

    public void setLeftEdge(float edge) {
        leftEdge = edge;
    }

    public void setMaxHeight(int maxHeight) {
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setRightEdge(float edge) {
        rightEdge = edge;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public void setShowing(int showing) {
    }

    protected void setCardPosition(int idx) {
        cards[idx].setPosition(locationX, locationY);
    }

    public void setDone(boolean done) {
        this.isDone = done;
    }

    //Methods for GenericAnchor
    public void setStartSeq(int seq) {
        mSTARTSEQ = seq;
    }

    public void setSeq(int seq) {
        mBUILDSEQ = seq;
        mMOVESEQ = seq;
    }

    public void setBuildSeq(int buildseq) {
        mBUILDSEQ = buildseq;
    }

    public void setMoveSeq(int moveseq) {
        mMOVESEQ = moveseq;
    }

    public void setWrap(boolean wrap) {
        mBUILDWRAP = wrap;
        mMOVEWRAP = wrap;
    }

    public void setMoveWrap(boolean movewrap) {
        mMOVEWRAP = movewrap;
    }

    public void setBuildWrap(boolean buildwrap) {
        mBUILDWRAP = buildwrap;
    }

    public void setSuit(int suit) {
        mBUILDSUIT = suit;
        mMOVESUIT = suit;
    }

    public void setBuildSuit(int buildsuit) {
        mBUILDSUIT = buildsuit;
    }

    public void setMoveSuit(int movesuit) {
        mMOVESUIT = movesuit;
    }

    public void setBehavior(int beh) {
        mDROPOFF = beh;
        mPICKUP = beh;
    }

    public void setDropoff(int dropoff) {
        mDROPOFF = dropoff;
    }

    public void setPickup(int pickup) {
        mPICKUP = pickup;
    }

    public void setDisplay(int display) {
        mDISPLAY = display;
    }

    public void setHack(int hack) {
        mHACK = hack;
    }
    //End Methods for Generic Anchor

    public void setPosition(float x, float y) {
        locationX = x;
        locationY = y;
        for (int i = 0; i < cardCount; i++) {
            setCardPosition(i);
        }
    }

    // ==========================================================================
    // Functions to add cards
    // ----------------------
    public void addCard(Card card) {
        cards[cardCount++] = card;
        setCardPosition(cardCount - 1);
    }

    public void addMoveCard(MoveCard moveCard) {
        int count = moveCard.getCount();
        Card[] cards = moveCard.dumpCards();

        for (int i = 0; i < count; i++) {
            addCard(cards[i]);
        }
    }

    public boolean dropSingleCard(Card card) {
        return false;
    }

    public boolean canDropCard(MoveCard moveCard, int close) {
        return false;
    }

    // ==========================================================================
    // Functions to take cards
    // -----------------------
    public Card[] getCardStack() {
        return null;
    }

    public Card grabCard(float x, float y) {
        Card ret = null;
        if (cardCount > 0 && isOverCard(x, y)) {
            ret = popCard();
        }
        return ret;
    }

    public Card popCard() {
        Card ret = cards[--cardCount];
        cards[cardCount] = null;
        return ret;
    }

    // ==========================================================================
    // Functions to interact with cards
    // --------------------------------
    public boolean tapCard(float x, float y) {
        return false;
    }

    public boolean unhideTopCard() {
        if (cardCount > 0 && hiddenCount > 0 && hiddenCount == cardCount) {
            hiddenCount--;
            return true;
        }
        return false;
    }

    public boolean expandStack(float x, float y) {
        return false;
    }

    public boolean canMoveStack(float x, float y) {
        return false;
    }


    // ==========================================================================
    // Functions to check locations
    // ----------------------------
    private boolean isOver(float x, float y, boolean deck, int close) {
        float clx = cardCount == 0 ? locationX : cards[cardCount - 1].getX();
        float leftX = leftEdge == -1 ? clx : leftEdge;
        float rightX = rightEdge == -1 ? clx + Card.WIDTH : rightEdge;
        float topY = (cardCount == 0 || deck) ? locationY : cards[cardCount - 1].getY();
        float botY = cardCount > 0 ? cards[cardCount - 1].getY() : locationY;
        botY += Card.HEIGHT;

        leftX -= close * Card.WIDTH / 2;
        rightX += close * Card.WIDTH / 2;
        topY -= close * Card.HEIGHT / 2;
        botY += close * Card.HEIGHT / 2;
        if (bottom != -1 && botY + 10 >= bottom)
            botY = bottom;

        if (x >= leftX && x <= rightX && y >= topY && y <= botY) {
            return true;
        }
        return false;
    }

    protected boolean isOverCard(float x, float y) {
        return isOver(x, y, false, 0);
    }

    protected boolean isOverCard(float x, float y, int close) {
        return isOver(x, y, false, close);
    }

    protected boolean isOverDeck(float x, float y) {
        return isOver(x, y, true, 0);
    }

    // ==========================================================================
    // Functions to draw
    // ----------------------------
    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (cardCount == 0) {
            drawMaster.drawEmptyAnchor(canvas, locationX, locationY, isDone);
        } else {
            drawMaster.drawCard(canvas, cards[cardCount - 1]);
        }
    }
}

// Straight up default
class DealTo extends CardAnchor {
    private int showingCount;

    public DealTo() {
        super();
        showingCount = 1;
    }

    @Override
    public void setShowing(int showing) {
        showingCount = showing;
    }

    @Override
    protected void setCardPosition(int idx) {
        if (showingCount == 1) {
            cards[idx].setPosition(locationX, locationY);
        } else {
            if (idx < cardCount - showingCount) {
                cards[idx].setPosition(locationX, locationY);
            } else {
                int offset = cardCount - showingCount;
                offset = offset < 0 ? 0 : offset;
                cards[idx].setPosition(locationX + (idx - offset) * Card.WIDTH / 2, locationY);
            }
        }
    }

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        setPosition(locationX, locationY);
    }

    @Override
    public boolean unhideTopCard() {
        setPosition(locationX, locationY);
        return false;
    }

    @Override
    public Card popCard() {
        Card ret = super.popCard();
        setPosition(locationX, locationY);
        return ret;
    }

    @Override
    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (cardCount == 0) {
            drawMaster.drawEmptyAnchor(canvas, locationX, locationY, isDone);
        } else {
            for (int i = cardCount - showingCount; i < cardCount; i++) {
                if (i >= 0) {
                    drawMaster.drawCard(canvas, cards[i]);
                }
            }
        }
    }
}

// Abstract stack anchor
class SeqStack extends CardAnchor {
    protected static final int SMALL_SPACING = 7;
    protected static final int HIDDEN_SPACING = 3;

    protected int spacing;
    protected boolean hideHidden;
    protected int maxHeight;

    public SeqStack() {
        super();
        spacing = getMaxSpacing();
        hideHidden = false;
        maxHeight = Card.HEIGHT;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        checkSizing();
        setPosition(locationX, locationY);
    }

    // This can't be a constant as Card.HEIGHT isn't constant.
    protected int getMaxSpacing() {
        return Card.HEIGHT / 3;
    }

    @Override
    protected void setCardPosition(int idx) {
        if (idx < hiddenCount) {
            if (hideHidden) {
                cards[idx].setPosition(locationX, locationY);
            } else {
                cards[idx].setPosition(locationX, locationY + HIDDEN_SPACING * idx);
            }
        } else {
            int startY = hideHidden ? HIDDEN_SPACING : hiddenCount * HIDDEN_SPACING;
            int y = (int) locationY + startY + (idx - hiddenCount) * spacing;
            cards[idx].setPosition(locationX, y);
        }
    }

    @Override
    public void setHiddenCount(int count) {
        super.setHiddenCount(count);
        checkSizing();
        setPosition(locationX, locationY);
    }

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        checkSizing();
    }

    @Override
    public Card popCard() {
        Card ret = super.popCard();
        checkSizing();
        return ret;
    }

    @Override
    public boolean expandStack(float x, float y) {
        if (isOverDeck(x, y)) {
            if (hiddenCount >= cardCount) {
                hiddenCount = cardCount == 0 ? 0 : cardCount - 1;
            } else if (cardCount - hiddenCount > 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMovableCount() {
        return getVisibleCount();
    }

    @Override
    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (cardCount == 0) {
            drawMaster.drawEmptyAnchor(canvas, locationX, locationY, isDone);
        } else {
            for (int i = 0; i < cardCount; i++) {
                if (i < hiddenCount) {
                    drawMaster.drawHiddenCard(canvas, cards[i]);
                } else {
                    drawMaster.drawCard(canvas, cards[i]);
                }
            }
        }
    }

    private void checkSizing() {
        if (cardCount < 2 || cardCount - hiddenCount < 2) {
            spacing = getMaxSpacing();
            hideHidden = false;
            return;
        }
        int max = maxHeight;
        int hidden = hiddenCount;
        int showing = cardCount - hidden;
        int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
        int spacing = spaceLeft / (showing - 1);

        if (spacing < SMALL_SPACING && hidden > 1) {
            hideHidden = true;
            spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
            spacing = spaceLeft / (showing - 1);
        } else {
            hideHidden = false;
            if (spacing > getMaxSpacing()) {
                spacing = getMaxSpacing();
            }
        }
        if (spacing != this.spacing) {
            this.spacing = spacing;
            setPosition(locationX, locationY);
        }
    }

    public float getNewY() {
        if (cardCount == 0) {
            return locationY;
        }
        return cards[cardCount - 1].getY() + spacing;
    }
}

// Anchor where cards to deal come from
class DealFrom extends CardAnchor {

    @Override
    public Card grabCard(float x, float y) {
        return null;
    }

    @Override
    public boolean tapCard(float x, float y) {
        if (isOverCard(x, y)) {
            rules.eventAlert(Rules.EVENT_DEAL, this);
            return true;
        }
        return false;
    }

    @Override
    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (cardCount == 0) {
            drawMaster.drawEmptyAnchor(canvas, locationX, locationY, isDone);
        } else {
            drawMaster.drawHiddenCard(canvas, cards[cardCount - 1]);
        }
    }
}

// Anchor that holds increasing same suited cards
class SeqSink extends CardAnchor {

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        rules.eventAlert(Rules.EVENT_STACK_ADD, this);
    }

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {
        Card card = moveCard.getTopCard();
        float x = card.getX() + Card.WIDTH / 2;
        float y = card.getY() + Card.HEIGHT / 2;
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        float my = cardCount > 0 ? topCard.getY() : locationY;

        if (isOverCard(x, y, close)) {
            if (moveCard.getCount() == 1) {
                if ((topCard == null && card.getValue() == 1) ||
                        (topCard != null && card.getSuit() == topCard.getSuit() &&
                                card.getValue() == topCard.getValue() + 1)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean dropSingleCard(Card card) {
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        if ((topCard == null && card.getValue() == 1) ||
                (topCard != null && card.getSuit() == topCard.getSuit() &&
                        card.getValue() == topCard.getValue() + 1)) {
            //addCard(card);
            return true;
        }
        return false;
    }
}

// Regular color alternating solitaire stack
class SuitSeqStack extends SeqStack {

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {

        Card card = moveCard.getTopCard();
        float x = card.getX() + Card.WIDTH / 2;
        float y = card.getY() + Card.HEIGHT / 2;
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        float my = cardCount > 0 ? topCard.getY() : locationY;

        if (isOverCard(x, y, close)) {
            if (topCard == null) {
                if (card.getValue() == Card.KING) {
                    return true;
                }
            } else if ((card.getSuit() & 1) != (topCard.getSuit() & 1) &&
                    card.getValue() == topCard.getValue() - 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Card[] getCardStack() {
        int visibleCount = getVisibleCount();
        Card[] ret = new Card[visibleCount];

        for (int i = visibleCount - 1; i >= 0; i--) {
            ret[i] = popCard();
        }
        return ret;
    }

    @Override
    public boolean canMoveStack(float x, float y) {
        return super.expandStack(x, y);
    }
}

// Spider solitaire style stack
class SpiderStack extends SeqStack {

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        rules.eventAlert(Rules.EVENT_STACK_ADD, this);
    }

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {

        Card card = moveCard.getTopCard();
        float x = card.getX() + Card.WIDTH / 2;
        float y = card.getY() + Card.HEIGHT / 2;
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        float my = cardCount > 0 ? topCard.getY() : locationY;

        if (isOverCard(x, y, close)) {
            if (topCard == null || card.getValue() == topCard.getValue() - 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getMovableCount() {
        if (cardCount < 2)
            return cardCount;

        int retCount = 1;
        int suit = cards[cardCount - 1].getSuit();
        int val = cards[cardCount - 1].getValue();

        for (int i = cardCount - 2; i >= hiddenCount; i--, retCount++, val++) {
            if (cards[i].getSuit() != suit || cards[i].getValue() != val + 1) {
                break;
            }
        }

        return retCount;
    }

    @Override
    public Card[] getCardStack() {
        int retCount = getMovableCount();

        Card[] ret = new Card[retCount];

        for (int i = retCount - 1; i >= 0; i--) {
            ret[i] = popCard();
        }

        return ret;
    }

    @Override
    public boolean expandStack(float x, float y) {
        if (super.expandStack(x, y)) {
            Card bottom = cards[cardCount - 1];
            Card second = cards[cardCount - 2];
            if (bottom.getSuit() == second.getSuit() &&
                    bottom.getValue() == second.getValue() - 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canMoveStack(float x, float y) {
        if (super.expandStack(x, y)) {
            float maxY = cards[cardCount - getMovableCount()].getY();

            if (y >= maxY - Card.HEIGHT / 2) {
                return true;
            }
        }
        return false;
    }

}

// Freecell stack
class FreecellStack extends SeqStack {

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {

        Card card = moveCard.getTopCard();
        float x = card.getX() + Card.WIDTH / 2;
        float y = card.getY() + Card.HEIGHT / 2;
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;

        if (isOverCard(x, y, close)) {
            if (topCard == null) {
                if (rules.countFreeSpaces() >= moveCard.getCount()) {
                    return true;
                }
            } else if ((card.getSuit() & 1) != (topCard.getSuit() & 1) &&
                    card.getValue() == topCard.getValue() - 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getMovableCount() {
        if (cardCount < 2)
            return cardCount;

        int retCount = 1;
        int maxMoveCount = rules.countFreeSpaces() + 1;

        for (int i = cardCount - 2; i >= 0 && retCount < maxMoveCount; i--, retCount++) {
            if ((cards[i].getSuit() & 1) == (cards[i + 1].getSuit() & 1) ||
                    cards[i].getValue() != cards[i + 1].getValue() + 1) {
                break;
            }
        }

        return retCount;
    }

    @Override
    public Card[] getCardStack() {
        int retCount = getMovableCount();
        Card[] ret = new Card[retCount];

        for (int i = retCount - 1; i >= 0; i--) {
            ret[i] = popCard();
        }
        return ret;
    }

    @Override
    public boolean expandStack(float x, float y) {
        if (super.expandStack(x, y)) {
            if (rules.countFreeSpaces() > 0) {
                Card bottom = cards[cardCount - 1];
                Card second = cards[cardCount - 2];
                if ((bottom.getSuit() & 1) != (second.getSuit() & 1) &&
                        bottom.getValue() == second.getValue() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canMoveStack(float x, float y) {
        if (super.expandStack(x, y)) {
            float maxY = cards[cardCount - getMovableCount()].getY();
            if (y >= maxY - Card.HEIGHT / 2) {
                return true;
            }
        }
        return false;
    }
}

// Freecell holding pen
class FreecellHold extends CardAnchor {

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {
        Card card = moveCard.getTopCard();
        if (cardCount == 0 && moveCard.getCount() == 1 &&
                isOverCard(card.getX() + Card.WIDTH / 2, card.getY() + Card.HEIGHT / 2, close)) {
            return true;
        }
        return false;
    }

}

// New Abstract
class GenericAnchor extends CardAnchor {

    //Sequence start values
    public static final int START_ANY = 1; // An empty stack can take any card.
    public static final int START_KING = 2; // An empty stack can take only a king.

    //Value Sequences
    public static final int SEQ_ANY = 1; //You can build as you like
    public static final int SEQ_SEQ = 2;  //Building only allows sequential
    public static final int SEQ_ASC = 3;  //Ascending only
    public static final int SEQ_DSC = 4;  //Descending only

    //Suit Sequences that limits how adding cards to the stack works
    public static final int SUIT_ANY = 1;  //Build doesn't care about suite
    public static final int SUIT_RB = 2;  //Must alternate Red & Black
    public static final int SUIT_OTHER = 3;//As long as different
    public static final int SUIT_COLOR = 4;//As long as same color
    public static final int SUIT_SAME = 5; //As long as same suit

    //Pickup & Dropoff Behavior
    public static final int PACK_NONE = 1;  // Interaction in this mode not allowed
    public static final int PACK_ONE = 2;  //Can only accept 1 card
    public static final int PACK_MULTI = 3;  //Can accept multiple cards
    public static final int PACK_FIXED = 4;  //Don't think this will ever be used
    public static final int PACK_LIMIT_BY_FREE = 5; //For freecell style movement

    //Anchor Display (Hidden vs. Shown faces)
    public static final int DISPLAY_ALL = 1;  //All cards are shown
    public static final int DISPLAY_HIDE = 2; //All cards are hidden
    public static final int DISPLAY_MIX = 3;  //Uses a mixture
    public static final int DISPLAY_ONE = 4;  //Displays one only

    //Hack to fix Spider Dealing
    public static final int DEALHACK = 1;

    protected static final int SMALL_SPACING = 7;
    protected static final int HIDDEN_SPACING = 3;

    protected int spacing;
    protected boolean hideHidden;
    protected int maxHeight;

    public GenericAnchor() {
        super();
        setStartSeq(GenericAnchor.SEQ_ANY);
        setBuildSeq(GenericAnchor.SEQ_ANY);
        setBuildWrap(false);
        setBuildSuit(GenericAnchor.SUIT_ANY);
        setDropoff(GenericAnchor.PACK_NONE);
        setPickup(GenericAnchor.PACK_NONE);
        setDisplay(GenericAnchor.DISPLAY_ALL);
        spacing = getMaxSpacing();
        hideHidden = false;
        maxHeight = Card.HEIGHT;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        checkSizing();
        setPosition(locationX, locationY);
    }

    @Override
    protected void setCardPosition(int idx) {
        if (idx < hiddenCount) {
            if (hideHidden) {
                cards[idx].setPosition(locationX, locationY);
            } else {
                cards[idx].setPosition(locationX, locationY + HIDDEN_SPACING * idx);
            }
        } else {
            int startY = hideHidden ? HIDDEN_SPACING : hiddenCount * HIDDEN_SPACING;
            int y = (int) locationY + startY + (idx - hiddenCount) * spacing;
            cards[idx].setPosition(locationX, y);
        }
    }

    @Override
    public void setHiddenCount(int count) {
        super.setHiddenCount(count);
        checkSizing();
        setPosition(locationX, locationY);
    }

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        checkSizing();
        if (mHACK == GenericAnchor.DEALHACK) {
            rules.eventAlert(Rules.EVENT_STACK_ADD, this);
        }
    }

    @Override
    public Card popCard() {
        Card ret = super.popCard();
        checkSizing();
        return ret;
    }

    @Override
    public boolean canDropCard(MoveCard moveCard, int close) {
        if (mDROPOFF == GenericAnchor.PACK_NONE) {
            return false;
        }

        Card card = moveCard.getTopCard();
        float x = card.getX() + Card.WIDTH / 2;
        float y = card.getY() + Card.HEIGHT / 2;
        //Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        //float my = cardCount > 0 ? topCard.getY() : locationY;
        if (isOverCard(x, y, close)) {
            return canBuildCard(card);
        }
        return false;
    }

    public boolean canBuildCard(Card card) {
        // SEQ_ANY will allow all
        if (mBUILDSEQ == GenericAnchor.SEQ_ANY) {
            return true;
        }
        Card topCard = cardCount > 0 ? cards[cardCount - 1] : null;
        // Rules for empty stacks
        if (topCard == null) {
            switch (mSTARTSEQ) {
                case GenericAnchor.START_KING:
                    return card.getValue() == Card.KING;
                case GenericAnchor.START_ANY:
                default:
                    return true;
            }
        }
        int value = card.getValue();
        int suit = card.getSuit();
        int tvalue = topCard.getValue();
        int tsuit = topCard.getSuit();
        // Fail if sequence is wrong
        switch (mBUILDSEQ) {
            //WRAP_NOWRAP=1; //Building stacks do not wrap
            //WRAP_WRAP=2;   //Building stacks wraps around
            case GenericAnchor.SEQ_ASC:
                if (value - tvalue != 1) {
                    return false;
                }
                break;
            case GenericAnchor.SEQ_DSC:
                if (tvalue - value != 1) {
                    return false;
                }
                break;
            case GenericAnchor.SEQ_SEQ:
                if (Math.abs(tvalue - value) != 1) {
                    return false;
                }
                break;
        }
        // Fail if suit is wrong
        switch (mBUILDSUIT) {
            case GenericAnchor.SUIT_RB:
                if (Math.abs(tsuit - suit) % 2 == 0) {
                    return false;
                }
                break;
            case GenericAnchor.SUIT_OTHER:
                if (tsuit == suit) {
                    return false;
                }
                break;
            case GenericAnchor.SUIT_COLOR:
                if (Math.abs(tsuit - suit) != 2) {
                    return false;
                }
                break;
            case GenericAnchor.SUIT_SAME:
                if (tsuit != suit) {
                    return false;
                }
                break;
        }
        // Passes all rules
        return true;
    }

    @Override
    public void draw(DrawMaster drawMaster, Canvas canvas) {
        if (cardCount == 0) {
            drawMaster.drawEmptyAnchor(canvas, locationX, locationY, isDone);
            return;
        }
        switch (mDISPLAY) {
            case GenericAnchor.DISPLAY_ALL:
                for (int i = 0; i < cardCount; i++) {
                    drawMaster.drawCard(canvas, cards[i]);
                }
                break;
            case GenericAnchor.DISPLAY_HIDE:
                for (int i = 0; i < cardCount; i++) {
                    drawMaster.drawHiddenCard(canvas, cards[i]);
                }
                break;
            case GenericAnchor.DISPLAY_MIX:
                for (int i = 0; i < cardCount; i++) {
                    if (i < hiddenCount) {
                        drawMaster.drawHiddenCard(canvas, cards[i]);
                    } else {
                        drawMaster.drawCard(canvas, cards[i]);
                    }
                }
                break;
            case GenericAnchor.DISPLAY_ONE:
                for (int i = 0; i < cardCount; i++) {
                    if (i < cardCount - 1) {
                        drawMaster.drawHiddenCard(canvas, cards[i]);
                    } else {
                        drawMaster.drawCard(canvas, cards[i]);
                    }
                }
                break;
        }
    }

    @Override
    public boolean expandStack(float x, float y) {
        if (isOverDeck(x, y)) {
            return (getMovableCount() > 0);
      /*
      if (hiddenCount >= cardCount) {
        hiddenCount = cardCount == 0 ? 0 : cardCount - 1;
      } else if (cardCount - hiddenCount > 1) {
        return true;
      }
      */
        }
        return false;
    }

    @Override
    public boolean canMoveStack(float x, float y) {
        return expandStack(x, y);
    }

    @Override
    public Card[] getCardStack() {
        int movableCount = getMovableCount();
        Card[] ret = new Card[movableCount];
        for (int i = movableCount - 1; i >= 0; i--) {
            ret[i] = popCard();
        }
        return ret;
    }

    @Override
    public int getMovableCount() {
        int visibleCount = getVisibleCount();
        if (visibleCount == 0 || mPICKUP == GenericAnchor.PACK_NONE) {
            return 0;
        }
        int seq_allowed = 1;
        if (visibleCount > 1) {
            int i = cardCount - 1;
            boolean g;
            boolean h;
            do {
                g = true;
                h = true;
                switch (mMOVESEQ) {
                    case GenericAnchor.SEQ_ANY:
                        h = true;
                        break;
                    case GenericAnchor.SEQ_ASC:
                        h = this.isSequenceAscending(i - 1, i, mMOVEWRAP);
                        break;
                    case GenericAnchor.SEQ_DSC:
                        h = this.isSequenceAscending(i, i - 1, mMOVEWRAP);
                        break;
                    case GenericAnchor.SEQ_SEQ:
                        h = (this.isSequenceAscending(i, i - 1, mMOVEWRAP) ||
                                this.isSequenceAscending(i - 1, i, mMOVEWRAP));
                        break;
                }
                if (h == false) {
                    g = false;
                }
                switch (mMOVESUIT) {
                    case GenericAnchor.SUIT_ANY:
                        h = true;
                        break;
                    case GenericAnchor.SUIT_COLOR:
                        h = !this.isSuitsOppositeColours(i - 1, i);
                        break;
                    case GenericAnchor.SUIT_OTHER:
                        h = this.isSuitsNotEqual(i - 1, i);
                        break;
                    case GenericAnchor.SUIT_RB:
                        h = this.isSuitsOppositeColours(i - 1, i);
                        break;
                    case GenericAnchor.SUIT_SAME:
                        h = this.isSuitsEqual(i - 1, i);
                        break;
                }
                if (h == false) {
                    g = false;
                }
                if (g) {
                    seq_allowed++;
                }
                i--;
            } while (g && (cardCount - i) < visibleCount);
        }

        switch (mPICKUP) {
            case GenericAnchor.PACK_NONE:
                return 0;
            case GenericAnchor.PACK_ONE:
                seq_allowed = Math.min(1, seq_allowed);
                break;
            case GenericAnchor.PACK_MULTI:
                break;
            case GenericAnchor.PACK_FIXED:
                //seq_allowed = Math.min( xmin, seq_allowed);
                break;
            case GenericAnchor.PACK_LIMIT_BY_FREE:
                seq_allowed = Math.min(rules.countFreeSpaces() + 1, seq_allowed);
                break;
        }
        return seq_allowed;
    }

    public boolean isSequenceAscending(int p1, int p2, boolean wrap) {
        Card c1 = cards[p1];
        Card c2 = cards[p2];
        int v1 = c1.getValue();
        int v2 = c2.getValue();

        if (v2 + 1 == v1) {
            return true;
        }
        if (wrap) {
            if (v2 == Card.KING && v1 == Card.ACE) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuitsOppositeColours(int p1, int p2) {
        Card c1 = cards[p1];
        Card c2 = cards[p2];
        int s1 = c1.getSuit();
        int s2 = c2.getSuit();
        if ((s1 == Card.CLUBS || s1 == Card.SPADES) &&
                (s2 == Card.HEARTS || s2 == Card.DIAMONDS)) {
            return true;
        }
        if ((s1 == Card.HEARTS || s1 == Card.DIAMONDS) &&
                (s2 == Card.CLUBS || s2 == Card.SPADES)) {
            return true;
        }
        return false;
    }

    public boolean isSuitsEqual(int p1, int p2) {
        return (cards[p1].getSuit() == cards[p2].getSuit());
    }

    public boolean isSuitsNotEqual(int p1, int p2) {
        return (cards[p1].getSuit() != cards[p2].getSuit());
    }

    private void checkSizing() {
        if (cardCount < 2 || cardCount - hiddenCount < 2) {
            spacing = getMaxSpacing();
            hideHidden = false;
            return;
        }
        int max = maxHeight;
        int hidden = hiddenCount;
        int showing = cardCount - hidden;
        int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
        int spacing = spaceLeft / (showing - 1);

        if (spacing < SMALL_SPACING && hidden > 1) {
            hideHidden = true;
            spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
            spacing = spaceLeft / (showing - 1);
        } else {
            hideHidden = false;
            if (spacing > getMaxSpacing()) {
                spacing = getMaxSpacing();
            }
        }
        if (spacing != this.spacing) {
            this.spacing = spacing;
            setPosition(locationX, locationY);
        }
    }

    // This can't be a constant as Card.HEIGHT isn't constant.
    protected int getMaxSpacing() {
        return Card.HEIGHT / 3;
    }

    public float getNewY() {
        if (cardCount == 0) {
            return locationY;
        }
        return cards[cardCount - 1].getY() + spacing;
    }
}
