## ExSolitaire

This is a clone of Ken Magic's [Solitaire for Android](https://code.google.com/p/solitaire-for-android/),
with updates to allow it to run on modern devices.

ExSolitaire is a collection different card games:
* Klondike (regular solitaire)
* Spider Solitaire
* Freecell
* Forty Thieves

Features include multi-level undo, animated card movement, and statistic/score tracking.

### Build Instructions

First off you need to download the SDK from (http://developer.android.com/sdk/) .
Once installed you need to modify solitaire's build.xml file to include the
path to the SDK (Two lines near the top). Then running ant should complete the
job. If you have difficulties, try building a sample program from
code.google.com first which has a bunch of detail and troubleshooting info.
