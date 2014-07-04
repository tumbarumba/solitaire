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

import android.os.Bundle;

import java.util.Stack;


public abstract class Rules {

    public static final int SOLITAIRE = 1;
    public static final int SPIDER = 2;
    public static final int FREECELL = 3;
    public static final int FORTYTHIEVES = 4;

    public static final int EVENT_INVALID = -1;
    public static final int EVENT_DEAL = 1;
    public static final int EVENT_STACK_ADD = 2;
    public static final int EVENT_FLING = 3;
    public static final int EVENT_SMART_MOVE = 4;
    public static final int EVENT_DEAL_NEXT = 5;

    public static final int AUTO_MOVE_ALWAYS = 2;
    public static final int AUTO_MOVE_FLING_ONLY = 1;
    public static final int AUTO_MOVE_NEVER = 0;

    protected SolitaireView view;
    protected Stack<Move> moveHistory;
    protected AnimateCard animateCard;
    protected boolean ignoreEvents;
    protected EventPoster eventPoster;

    // Anchors
    protected CardAnchor[] cardAnchors;
    protected int cardAnchorCount;
    protected Deck deck;
    protected int cardCount;

    // Automove
    protected int autoMoveLevel;
    protected boolean wasFling;
    private int type;

    public static Rules createRules(int type, Bundle map, SolitaireView view,
                                    Stack<Move> moveHistory, AnimateCard animate) {
        Rules ret = null;
        switch (type) {
            case SOLITAIRE:
                ret = new NormalSolitaire();
                break;
            case SPIDER:
                ret = new Spider();
                break;
            case FREECELL:
                ret = new Freecell();
                break;
            case FORTYTHIEVES:
                ret = new FortyThieves();
                break;
        }

        if (ret != null) {
            ret.setType(type);
            ret.setView(view);
            ret.setMoveHistory(moveHistory);
            ret.setAnimateCard(animate);
            ret.setEventPoster(new EventPoster(ret));
            ret.refreshOptions();
            ret.init(map);
        }
        return ret;
    }

    public int getType() {
        return type;
    }

    public int getCardCount() {
        return cardCount;
    }

    public CardAnchor[] getCardAnchors() {
        return cardAnchors;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setView(SolitaireView view) {
        this.view = view;
    }

    public void setMoveHistory(Stack<Move> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public void setAnimateCard(AnimateCard animateCard) {
        this.animateCard = animateCard;
    }

    public void setIgnoreEvents(boolean ignore) {
        ignoreEvents = ignore;
    }

    public void setEventPoster(EventPoster ep) {
        eventPoster = ep;
    }

    public boolean getIgnoreEvents() {
        return ignoreEvents;
    }

    public int getRulesExtra() {
        return 0;
    }

    public String getGameTypeString() {
        return "";
    }

    public String getPrettyGameTypeString() {
        return "";
    }

    public boolean hasScore() {
        return false;
    }

    public boolean hasString() {
        return false;
    }

    public String getString() {
        return "";
    }

    public void setCarryOverScore(int score) {
    }

    public int getScore() {
        return 0;
    }

    public void addDealCount() {
    }

    public int countFreeSpaces() {
        return 0;
    }

    protected void signalWin() {
        view.displayWin();
    }

    abstract public void init(Bundle map);

    public void eventAlert(int event) {
        if (!ignoreEvents) {
            eventPoster.postEvent(event);
            view.refresh();
        }
    }

    public void eventAlert(int event, CardAnchor anchor) {
        if (!ignoreEvents) {
            eventPoster.postEvent(event, anchor);
            view.refresh();
        }
    }

    public void eventAlert(int event, CardAnchor anchor, Card card) {
        if (!ignoreEvents) {
            eventPoster.postEvent(event, anchor, card);
            view.refresh();
        }
    }

    public void clearEvent() {
        eventPoster.clearEvent();
    }

    abstract public void eventProcess(int event, CardAnchor anchor);

    abstract public void eventProcess(int event, CardAnchor anchor, Card card);

    abstract public void eventProcess(int event);

    abstract public void resize(int width, int height);

    public boolean fling(MoveCard moveCard) {
        moveCard.release();
        return false;
    }

    public void handleEvents() {
        while (!ignoreEvents && eventPoster.hasEvent()) {
            eventPoster.handleEvent();
        }
    }

    public void refreshOptions() {
        autoMoveLevel = view.getSettings().getInt("AutoMoveLevel", Rules.AUTO_MOVE_ALWAYS);
        wasFling = false;
    }
}

class NormalSolitaire extends Rules {

    private boolean isDealThree;
    private int dealsRemainingCount;
    private String scoreString;
    private int lastScore;
    private int carryOverScore;

    @Override
    public void init(Bundle map) {
        ignoreEvents = true;
        isDealThree = view.getSettings().getBoolean("SolitaireDealThree", true);

        // Thirteen total anchors for regular solitaire
        cardCount = 52;
        cardAnchorCount = 13;
        cardAnchors = new CardAnchor[cardAnchorCount];

        // Top dealt from anchors
        cardAnchors[0] = CardAnchor.createAnchor(CardAnchor.DEAL_FROM, 0, this);
        cardAnchors[1] = CardAnchor.createAnchor(CardAnchor.DEAL_TO, 1, this);
        if (isDealThree) {
            cardAnchors[1].setShowing(3);
        } else {
            cardAnchors[1].setShowing(1);
        }

        // Top anchors for placing cards
        for (int i = 0; i < 4; i++) {
            cardAnchors[i + 2] = CardAnchor.createAnchor(CardAnchor.SEQ_SINK, i + 2, this);
        }

        // Middle anchor stacks
        for (int i = 0; i < 7; i++) {
            cardAnchors[i + 6] = CardAnchor.createAnchor(CardAnchor.GENERIC_ANCHOR, i + 6, this);
            cardAnchors[i + 6].setStartSeq(GenericAnchor.START_KING);
            cardAnchors[i + 6].setBuildSeq(GenericAnchor.SEQ_DSC);
            cardAnchors[i + 6].setMoveSeq(GenericAnchor.SEQ_ASC);
            cardAnchors[i + 6].setSuit(GenericAnchor.SUIT_RB);
            cardAnchors[i + 6].setWrap(false);
            cardAnchors[i + 6].setBehavior(GenericAnchor.PACK_MULTI);
            cardAnchors[i + 6].setDisplay(GenericAnchor.DISPLAY_MIX);
        }

        if (map != null) {
            // Do some assertions, default to a new game if we find an invalid state
            if (map.getInt("cardAnchorCount") == 13 &&
                    map.getInt("cardCount") == 52) {
                int[] cardCount = map.getIntArray("anchorCardCount");
                int[] hiddenCount = map.getIntArray("anchorHiddenCount");
                int[] value = map.getIntArray("value");
                int[] suit = map.getIntArray("suit");
                int cardIdx = 0;
                dealsRemainingCount = map.getInt("rulesExtra");

                for (int i = 0; i < 13; i++) {
                    for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
                        Card card = new Card(value[cardIdx], suit[cardIdx]);
                        cardAnchors[i].addCard(card);
                    }
                    cardAnchors[i].setHiddenCount(hiddenCount[i]);
                }
                if (dealsRemainingCount != -1) {
                    // reset to zero as getScore() uses it in its calculation.
                    carryOverScore = 0;
                    carryOverScore = map.getInt("score") - getScore();
                }

                ignoreEvents = false;
                // Return here so an invalid save state will result in a new game
                return;
            }
        }

        deck = new Deck(1);
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j <= i; j++) {
                cardAnchors[i + 6].addCard(deck.popCard());
            }
            cardAnchors[i + 6].setHiddenCount(i);
        }

        while (!deck.isEmpty()) {
            cardAnchors[0].addCard(deck.popCard());
        }

        if (view.getSettings().getBoolean("SolitaireStyleNormal", true)) {
            dealsRemainingCount = -1;
        } else {
            dealsRemainingCount = isDealThree ? 2 : 0;
            lastScore = -52;
            scoreString = "-$52";
            carryOverScore = 0;
        }
        ignoreEvents = false;
    }

    @Override
    public void setCarryOverScore(int score) {
        carryOverScore = score;
    }

    @Override
    public void resize(int width, int height) {
        int rem = width - Card.WIDTH * 7;
        int maxHeight = height - (20 + Card.HEIGHT);
        rem /= 8;
        for (int i = 0; i < 7; i++) {
            cardAnchors[i + 6].setPosition(rem + i * (rem + Card.WIDTH), 20 + Card.HEIGHT);
            cardAnchors[i + 6].setMaxHeight(maxHeight);
        }

        for (int i = 3; i >= 0; i--) {
            cardAnchors[i + 2].setPosition(rem + (6 - i) * (rem + Card.WIDTH), 10);
        }

        for (int i = 0; i < 2; i++) {
            cardAnchors[i].setPosition(rem + i * (rem + Card.WIDTH), 10);
        }

        // Setup edge cards (Touch sensor loses sensitivity towards the edge).
        cardAnchors[0].setLeftEdge(0);
        cardAnchors[2].setRightEdge(width);
        cardAnchors[6].setLeftEdge(0);
        cardAnchors[12].setRightEdge(width);
        for (int i = 0; i < 7; i++) {
            cardAnchors[i + 6].setBottom(height);
        }
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor) {
        if (ignoreEvents) {
            return;
        }
        if (event == EVENT_DEAL) {
            if (cardAnchors[0].getCount() == 0) {
                boolean addDealCount = false;
                if (dealsRemainingCount == 0) {
                    cardAnchors[0].setDone(true);
                    return;
                } else if (dealsRemainingCount > 0) {
                    dealsRemainingCount--;
                    addDealCount = true;
                }
                int count = 0;
                while (cardAnchors[1].getCount() > 0) {
                    cardAnchors[0].addCard(cardAnchors[1].popCard());
                    count++;
                }
                moveHistory.push(new Move(1, 0, count, true, false, addDealCount));
                view.refresh();
            } else {
                int count = 0;
                int maxCount = isDealThree ? 3 : 1;
                for (int i = 0; i < maxCount && cardAnchors[0].getCount() > 0; i++) {
                    cardAnchors[1].addCard(cardAnchors[0].popCard());
                    count++;
                }
                if (dealsRemainingCount == 0 && cardAnchors[0].getCount() == 0) {
                    cardAnchors[0].setDone(true);
                }
                moveHistory.push(new Move(0, 1, count, true, false));
            }
        } else if (event == EVENT_STACK_ADD) {
            if (cardAnchors[2].getCount() == 13 && cardAnchors[3].getCount() == 13 &&
                    cardAnchors[4].getCount() == 13 && cardAnchors[5].getCount() == 13) {
                signalWin();
            } else {
                if (autoMoveLevel == AUTO_MOVE_ALWAYS ||
                        (autoMoveLevel == AUTO_MOVE_FLING_ONLY && wasFling)) {
                    eventAlert(EVENT_SMART_MOVE);
                } else {
                    view.stopAnimating();
                    wasFling = false;
                }
            }
        }
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor, Card card) {
        if (ignoreEvents) {
            anchor.addCard(card);
            return;
        }
        if (event == EVENT_FLING) {
            wasFling = true;
            if (!TryToSinkCard(anchor, card)) {
                anchor.addCard(card);
                wasFling = false;
            }
        } else {
            anchor.addCard(card);
        }
    }

    @Override
    public void eventProcess(int event) {
        if (ignoreEvents) {
            return;
        }
        if (event == EVENT_SMART_MOVE) {
            int i;
            for (i = 0; i < 7; i++) {
                if (cardAnchors[i + 6].getCount() > 0 &&
                        TryToSink(cardAnchors[i + 6])) {
                    break;
                }
            }
            if (i == 7) {
                wasFling = false;
                view.stopAnimating();
            }
        }
    }

    @Override
    public boolean fling(MoveCard moveCard) {
        if (moveCard.getCount() == 1) {
            CardAnchor anchor = moveCard.getAnchor();
            Card card = moveCard.dumpCards(false)[0];
            for (int i = 0; i < 4; i++) {
                if (cardAnchors[i + 2].dropSingleCard(card)) {
                    eventAlert(EVENT_FLING, anchor, card);
                    return true;
                }
            }
            anchor.addCard(card);
        } else {
            moveCard.release();
        }
        return false;
    }

    private boolean TryToSink(CardAnchor anchor) {
        Card card = anchor.popCard();
        boolean ret = TryToSinkCard(anchor, card);
        if (!ret) {
            anchor.addCard(card);
        }
        return ret;
    }

    private boolean TryToSinkCard(CardAnchor anchor, Card card) {
        for (int i = 0; i < 4; i++) {
            if (cardAnchors[i + 2].dropSingleCard(card)) {
                moveHistory.push(new Move(anchor.getNumber(), i + 2, 1, false, anchor.unhideTopCard()));
                animateCard.moveCard(card, cardAnchors[i + 2]);
                return true;
            }
        }

        return false;
    }

    @Override
    public int getRulesExtra() {
        return dealsRemainingCount;
    }

    @Override
    public String getGameTypeString() {
        if (dealsRemainingCount == -1) {
            if (isDealThree) {
                return "SolitaireNormalDeal3";
            } else {
                return "SolitaireNormalDeal1";
            }
        } else {
            if (isDealThree) {
                return "SolitaireVegasDeal3";
            } else {
                return "SolitaireVegasDeal1";
            }
        }
    }

    @Override
    public String getPrettyGameTypeString() {
        if (dealsRemainingCount == -1) {
            if (isDealThree) {
                return "Solitaire Dealing Three Cards";
            } else {
                return "Solitaire Dealing One Card";
            }
        } else {
            if (isDealThree) {
                return "Vegas Solitaire Dealing Three Cards";
            } else {
                return "Vegas Solitaire Dealing One Card";
            }
        }
    }

    @Override
    public boolean hasScore() {
        if (dealsRemainingCount != -1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasString() {
        return hasScore();
    }

    @Override
    public String getString() {
        if (dealsRemainingCount != -1) {
            int score = carryOverScore - 52;
            for (int i = 0; i < 4; i++) {
                score += 5 * cardAnchors[i + 2].getCount();
            }
            if (score != lastScore) {
                if (score < 0) {
                    scoreString = "-$" + (score * -1);
                } else {
                    scoreString = "$" + score;
                }
            }
            return scoreString;
        }
        return "";
    }

    @Override
    public int getScore() {
        if (dealsRemainingCount != -1) {
            int score = carryOverScore - 52;
            for (int i = 0; i < 4; i++) {
                score += 5 * cardAnchors[i + 2].getCount();
            }
            return score;
        }
        return 0;
    }

    @Override
    public void addDealCount() {
        if (dealsRemainingCount != -1) {
            dealsRemainingCount++;
            cardAnchors[0].setDone(false);
        }
    }
}

class Spider extends Rules {
    private boolean isStillDealing;

    public void init(Bundle map) {
        ignoreEvents = true;
        isStillDealing = false;

        cardCount = 104;
        cardAnchorCount = 12;
        cardAnchors = new CardAnchor[cardAnchorCount];

        // Anchor stacks
        for (int i = 0; i < 10; i++) {
            cardAnchors[i] = CardAnchor.createAnchor(CardAnchor.SPIDER_STACK, i, this);
            cardAnchors[i] = CardAnchor.createAnchor(CardAnchor.GENERIC_ANCHOR, i, this);
            cardAnchors[i].setBuildSeq(GenericAnchor.SEQ_DSC);
            cardAnchors[i].setBuildSuit(GenericAnchor.SEQ_ANY);
            cardAnchors[i].setMoveSeq(GenericAnchor.SEQ_ASC);
            cardAnchors[i].setMoveSuit(GenericAnchor.SUIT_SAME);
            cardAnchors[i].setBehavior(GenericAnchor.PACK_MULTI);
            cardAnchors[i].setDisplay(GenericAnchor.DISPLAY_MIX);
            cardAnchors[i].setHack(GenericAnchor.DEALHACK);
        }

        cardAnchors[10] = CardAnchor.createAnchor(CardAnchor.DEAL_FROM, 10, this);
        cardAnchors[11] = CardAnchor.createAnchor(CardAnchor.DEAL_TO, 11, this);

        if (map != null) {
            // Do some assertions, default to a new game if we find an invalid state
            if (map.getInt("cardAnchorCount") == 12 &&
                    map.getInt("cardCount") == 104) {
                int[] cardCount = map.getIntArray("anchorCardCount");
                int[] hiddenCount = map.getIntArray("anchorHiddenCount");
                int[] value = map.getIntArray("value");
                int[] suit = map.getIntArray("suit");
                int cardIdx = 0;

                for (int i = 0; i < cardAnchorCount; i++) {
                    for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
                        Card card = new Card(value[cardIdx], suit[cardIdx]);
                        cardAnchors[i].addCard(card);
                    }
                    cardAnchors[i].setHiddenCount(hiddenCount[i]);
                }

                ignoreEvents = false;
                // Return here so an invalid save state will result in a new game
                return;
            }
        }

        int suits = view.getSettings().getInt("SpiderSuits", 4);
        deck = new Deck(2, suits);
        int i = 54;
        while (i > 0) {
            for (int j = 0; j < 10 && i > 0; j++) {
                i--;
                cardAnchors[j].addCard(deck.popCard());
                cardAnchors[j].setHiddenCount(cardAnchors[j].getCount() - 1);
            }
        }

        while (!deck.isEmpty()) {
            cardAnchors[10].addCard(deck.popCard());
        }
        ignoreEvents = false;
    }

    public void resize(int width, int height) {
        int rem = (width - (Card.WIDTH * 10)) / 10;
        for (int i = 0; i < 10; i++) {
            cardAnchors[i].setPosition(rem / 2 + i * (rem + Card.WIDTH), 10);
            cardAnchors[i].setMaxHeight(height - 10);
        }
        // Setup edge cards (Touch sensor loses sensitivity towards the edge).
        cardAnchors[0].setLeftEdge(0);
        cardAnchors[9].setRightEdge(width);

        for (int i = 0; i < 10; i++) {
            cardAnchors[i].setBottom(height);
        }
        // These two are offscreen as the user doesn't need to see them, but they
        // are needed to hold onto out of play cards.
        cardAnchors[10].setPosition(-50, 1);
        cardAnchors[11].setPosition(-50, 1);
    }

    @Override
    public void eventProcess(int event) {
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor, Card card) {
        anchor.addCard(card);
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor) {
        if (ignoreEvents) {
            return;
        }
        if (event == EVENT_STACK_ADD) {
            if (anchor.getCount() - anchor.getHiddenCount() >= 13) {
                Card[] card = anchor.getCards();
                if (card[anchor.getCount() - 1].getValue() == 1) {
                    int suit = card[anchor.getCount() - 1].getSuit();
                    int val = 2;
                    for (int i = anchor.getCount() - 2; i >= 0 && val < 14; i--, val++) {
                        if (card[i].getValue() != val || card[i].getSuit() != suit) {
                            break;
                        }
                    }
                    if (val == 14) {
                        for (int j = 0; j < 13; j++) {
                            cardAnchors[11].addCard(anchor.popCard());
                        }
                        moveHistory.push(new Move(anchor.getNumber(), 11, 13, true, anchor.unhideTopCard()));

                        if (cardAnchors[11].getCount() == cardCount) {
                            signalWin();
                        }
                    }
                }
            }
            if (isStillDealing) {
                // Post another event if we aren't isDone yet.
                eventAlert(EVENT_DEAL_NEXT, cardAnchors[anchor.getNumber() + 1]);
            }
        } else if (event == EVENT_DEAL) {
            if (cardAnchors[10].getCount() > 0) {
                int count = cardAnchors[10].getCount() > 10 ? 10 : cardAnchors[10].getCount();
                animateCard.moveCard(cardAnchors[10].popCard(), cardAnchors[0]);
                moveHistory.push(new Move(10, 0, count - 1, 1, false, false));
                isStillDealing = true;
            }
        } else if (event == EVENT_DEAL_NEXT) {
            if (cardAnchors[10].getCount() > 0 && anchor.getNumber() < 10) {
                animateCard.moveCard(cardAnchors[10].popCard(), anchor);
            } else {
                view.stopAnimating();
                isStillDealing = false;
            }
        }
    }

    @Override
    public String getGameTypeString() {
        int suits = view.getSettings().getInt("SpiderSuits", 4);
        if (suits == 1) {
            return "Spider1Suit";
        } else if (suits == 2) {
            return "Spider2Suit";
        } else {
            return "Spider4Suit";
        }
    }

    @Override
    public String getPrettyGameTypeString() {
        int suits = view.getSettings().getInt("SpiderSuits", 4);
        if (suits == 1) {
            return "Spider One Suit";
        } else if (suits == 2) {
            return "Spider Two Suit";
        } else {
            return "Spider Four Suit";
        }
    }

    @Override
    public boolean hasString() {
        return true;
    }

    @Override
    public String getString() {
        int dealCount = cardAnchors[10].getCount() / 10;
        if (dealCount == 1) {
            return "1 deal left";
        }
        return dealCount + " deals left";
    }

}

class Freecell extends Rules {

    public void init(Bundle map) {
        ignoreEvents = true;

        // Thirteen total anchors for regular solitaire
        cardCount = 52;
        cardAnchorCount = 16;
        cardAnchors = new CardAnchor[cardAnchorCount];

        // Top anchors for holding cards
        for (int i = 0; i < 4; i++) {
            cardAnchors[i] = CardAnchor.createAnchor(CardAnchor.FREECELL_HOLD, i, this);
        }

        // Top anchors for sinking cards
        for (int i = 0; i < 4; i++) {
            cardAnchors[i + 4] = CardAnchor.createAnchor(CardAnchor.SEQ_SINK, i + 4, this);
        }

        // Middle anchor stacks
        for (int i = 0; i < 8; i++) {
            cardAnchors[i + 8] = CardAnchor.createAnchor(CardAnchor.FREECELL_STACK, i + 8,
                    this);
        }

        if (map != null) {
            // Do some assertions, default to a new game if we find an invalid state
            if (map.getInt("cardAnchorCount") == 16 &&
                    map.getInt("cardCount") == 52) {
                int[] cardCount = map.getIntArray("anchorCardCount");
                int[] hiddenCount = map.getIntArray("anchorHiddenCount");
                int[] value = map.getIntArray("value");
                int[] suit = map.getIntArray("suit");
                int cardIdx = 0;

                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
                        Card card = new Card(value[cardIdx], suit[cardIdx]);
                        cardAnchors[i].addCard(card);
                    }
                    cardAnchors[i].setHiddenCount(hiddenCount[i]);
                }

                ignoreEvents = false;
                // Return here so an invalid save state will result in a new game
                return;
            }
        }

        deck = new Deck(1);
        while (!deck.isEmpty()) {
            for (int i = 0; i < 8 && !deck.isEmpty(); i++) {
                cardAnchors[i + 8].addCard(deck.popCard());
            }
        }
        ignoreEvents = false;
    }

    public void resize(int width, int height) {
        int rem = (width - (Card.WIDTH * 8)) / 8;
        for (int i = 0; i < 8; i++) {
            cardAnchors[i].setPosition(rem / 2 + i * (rem + Card.WIDTH), 10);
            cardAnchors[i + 8].setPosition(rem / 2 + i * (rem + Card.WIDTH), 30 + Card.HEIGHT);
            cardAnchors[i + 8].setMaxHeight(height - 30 - Card.HEIGHT);
        }

        // Setup edge cards (Touch sensor loses sensitivity towards the edge).
        cardAnchors[0].setLeftEdge(0);
        cardAnchors[7].setRightEdge(width);
        cardAnchors[8].setLeftEdge(0);
        cardAnchors[15].setRightEdge(width);
        for (int i = 0; i < 8; i++) {
            cardAnchors[i + 8].setBottom(height);
        }
    }

    public void eventProcess(int event, CardAnchor anchor) {
        if (ignoreEvents) {
            return;
        }
        if (event == EVENT_STACK_ADD) {
            if (anchor.getNumber() >= 4 && anchor.getNumber() < 8) {
                if (cardAnchors[4].getCount() == 13 && cardAnchors[5].getCount() == 13 &&
                        cardAnchors[6].getCount() == 13 && cardAnchors[7].getCount() == 13) {
                    signalWin();
                } else {
                    if (autoMoveLevel == AUTO_MOVE_ALWAYS ||
                            (autoMoveLevel == AUTO_MOVE_FLING_ONLY && wasFling)) {
                        eventAlert(EVENT_SMART_MOVE);
                    } else {
                        view.stopAnimating();
                        wasFling = false;
                    }
                }
            }
        }
    }

    @Override
    public boolean fling(MoveCard moveCard) {
        if (moveCard.getCount() == 1) {
            CardAnchor anchor = moveCard.getAnchor();
            Card card = moveCard.dumpCards(false)[0];
            for (int i = 0; i < 4; i++) {
                if (cardAnchors[i + 4].dropSingleCard(card)) {
                    eventAlert(EVENT_FLING, anchor, card);
                    return true;
                }
            }
            anchor.addCard(card);
        } else {
            moveCard.release();
        }

        return false;
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor, Card card) {
        if (ignoreEvents) {
            anchor.addCard(card);
            return;
        }
        if (event == EVENT_FLING) {
            wasFling = true;
            if (!TryToSinkCard(anchor, card)) {
                anchor.addCard(card);
                wasFling = false;
            }
        } else {
            anchor.addCard(card);
        }
    }

    private boolean TryToSink(CardAnchor anchor) {
        Card card = anchor.popCard();
        boolean ret = TryToSinkCard(anchor, card);
        if (!ret) {
            anchor.addCard(card);
        }
        return ret;
    }

    private boolean TryToSinkCard(CardAnchor anchor, Card card) {
        for (int i = 0; i < 4; i++) {
            if (cardAnchors[i + 4].dropSingleCard(card)) {
                animateCard.moveCard(card, cardAnchors[i + 4]);
                moveHistory.push(new Move(anchor.getNumber(), i + 4, 1, false, false));
                return true;
            }
        }

        return false;
    }

    @Override
    public void eventProcess(int event) {
        if (ignoreEvents) {
            return;
        }

        if (event == EVENT_SMART_MOVE) {
            for (int i = 0; i < 4; i++) {
                if (cardAnchors[i].getCount() > 0 &&
                        TryToSink(cardAnchors[i])) {
                    return;
                }
            }
            for (int i = 0; i < 8; i++) {
                if (cardAnchors[i + 8].getCount() > 0 &&
                        TryToSink(cardAnchors[i + 8])) {
                    return;
                }
            }
            wasFling = false;
            view.stopAnimating();
        }
    }

    @Override
    public int countFreeSpaces() {
        int free = 0;
        for (int i = 0; i < 4; i++) {
            if (cardAnchors[i].getCount() == 0) {
                free++;
            }
        }
        for (int i = 0; i < 8; i++) {
            if (cardAnchors[i + 8].getCount() == 0) {
                free++;
            }
        }
        return free;
    }

    @Override
    public String getGameTypeString() {
        return "Freecell";
    }

    @Override
    public String getPrettyGameTypeString() {
        return "Freecell";
    }
}

class FortyThieves extends Rules {

    public void init(Bundle map) {
        ignoreEvents = true;

        cardCount = 104;
        cardAnchorCount = 20;
        cardAnchors = new CardAnchor[cardAnchorCount];

        // Anchor stacks
        for (int i = 0; i < 10; i++) {
            cardAnchors[i] = CardAnchor.createAnchor(CardAnchor.GENERIC_ANCHOR, i, this);
            cardAnchors[i].setBuildSeq(GenericAnchor.SEQ_DSC);
            cardAnchors[i].setMoveSeq(GenericAnchor.SEQ_ASC);
            cardAnchors[i].setSuit(GenericAnchor.SUIT_SAME);
            cardAnchors[i].setWrap(false);
            cardAnchors[i].setPickup(GenericAnchor.PACK_LIMIT_BY_FREE);
            cardAnchors[i].setDropoff(GenericAnchor.PACK_MULTI);
            cardAnchors[i].setDisplay(GenericAnchor.DISPLAY_ALL);
        }
        // Bottom anchors for holding cards
        for (int i = 0; i < 8; i++) {
            cardAnchors[i + 10] = CardAnchor.createAnchor(CardAnchor.SEQ_SINK, i + 10, this);
        }

        cardAnchors[18] = CardAnchor.createAnchor(CardAnchor.DEAL_FROM, 18, this);
        cardAnchors[19] = CardAnchor.createAnchor(CardAnchor.DEAL_TO, 19, this);

        if (map != null) {
            // Do some assertions, default to a new game if we find an invalid state
            if (map.getInt("cardAnchorCount") == 20 &&
                    map.getInt("cardCount") == 104) {
                int[] cardCount = map.getIntArray("anchorCardCount");
                int[] hiddenCount = map.getIntArray("anchorHiddenCount");
                int[] value = map.getIntArray("value");
                int[] suit = map.getIntArray("suit");
                int cardIdx = 0;

                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
                        Card card = new Card(value[cardIdx], suit[cardIdx]);
                        cardAnchors[i].addCard(card);
                    }
                    cardAnchors[i].setHiddenCount(hiddenCount[i]);
                }

                ignoreEvents = false;
                // Return here so an invalid save state will result in a new game
                return;
            }
        }

        deck = new Deck(2);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 4; j++) {
                cardAnchors[i].addCard(deck.popCard());
            }
        }
        while (!deck.isEmpty()) {
            cardAnchors[18].addCard(deck.popCard());
        }
        ignoreEvents = false;
    }

    public void resize(int width, int height) {
        int rem = (width - (Card.WIDTH * 10)) / 10;
        for (int i = 0; i < 10; i++) {
            cardAnchors[i].setMaxHeight(height - 30 - Card.HEIGHT);
            cardAnchors[i].setPosition(rem / 2 + i * (rem + Card.WIDTH), 30 + Card.HEIGHT);

            cardAnchors[i + 10].setPosition(rem / 2 + i * (rem + Card.WIDTH), 10);
        }

        // Setup edge cards (Touch sensor loses sensitivity towards the edge).
        cardAnchors[0].setLeftEdge(0);
        cardAnchors[9].setRightEdge(width);
        cardAnchors[10].setLeftEdge(0);
        cardAnchors[19].setRightEdge(width);
        for (int i = 0; i < 10; i++) {
            cardAnchors[i].setBottom(height);
        }
    }

    @Override
    public boolean fling(MoveCard moveCard) {
        if (moveCard.getCount() == 1) {
            CardAnchor anchor = moveCard.getAnchor();
            Card card = moveCard.dumpCards(false)[0];
            for (int i = 0; i < 8; i++) {
                if (cardAnchors[i + 10].dropSingleCard(card)) {
                    eventPoster.postEvent(EVENT_FLING, anchor, card);
                    return true;
                }
            }
            anchor.addCard(card);
        } else {
            moveCard.release();
        }
        return false;
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor, Card card) {
        if (ignoreEvents) {
            anchor.addCard(card);
            return;
        }
        if (event == EVENT_FLING) {
            wasFling = true;
            if (!tryToSinkCard(anchor, card)) {
                anchor.addCard(card);
                wasFling = false;
            }
        } else {
            anchor.addCard(card);
        }
    }

    private boolean tryToSink(CardAnchor anchor) {
        Card card = anchor.popCard();
        boolean ret = tryToSinkCard(anchor, card);
        if (!ret) {
            anchor.addCard(card);
        }
        return ret;
    }

    private boolean tryToSinkCard(CardAnchor anchor, Card card) {
        for (int i = 0; i < 8; i++) {
            if (cardAnchors[i + 10].dropSingleCard(card)) {
                animateCard.moveCard(card, cardAnchors[i + 10]);
                moveHistory.push(new Move(anchor.getNumber(), i + 10, 1, false, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public void eventProcess(int event, CardAnchor anchor) {
        if (ignoreEvents) {
            return;
        }
        if (event == EVENT_DEAL) {
            if (cardAnchors[18].getCount() > 0) {
                cardAnchors[19].addCard(cardAnchors[18].popCard());
                if (cardAnchors[18].getCount() == 0) {
                    cardAnchors[18].setDone(true);
                }
                moveHistory.push(new Move(18, 19, 1, true, false));
            }
        } else if (event == EVENT_STACK_ADD) {
            if (anchor.getNumber() >= 10 && anchor.getNumber() < 18) {
                if (cardAnchors[10].getCount() == 13 && cardAnchors[11].getCount() == 13 &&
                        cardAnchors[12].getCount() == 13 && cardAnchors[13].getCount() == 13 &&
                        cardAnchors[14].getCount() == 13 && cardAnchors[15].getCount() == 13 &&
                        cardAnchors[16].getCount() == 13 && cardAnchors[17].getCount() == 13) {
                    signalWin();
                } else {
                    if (autoMoveLevel == AUTO_MOVE_ALWAYS ||
                            (autoMoveLevel == AUTO_MOVE_FLING_ONLY && wasFling)) {
                        eventPoster.postEvent(EVENT_SMART_MOVE);
                    } else {
                        view.stopAnimating();
                        wasFling = false;
                    }
                }
            }
        }
    }

    @Override
    public void eventProcess(int event) {
        if (ignoreEvents) {
            return;
        }

        if (event == EVENT_SMART_MOVE) {
            for (int i = 0; i < 10; i++) {
                if (cardAnchors[i].getCount() > 0 &&
                        tryToSink(cardAnchors[i])) {
                    return;
                }
            }
            wasFling = false;
            view.stopAnimating();
        }
    }

    @Override
    public int countFreeSpaces() {
        int free = 0;
        for (int i = 0; i < 10; i++) {
            if (cardAnchors[i].getCount() == 0) {
                free++;
            }
        }
        return free;
    }

    @Override
    public String getGameTypeString() {
        return "Forty Thieves";
    }

    @Override
    public String getPrettyGameTypeString() {
        return "Forty Thieves";
    }

    @Override
    public boolean hasString() {
        return true;
    }

    @Override
    public String getString() {
        int cardsLeft = cardAnchors[18].getCount();
        if (cardsLeft == 1) {
            return "1 card left";
        }
        return cardsLeft + " cards left";
    }

}


class EventPoster {
    private int event;
    private CardAnchor cardAnchor;
    private Card card;
    private Rules rules;

    public EventPoster(Rules rules) {
        this.rules = rules;
        event = -1;
        cardAnchor = null;
        card = null;
    }

    public void postEvent(int event) {
        postEvent(event, null, null);
    }

    public void postEvent(int event, CardAnchor anchor) {
        postEvent(event, anchor, null);
    }

    public void postEvent(int theEvent, CardAnchor theAnchor, Card theCard) {
        event = theEvent;
        cardAnchor = theAnchor;
        card = theCard;
    }


    public void clearEvent() {
        event = Rules.EVENT_INVALID;
        cardAnchor = null;
        card = null;
    }

    public boolean hasEvent() {
        return event != Rules.EVENT_INVALID;
    }

    public void handleEvent() {
        if (hasEvent()) {
            int event = this.event;
            CardAnchor cardAnchor = this.cardAnchor;
            Card card = this.card;
            clearEvent();
            if (cardAnchor != null && card != null) {
                rules.eventProcess(event, cardAnchor, card);
            } else if (cardAnchor != null) {
                rules.eventProcess(event, cardAnchor);
            } else {
                rules.eventProcess(event);
            }
        }
    }
}


