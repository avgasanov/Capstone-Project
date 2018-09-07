package com.tuesday_apps.catchmycar.utils;

import android.util.Log;

import java.util.ArrayList;

public class TextUtils {
    public static ArrayList<String> textToLines(String text, int lineMaxChar) {
        Log.v("GIHH", "Text: " + text);
        Log.v("GIHH", "Text length: " + String.valueOf(text.length()));
        Log.v("GIHH", "lineMaxChar: " + String.valueOf(lineMaxChar));
        ArrayList<String> textLines = new ArrayList<>();

        if (text.length() < lineMaxChar) {
            textLines.add(text);
            return  textLines;
        }

        while (text.length() > lineMaxChar) {
                int lineEnd = text.substring(0, lineMaxChar).lastIndexOf(" ");
                if (lineEnd == -1) lineEnd = lineMaxChar;
                textLines.add(text.substring(0, lineEnd));
                text = text.substring(lineEnd).trim();
                if (text.length() < lineMaxChar) textLines.add(text);
        }

        return textLines;
    }
}
