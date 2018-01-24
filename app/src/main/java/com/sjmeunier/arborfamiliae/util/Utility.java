package com.sjmeunier.arborfamiliae.util;

import android.text.Html;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    public static Spanned getHtmlString(String value) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            result = Html.fromHtml(value);
        }
        return result;
    }

    public static String wrap(final String str, int wrapLength, String newLineStr) {
        if (str == null) {
            return null;
        }

        if (wrapLength < 1) {
            wrapLength = 1;
        }
        String wrapOn = " ";

        Pattern patternToWrapOn = Pattern.compile(wrapOn);
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

        while (offset < inputLineLength) {
            int spaceToWrapAt = -1;
            Matcher matcher = patternToWrapOn.matcher(str.substring(offset, Math.min(offset + wrapLength + 1, inputLineLength)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    offset += matcher.end();
                    continue;
                }else {
                    spaceToWrapAt = matcher.start();
                }
            }

            // only last line without leading spaces is left
            if(inputLineLength - offset <= wrapLength) {
                break;
            }

            while(matcher.find()){
                spaceToWrapAt = matcher.start() + offset;
            }

            if (spaceToWrapAt >= offset) {
                // normal case
                wrappedLine.append(str.substring(offset, spaceToWrapAt));
                wrappedLine.append(newLineStr);
                offset = spaceToWrapAt + 1;

            } else {
                // do not wrap really long word, just extend beyond limit
                matcher = patternToWrapOn.matcher(str.substring(offset + wrapLength));
                if (matcher.find()) {
                    spaceToWrapAt = matcher.start() + offset + wrapLength;
                }

                if (spaceToWrapAt >= 0) {
                    wrappedLine.append(str.substring(offset, spaceToWrapAt));
                    wrappedLine.append(newLineStr);
                    offset = spaceToWrapAt + 1;
                } else {
                    wrappedLine.append(str.substring(offset));
                    offset = inputLineLength;
                }
            }
        }

        // Whatever is left in line is short enough to just pass through
        wrappedLine.append(str.substring(offset));

        return wrappedLine.toString();
    }
}
