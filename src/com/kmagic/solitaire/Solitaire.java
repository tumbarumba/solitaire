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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

// Base activity class.
public class Solitaire extends Activity {
    private static final int MENU_NEW_GAME = 1;
    private static final int MENU_RESTART = 2;
    private static final int MENU_OPTIONS = 3;
    private static final int MENU_SAVE_QUIT = 4;
    private static final int MENU_DEAL = 5;
    private static final int MENU_SOLITAIRE = 6;
    private static final int MENU_SPIDER = 7;
    private static final int MENU_FREECELL = 8;
    private static final int MENU_FORTYTHIEVES = 9;
    private static final int MENU_STATS = 10;
    private static final int MENU_HELP = 11;

    // View extracted from main.xml.
    private View mainView;
    private SolitaireView solitaireView;
    private SharedPreferences settings;

    private boolean doSave;

    // Shared preferences are where the various user settings are stored.
    public SharedPreferences GetSettings() {
        return settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doSave = true;

        // Force landscape and no title for extra room
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // If the user has never accepted the EULA show it again.
        settings = getSharedPreferences("SolitairePreferences", 0);
        setContentView(R.layout.main);
        mainView = findViewById(R.id.main_view);
        solitaireView = (SolitaireView) findViewById(R.id.solitaire);
        solitaireView.setTextView((TextView) findViewById(R.id.text));

        //StartSolitaire(savedInstanceState);
    }

    // Entry point for starting the game.
    //public void StartSolitaire(Bundle savedInstanceState) {
    @Override
    public void onStart() {
        super.onStart();
        if (settings.getBoolean("SolitaireSaveValid", false)) {
            SharedPreferences.Editor editor = GetSettings().edit();
            editor.putBoolean("SolitaireSaveValid", false);
            editor.commit();
            // If save is corrupt, just start a new game.
            if (solitaireView.loadSave()) {
                helpSplashScreen();
                return;
            }
        }

        solitaireView.initGame(settings.getInt("LastType", Rules.SOLITAIRE));
        helpSplashScreen();
    }

    // Force show the help if this is the first time played. Sadly no one reads
    // it anyways.
    private void helpSplashScreen() {
        if (!settings.getBoolean("PlayedBefore", false)) {
            solitaireView.displayHelp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        SubMenu subMenu = menu.addSubMenu(0, MENU_NEW_GAME, 0, R.string.menu_newgame);
        subMenu.add(0, MENU_SOLITAIRE, 0, R.string.menu_solitaire);
        subMenu.add(0, MENU_SPIDER, 0, R.string.menu_spider);
        subMenu.add(0, MENU_FREECELL, 0, R.string.menu_freecell);
        subMenu.add(0, MENU_FORTYTHIEVES, 0, R.string.menu_fortythieves);

        menu.add(0, MENU_RESTART, 0, R.string.menu_restart);
        menu.add(0, MENU_OPTIONS, 0, R.string.menu_options);
        menu.add(0, MENU_SAVE_QUIT, 0, R.string.menu_save_quit);
        menu.add(0, MENU_DEAL, 0, R.string.menu_deal);
        menu.add(0, MENU_STATS, 0, R.string.menu_stats);
        menu.add(0, MENU_HELP, 0, R.string.menu_help);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SOLITAIRE:
                solitaireView.initGame(Rules.SOLITAIRE);
                break;
            case MENU_SPIDER:
                solitaireView.initGame(Rules.SPIDER);
                break;
            case MENU_FREECELL:
                solitaireView.initGame(Rules.FREECELL);
                break;
            case MENU_FORTYTHIEVES:
                solitaireView.initGame(Rules.FORTYTHIEVES);
                break;
            case MENU_RESTART:
                solitaireView.restartGame();
                break;
            case MENU_STATS:
                displayStats();
                break;
            case MENU_OPTIONS:
                displayOptions();
                break;
            case MENU_HELP:
                solitaireView.displayHelp();
                break;
            case MENU_SAVE_QUIT:
                solitaireView.saveGame();
                doSave = false;
                finish();
                break;
            case MENU_DEAL:
                solitaireView.deal();
                break;
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        solitaireView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (doSave) {
            solitaireView.saveGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        solitaireView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void displayOptions() {
        solitaireView.setTimePassing(false);
        new Options(this, solitaireView.getDrawMaster());
    }

    public void displayStats() {
        solitaireView.setTimePassing(false);
        new Stats(this, solitaireView);
    }

    public void cancelOptions() {
        setContentView(mainView);
        solitaireView.requestFocus();
        solitaireView.setTimePassing(true);
    }

    public void newOptions() {
        setContentView(mainView);
        solitaireView.initGame(settings.getInt("LastType", Rules.SOLITAIRE));
    }

    // This is called for option changes that require a refresh, but not a new game
    public void refreshOptions() {
        setContentView(mainView);
        solitaireView.refreshOptions();
    }
}
