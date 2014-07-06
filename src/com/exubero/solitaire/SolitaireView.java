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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Stack;

// The brains of the operation
public class SolitaireView extends View {

    private static final int MODE_NORMAL = 1;
    private static final int MODE_MOVE_CARD = 2;
    private static final int MODE_CARD_SELECT = 3;
    private static final int MODE_TEXT = 4;
    private static final int MODE_ANIMATE = 5;
    private static final int MODE_WIN = 6;
    private static final int MODE_WIN_STOP = 7;

    private static final String SAVE_FILENAME = "solitaire_save.bin";
    // This is incremented only when the save system changes.
    private static final String SAVE_VERSION = "solitaire_save_2";

    private CharSequence helpText;
    private CharSequence winText;

    private CardAnchor[] cardAnchors;
    private DrawMaster drawMaster;
    private Rules rules;
    private TextView textView;
    private AnimateCard animateCard;

    private MoveCard moveCard;
    private SelectCard selectCard;
    private int viewMode;
    private boolean textViewDown;

    private PointF lastPoint;
    private PointF downPoint;
    private RefreshHandler refreshHandler;
    private Thread refreshThread;
    private Stack<Move> moveHistory;
    private Replay replay;
    private Context context;
    private boolean hasMoved;
    private Speed speed;

    private Card[] undoStorage;

    private int elapsed = 0;
    private long startTime;
    private boolean timePaused;

    private boolean isGameStarted;
    private boolean isPaused;
    private boolean isDisplayTime;

    private int winningScore;

    public SolitaireView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);

        drawMaster = new DrawMaster(context);
        moveCard = new MoveCard();
        selectCard = new SelectCard();
        viewMode = MODE_NORMAL;
        lastPoint = new PointF();
        downPoint = new PointF();
        refreshHandler = new RefreshHandler(this);
        refreshThread = new Thread(refreshHandler);
        moveHistory = new Stack<Move>();
        undoStorage = new Card[CardAnchor.MAX_CARDS];
        animateCard = new AnimateCard(this);
        speed = new Speed();
        replay = new Replay(this, animateCard);

        helpText = context.getResources().getText(R.string.help_text);
        winText = context.getResources().getText(R.string.win_text);
        this.context = context;
        textViewDown = false;
        refreshThread.start();
        winningScore = 0;
    }

    public void initGame(int gameType) {
        int oldScore = 0;
        String oldGameType = "None";

        // We really really want focus :)
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        SharedPreferences.Editor editor = getSettings().edit();
        if (rules != null) {
            if (rules.hasScore()) {
                if (viewMode == MODE_WIN || viewMode == MODE_WIN_STOP) {
                    oldScore = winningScore;
                } else {
                    oldScore = rules.getScore();
                }
                oldGameType = rules.getGameTypeString();
                if (oldScore > getSettings().getInt(rules.getGameTypeString() + "Score", -52)) {
                    editor.putInt(rules.getGameTypeString() + "Score", oldScore);
                }
            }
        }
        changeViewMode(MODE_NORMAL);
        textView.setVisibility(View.INVISIBLE);
        moveHistory.clear();
        rules = Rules.createRules(gameType, null, this, moveHistory, animateCard);
        if (oldGameType == rules.getGameTypeString()) {
            rules.setCarryOverScore(oldScore);
        }
        Card.setSize(gameType);
        drawMaster.drawCards(getSettings().getBoolean("DisplayBigCards", false));
        cardAnchors = rules.getCardAnchors();
        if (drawMaster.getWidth() > 1) {
            rules.resize(drawMaster.getWidth(), drawMaster.getHeight());
            refresh();
        }
        setDisplayTime(getSettings().getBoolean("DisplayTime", true));
        editor.putInt("LastType", gameType);
        editor.commit();
        startTime = SystemClock.uptimeMillis();
        elapsed = 0;
        timePaused = false;
        isPaused = false;
        isGameStarted = false;
    }

    public SharedPreferences getSettings() {
        return ((Solitaire) context).GetSettings();
    }

    public DrawMaster getDrawMaster() {
        return drawMaster;
    }

    public Rules getRules() {
        return rules;
    }

    public void clearGameStarted() {
        isGameStarted = false;
    }

    public void setDisplayTime(boolean displayTime) {
        isDisplayTime = displayTime;
    }

    public void setTimePassing(boolean timePassing) {
        if (timePassing && (viewMode == MODE_WIN || viewMode == MODE_WIN_STOP)) {
            return;
        }
        if (timePassing && timePaused) {
            startTime = SystemClock.uptimeMillis() - elapsed;
            timePaused = false;
        } else if (!timePassing) {
            timePaused = true;
        }
    }

    public void updateTime() {
        if (!timePaused) {
            int elapsed = (int) (SystemClock.uptimeMillis() - startTime);
            if (elapsed / 1000 > this.elapsed / 1000) {
                refresh();
            }
            this.elapsed = elapsed;
        }
    }

    private void changeViewMode(int newMode) {
        switch (viewMode) {
            case MODE_NORMAL:
                if (newMode != MODE_NORMAL) {
                    drawBoard();
                }
                break;
            case MODE_MOVE_CARD:
                moveCard.release();
                drawBoard();
                break;
            case MODE_CARD_SELECT:
                selectCard.release();
                drawBoard();
                break;
            case MODE_TEXT:
                textView.setVisibility(View.INVISIBLE);
                break;
            case MODE_ANIMATE:
                refreshHandler.setRefresh(RefreshHandler.SINGLE_REFRESH);
                break;
            case MODE_WIN:
            case MODE_WIN_STOP:
                if (newMode != MODE_WIN_STOP) {
                    textView.setVisibility(View.INVISIBLE);
                }
                drawBoard();
                replay.stopPlaying();
                break;
        }
        viewMode = newMode;
        switch (newMode) {
            case MODE_WIN:
                setTimePassing(false);
            case MODE_MOVE_CARD:
            case MODE_CARD_SELECT:
            case MODE_ANIMATE:
                refreshHandler.setRefresh(RefreshHandler.LOCK_REFRESH);
                break;

            case MODE_NORMAL:
            case MODE_TEXT:
            case MODE_WIN_STOP:
                refreshHandler.setRefresh(RefreshHandler.SINGLE_REFRESH);
                break;
        }
    }

    public void onPause() {
        isPaused = true;

        if (refreshThread != null) {
            refreshHandler.setRunning(false);
            rules.clearEvent();
            rules.setIgnoreEvents(true);
            replay.stopPlaying();
            try {
                refreshThread.join(1000);
            } catch (InterruptedException e) {
            }
            refreshThread = null;
            if (animateCard.isAnimating()) {
                animateCard.cancel();
            }
            if (viewMode != MODE_WIN && viewMode != MODE_WIN_STOP) {
                changeViewMode(MODE_NORMAL);
            }

            if (rules != null && rules.getScore() > getSettings().getInt(rules.getGameTypeString() + "Score", -52)) {
                SharedPreferences.Editor editor = getSettings().edit();
                editor.putInt(rules.getGameTypeString() + "Score", rules.getScore());
                editor.commit();
            }
        }
    }

    public void saveGame() {
        // This is supposed to have been called but I've seen instances where it wasn't.
        if (refreshThread != null) {
            onPause();
        }

        if (rules != null && viewMode == MODE_NORMAL) {
            try {

                FileOutputStream fout = context.openFileOutput(SAVE_FILENAME, 0);
                ObjectOutputStream oout = new ObjectOutputStream(fout);

                int cardCount = rules.getCardCount();
                int[] value = new int[cardCount];
                int[] suit = new int[cardCount];
                int[] anchorCardCount = new int[cardAnchors.length];
                int[] anchorHiddenCount = new int[cardAnchors.length];
                int historySize = moveHistory.size();
                int[] historyFrom = new int[historySize];
                int[] historyToBegin = new int[historySize];
                int[] historyToEnd = new int[historySize];
                int[] historyCount = new int[historySize];
                int[] historyFlags = new int[historySize];
                Card[] card;

                cardCount = 0;
                for (int i = 0; i < cardAnchors.length; i++) {
                    anchorCardCount[i] = cardAnchors[i].getCount();
                    anchorHiddenCount[i] = cardAnchors[i].getHiddenCount();
                    card = cardAnchors[i].getCards();
                    for (int j = 0; j < anchorCardCount[i]; j++, cardCount++) {
                        value[cardCount] = card[j].getValue();
                        suit[cardCount] = card[j].getSuit();
                    }
                }

                for (int i = 0; i < historySize; i++) {
                    Move move = moveHistory.pop();
                    historyFrom[i] = move.getFrom();
                    historyToBegin[i] = move.getToBegin();
                    historyToEnd[i] = move.getToEnd();
                    historyCount[i] = move.getCount();
                    historyFlags[i] = move.getFlags();
                }

                oout.writeObject(SAVE_VERSION);
                oout.writeInt(cardAnchors.length);
                oout.writeInt(cardCount);
                oout.writeInt(rules.getType());
                oout.writeObject(anchorCardCount);
                oout.writeObject(anchorHiddenCount);
                oout.writeObject(value);
                oout.writeObject(suit);
                oout.writeInt(rules.getRulesExtra());
                oout.writeInt(rules.getScore());
                oout.writeInt(elapsed);
                oout.writeObject(historyFrom);
                oout.writeObject(historyToBegin);
                oout.writeObject(historyToEnd);
                oout.writeObject(historyCount);
                oout.writeObject(historyFlags);
                oout.close();

                SharedPreferences.Editor editor = getSettings().edit();
                editor.putBoolean("SolitaireSaveValid", true);
                editor.commit();

            } catch (FileNotFoundException e) {
                Log.e("SolitaireView.java", "onStop(): File not found");
            } catch (IOException e) {
                Log.e("SolitaireView.java", "onStop(): IOException");
            }
        }
    }

    public boolean loadSave() {
        drawMaster.drawCards(getSettings().getBoolean("DisplayBigCards", false));
        timePaused = true;

        try {
            FileInputStream fin = context.openFileInput(SAVE_FILENAME);
            ObjectInputStream oin = new ObjectInputStream(fin);

            String version = (String) oin.readObject();
            if (!version.equals(SAVE_VERSION)) {
                Log.e("SolitaireView.java", "Invalid save version");
                return false;
            }
            Bundle map = new Bundle();

            map.putInt("cardAnchorCount", oin.readInt());
            map.putInt("cardCount", oin.readInt());
            int type = oin.readInt();
            map.putIntArray("anchorCardCount", (int[]) oin.readObject());
            map.putIntArray("anchorHiddenCount", (int[]) oin.readObject());
            map.putIntArray("value", (int[]) oin.readObject());
            map.putIntArray("suit", (int[]) oin.readObject());
            map.putInt("rulesExtra", oin.readInt());
            map.putInt("score", oin.readInt());
            elapsed = oin.readInt();
            startTime = SystemClock.uptimeMillis() - elapsed;
            int[] historyFrom = (int[]) oin.readObject();
            int[] historyToBegin = (int[]) oin.readObject();
            int[] historyToEnd = (int[]) oin.readObject();
            int[] historyCount = (int[]) oin.readObject();
            int[] historyFlags = (int[]) oin.readObject();
            for (int i = historyFrom.length - 1; i >= 0; i--) {
                moveHistory.push(new Move(historyFrom[i], historyToBegin[i], historyToEnd[i],
                        historyCount[i], historyFlags[i]));
            }

            oin.close();

            isGameStarted = !moveHistory.isEmpty();
            rules = Rules.createRules(type, map, this, moveHistory, animateCard);
            Card.setSize(type);
            setDisplayTime(getSettings().getBoolean("DisplayTime", true));
            cardAnchors = rules.getCardAnchors();
            if (drawMaster.getWidth() > 1) {
                rules.resize(drawMaster.getWidth(), drawMaster.getHeight());
                refresh();
            }
            timePaused = false;
            return true;

        } catch (FileNotFoundException e) {
            Log.e("SolitaireView.java", "loadSave(): File not found");
        } catch (StreamCorruptedException e) {
            Log.e("SolitaireView.java", "loadSave(): Stream Corrupted");
        } catch (IOException e) {
            Log.e("SolitaireView.java", "loadSave(): IOException");
        } catch (ClassNotFoundException e) {
            Log.e("SolitaireView.java", "loadSave(): Class not found exception");
        }
        timePaused = false;
        isPaused = false;
        return false;
    }

    public void onResume() {
        startTime = SystemClock.uptimeMillis() - elapsed;
        refreshHandler.setRunning(true);
        refreshThread = new Thread(refreshHandler);
        refreshThread.start();
        rules.setIgnoreEvents(false);
        isPaused = false;
    }

    public void refresh() {
        refreshHandler.singleRefresh();
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        drawMaster.setScreenSize(w, h);
        rules.resize(w, h);
        selectCard.setHeight(h);
    }

    public void displayHelp() {
        textView.setTextSize(15);
        textView.setGravity(Gravity.LEFT);
        displayText(helpText);
    }

    public void displayWin() {
        markWin();
        textView.setTextSize(24);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        displayText(winText);
        changeViewMode(MODE_WIN);
        textView.setVisibility(View.VISIBLE);
        rules.setIgnoreEvents(true);
        replay.startReplay(moveHistory, cardAnchors);
    }

    public void restartGame() {
        rules.setIgnoreEvents(true);
        while (!moveHistory.empty()) {
            undo();
        }
        rules.setIgnoreEvents(false);
        refresh();
    }

    public void displayText(CharSequence text) {
        changeViewMode(MODE_TEXT);
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
        refresh();
    }

    public void drawBoard() {
        Canvas boardCanvas = drawMaster.getBoardCanvas();
        drawMaster.drawBackground(boardCanvas);
        for (int i = 0; i < cardAnchors.length; i++) {
            cardAnchors[i].draw(drawMaster, boardCanvas);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        // Only draw the stagnant stuff if it may have changed
        if (viewMode == MODE_NORMAL) {
            // sanityCheck is for debug use only.
            sanityCheck();
            drawBoard();
        }
        drawMaster.drawLastBoard(canvas);
        if (isDisplayTime) {
            drawMaster.drawTime(canvas, elapsed);
        }
        if (rules.hasString()) {
            drawMaster.drawRulesString(canvas, rules.getString());
        }

        switch (viewMode) {
            case MODE_MOVE_CARD:
                moveCard.draw(drawMaster, canvas);
                break;
            case MODE_CARD_SELECT:
                selectCard.draw(drawMaster, canvas);
                break;
            case MODE_WIN:
                if (replay.isPlaying()) {
                    animateCard.draw(drawMaster, canvas);
                }
            case MODE_WIN_STOP:
            case MODE_TEXT:
                drawMaster.drawShade(canvas);
                break;
            case MODE_ANIMATE:
                animateCard.draw(drawMaster, canvas);
        }

        rules.handleEvents();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_SEARCH:
                if (viewMode == MODE_TEXT) {
                    changeViewMode(MODE_NORMAL);
                } else if (viewMode == MODE_NORMAL) {
                    deal();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                undo();
                return true;
        }
        rules.handleEvents();
        return super.onKeyDown(keyCode, msg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;

        // Yes you can get touch events while in the "paused" state.
        if (isPaused) {
            return false;
        }

        // Text mode only handles clickys
        if (viewMode == MODE_TEXT) {
            if (event.getAction() == MotionEvent.ACTION_UP && textViewDown) {
                SharedPreferences.Editor editor = context.getSharedPreferences("SolitairePreferences", 0).edit();
                editor.putBoolean("PlayedBefore", true);
                editor.commit();
                textViewDown = false;
                changeViewMode(MODE_NORMAL);
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                textViewDown = true;
            }
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hasMoved = false;
                speed.reset();
                ret = onDown(event.getX(), event.getY());
                downPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ret = onRelease(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (!hasMoved) {
                    checkMoved(event.getX(), event.getY());
                }
                ret = onMove(lastPoint.x - event.getX(), lastPoint.y - event.getY(),
                        event.getX(), event.getY());
                break;
        }
        lastPoint.set(event.getX(), event.getY());

        if (!isGameStarted && !moveHistory.empty()) {
            isGameStarted = true;
            markAttempt();
        }

        rules.handleEvents();
        return ret;
    }

    private boolean onRelease(float x, float y) {
        switch (viewMode) {
            case MODE_NORMAL:
                if (!hasMoved) {
                    for (int i = 0; i < cardAnchors.length; i++) {
                        if (cardAnchors[i].expandStack(x, y)) {
                            selectCard.initFromAnchor(cardAnchors[i]);
                            changeViewMode(MODE_CARD_SELECT);
                            return true;
                        } else if (cardAnchors[i].tapCard(x, y)) {
                            refresh();
                            return true;
                        }
                    }
                }
                break;
            case MODE_MOVE_CARD:
                for (int close = 0; close < 2; close++) {
                    CardAnchor prevAnchor = moveCard.getAnchor();
                    boolean unhide = (prevAnchor.getVisibleCount() == 0 &&
                            prevAnchor.getCount() > 0);
                    int count = moveCard.getCount();

                    for (int i = 0; i < cardAnchors.length; i++) {
                        if (cardAnchors[i] != prevAnchor) {
                            if (cardAnchors[i].canDropCard(moveCard, close)) {
                                moveHistory.push(new Move(prevAnchor.getNumber(), i, count, false, unhide));
                                cardAnchors[i].addMoveCard(moveCard);
                                if (viewMode == MODE_MOVE_CARD) {
                                    changeViewMode(MODE_NORMAL);
                                }
                                return true;
                            }
                        }
                    }
                }
                if (!moveCard.hasMoved()) {
                    CardAnchor anchor = moveCard.getAnchor();
                    moveCard.release();
                    if (anchor.expandStack(x, y)) {
                        selectCard.initFromAnchor(anchor);
                        changeViewMode(MODE_CARD_SELECT);
                    } else {
                        changeViewMode(MODE_NORMAL);
                    }
                } else if (speed.isFast() && moveCard.getCount() == 1) {
                    if (!rules.fling(moveCard)) {
                        changeViewMode(MODE_NORMAL);
                    }
                } else {
                    moveCard.release();
                    changeViewMode(MODE_NORMAL);
                }
                return true;
            case MODE_CARD_SELECT:
                if (!selectCard.isOnCard() && !hasMoved) {
                    selectCard.release();
                    changeViewMode(MODE_NORMAL);
                    return true;
                }
                break;
        }

        return false;
    }

    public boolean onDown(float x, float y) {
        switch (viewMode) {
            case MODE_NORMAL:
                Card card = null;
                for (int i = 0; i < cardAnchors.length; i++) {
                    card = cardAnchors[i].grabCard(x, y);
                    if (card != null) {
                        if (y < card.getY() + Card.HEIGHT / 4) {
                            boolean lastIgnore = rules.getIgnoreEvents();
                            rules.setIgnoreEvents(true);
                            cardAnchors[i].addCard(card);
                            rules.setIgnoreEvents(lastIgnore);
                            if (cardAnchors[i].expandStack(x, y)) {
                                moveCard.initFromAnchor(cardAnchors[i], x - Card.WIDTH / 2, y - Card.HEIGHT / 2);
                                changeViewMode(MODE_MOVE_CARD);
                                break;
                            }
                            card = cardAnchors[i].popCard();
                        }
                        moveCard.setAnchor(cardAnchors[i]);
                        moveCard.addCard(card);
                        changeViewMode(MODE_MOVE_CARD);
                        break;
                    }
                }
                break;
            case MODE_CARD_SELECT:
                selectCard.tap(x, y);
                break;
        }
        return true;
    }

    public boolean onMove(float dx, float dy, float x, float y) {
        speed.addSpeed(dx, dy);
        switch (viewMode) {
            case MODE_NORMAL:
                if (Math.abs(downPoint.x - x) > 15 || Math.abs(downPoint.y - y) > 15) {
                    for (int i = 0; i < cardAnchors.length; i++) {
                        if (cardAnchors[i].canMoveStack(downPoint.x, downPoint.y)) {
                            moveCard.initFromAnchor(cardAnchors[i], x - Card.WIDTH / 2, y - Card.HEIGHT / 2);
                            changeViewMode(MODE_MOVE_CARD);
                            return true;
                        }
                    }
                }
                break;
            case MODE_MOVE_CARD:
                moveCard.movePosition(dx, dy);
                return true;
            case MODE_CARD_SELECT:
                if (selectCard.isOnCard() && Math.abs(downPoint.x - x) > 30) {
                    moveCard.initFromSelectCard(selectCard, x, y);
                    changeViewMode(MODE_MOVE_CARD);
                } else {
                    selectCard.scroll(dy);
                    if (!selectCard.isOnCard()) {
                        selectCard.tap(x, y);
                    }
                }
                return true;
        }

        return false;
    }

    private void checkMoved(float x, float y) {
        if (x >= downPoint.x - 30 && x <= downPoint.x + 30 &&
                y >= downPoint.y - 30 && y <= downPoint.y + 30) {
            hasMoved = false;
        } else {
            hasMoved = true;
        }
    }

    public void startAnimating() {
        drawBoard();
        if (viewMode != MODE_WIN && viewMode != MODE_ANIMATE) {
            changeViewMode(MODE_ANIMATE);
        }
    }

    public void stopAnimating() {
        if (viewMode == MODE_ANIMATE) {
            changeViewMode(MODE_NORMAL);
        } else if (viewMode == MODE_WIN) {
            changeViewMode(MODE_WIN_STOP);
        }
    }

    public void deal() {
        rules.eventAlert(Rules.EVENT_DEAL, cardAnchors[0]);
        refresh();
    }

    public void undo() {
        if (viewMode != MODE_NORMAL && viewMode != MODE_WIN) {
            return;
        }
        boolean oldIgnore = rules.getIgnoreEvents();
        rules.setIgnoreEvents(true);

        moveCard.release();
        selectCard.release();

        if (!moveHistory.empty()) {
            Move move = moveHistory.pop();
            int count = 0;
            int from = move.getFrom();
            if (move.getToBegin() != move.getToEnd()) {
                for (int i = move.getToBegin(); i <= move.getToEnd(); i++) {
                    for (int j = 0; j < move.getCount(); j++) {
                        undoStorage[count++] = cardAnchors[i].popCard();
                    }
                }
            } else {
                for (int i = 0; i < move.getCount(); i++) {
                    undoStorage[count++] = cardAnchors[move.getToBegin()].popCard();
                }
            }
            if (move.getUnhide()) {
                cardAnchors[from].setHiddenCount(cardAnchors[from].getHiddenCount() + 1);
            }
            if (move.getInvert()) {
                for (int i = 0; i < count; i++) {
                    cardAnchors[from].addCard(undoStorage[i]);
                }
            } else {
                for (int i = count - 1; i >= 0; i--) {
                    cardAnchors[from].addCard(undoStorage[i]);
                }
            }
            if (move.getAddDealCount()) {
                rules.addDealCount();
            }
            if (undoStorage[0].getValue() == 1) {
                for (int i = 0; i < cardAnchors[from].getCount(); i++) {
                    Card card = cardAnchors[from].getCards()[i];
                }
            }
            refresh();
        }
        rules.setIgnoreEvents(oldIgnore);
    }

    private void markAttempt() {
        String gameAttemptString = rules.getGameTypeString() + "Attempts";
        int attempts = getSettings().getInt(gameAttemptString, 0);
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putInt(gameAttemptString, attempts + 1);
        editor.commit();
    }

    private void markWin() {
        String gameWinString = rules.getGameTypeString() + "Wins";
        String gameTimeString = rules.getGameTypeString() + "Time";
        int wins = getSettings().getInt(gameWinString, 0);
        int bestTime = getSettings().getInt(gameTimeString, -1);
        SharedPreferences.Editor editor = getSettings().edit();

        if (bestTime == -1 || elapsed < bestTime) {
            editor.putInt(gameTimeString, elapsed);
        }

        editor.putInt(gameWinString, wins + 1);
        editor.commit();
        if (rules.hasScore()) {
            winningScore = rules.getScore();
            if (winningScore > getSettings().getInt(rules.getGameTypeString() + "Score", -52)) {
                editor.putInt(rules.getGameTypeString() + "Score", winningScore);
            }
        }
    }

    // Simple function to check for a consistent state in Solitaire.
    private void sanityCheck() {
        int cardCount;
        int matchCount;
        String type = rules.getGameTypeString();
        if ("Spider1Suit".equals(type)) {
            cardCount = 13;
            matchCount = 8;
        } else if ("Spider2Suit".equals(type)) {
            cardCount = 26;
            matchCount = 4;
        } else if ("Spider4Suit".equals(type)) {
            cardCount = 52;
            matchCount = 2;
        } else if ("Forty Thieves".equals(type)) {
            cardCount = 52;
            matchCount = 2;
        } else {
            cardCount = 52;
            matchCount = 1;
        }

        int[] cards = new int[cardCount];
        for (int i = 0; i < cardCount; i++) {
            cards[i] = 0;
        }
        for (int i = 0; i < cardAnchors.length; i++) {
            for (int j = 0; j < cardAnchors[i].getCount(); j++) {
                Card card = cardAnchors[i].getCards()[j];
                int idx = card.getSuit() * 13 + card.getValue() - 1;
                if (cards[idx] >= matchCount) {
                    textView.setTextSize(20);
                    textView.setGravity(Gravity.CENTER);
                    displayText("Sanity Check Failed\nExtra: " + card.getValue() + " " + card.getSuit());
                    return;
                }
                cards[idx]++;
            }
        }
        for (int i = 0; i < cardCount; i++) {
            if (cards[i] != matchCount) {
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                displayText("Sanity Check Failed\nMissing: " + (i % 13 + 1) + " " + i / 13);
                return;
            }
        }
    }

    public void refreshOptions() {
        rules.refreshOptions();
        setDisplayTime(getSettings().getBoolean("DisplayTime", true));
    }
}

class RefreshHandler implements Runnable {
    public static final int NO_REFRESH = 1;
    public static final int SINGLE_REFRESH = 2;
    public static final int LOCK_REFRESH = 3;

    private static final int FPS = 30;

    private boolean isRunning;
    private int refresh;
    private SolitaireView mView;

    public RefreshHandler(SolitaireView solitaireView) {
        mView = solitaireView;
        isRunning = true;
        refresh = NO_REFRESH;
    }

    public void setRefresh(int refresh) {
        synchronized (this) {
            this.refresh = refresh;
        }
    }

    public void singleRefresh() {
        synchronized (this) {
            if (refresh == NO_REFRESH) {
                refresh = SINGLE_REFRESH;
            }
        }
    }

    public void setRunning(boolean run) {
        isRunning = run;
    }

    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
            }
            mView.updateTime();
            if (refresh != NO_REFRESH) {
                mView.postInvalidate();
                if (refresh == SINGLE_REFRESH) {
                    setRefresh(NO_REFRESH);
                }
            }
        }
    }
}

class Speed {
    private static final int SPEED_COUNT = 4;
    private static final float SPEED_THRESHOLD = 10 * 10;

    private float[] speedBuffer;
    private int nextIndex;

    public Speed() {
        speedBuffer = new float[SPEED_COUNT];
        reset();
    }

    public void reset() {
        nextIndex = 0;
        for (int i = 0; i < SPEED_COUNT; i++) {
            speedBuffer[i] = 0;
        }
    }

    public void addSpeed(float dx, float dy) {
        speedBuffer[nextIndex] = dx * dx + dy * dy;
        nextIndex = (nextIndex + 1) % SPEED_COUNT;
    }

    public boolean isFast() {
        for (int i = 0; i < SPEED_COUNT; i++) {
            if (speedBuffer[i] > SPEED_THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}

