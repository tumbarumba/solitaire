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


public class Move {
    private static final int FLAGS_INVERT = 0x0001;
    private static final int FLAGS_UNHIDE = 0x0002;
    private static final int FLAGS_ADD_DEAL_COUNT = 0x0004;
    private int from;
    private int toBegin;
    private int toEnd;
    private int count;
    private int flags;

    public Move(int from, int toBegin, int toEnd, int count, boolean invert,
                boolean unhide) {
        this.from = from;
        this.toBegin = toBegin;
        this.toEnd = toEnd;
        this.count = count;
        flags = 0;
        if (invert)
            flags |= FLAGS_INVERT;
        if (unhide)
            flags |= FLAGS_UNHIDE;
    }

    public Move(int from, int to, int count, boolean invert,
                boolean unhide) {
        this.from = from;
        toBegin = to;
        toEnd = to;
        this.count = count;
        flags = 0;
        if (invert) {
            flags |= FLAGS_INVERT;
        }
        if (unhide) {
            flags |= FLAGS_UNHIDE;
        }
    }

    public Move(int from, int to, int count, boolean invert,
                boolean unhide, boolean addDealCount) {
        this.from = from;
        toBegin = to;
        toEnd = to;
        this.count = count;
        flags = 0;
        if (invert) {
            flags |= FLAGS_INVERT;
        }
        if (unhide) {
            flags |= FLAGS_UNHIDE;
        }
        if (addDealCount) {
            flags |= FLAGS_ADD_DEAL_COUNT;
        }
    }

    public Move(int from, int toBegin, int toEnd, int count, int flags) {
        this.from = from;
        this.toBegin = toBegin;
        this.toEnd = toEnd;
        this.count = count;
        this.flags = flags;
    }

    public int getFrom() {
        return from;
    }

    public int getToBegin() {
        return toBegin;
    }

    public int getToEnd() {
        return toEnd;
    }

    public int getCount() {
        return count;
    }

    public int getFlags() {
        return flags;
    }

    public boolean getInvert() {
        return (flags & FLAGS_INVERT) != 0;
    }

    public boolean getUnhide() {
        return (flags & FLAGS_UNHIDE) != 0;
    }

    public boolean getAddDealCount() {
        return (flags & FLAGS_ADD_DEAL_COUNT) != 0;
    }
}
