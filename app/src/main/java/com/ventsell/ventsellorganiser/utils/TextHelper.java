package com.ventsell.ventsellorganiser.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import com.ventsell.ventsellorganiser.R;

import static android.content.Intent.ACTION_VIEW;

public class TextHelper {

    private static final String TAG = TextHelper.class.getSimpleName();

    @SuppressWarnings("deprecation")
    public static Spanned plainTextToHtml(String source) {
        Spanned str;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            str = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            str = Html.fromHtml(source);
        }
        return str;
    }

    public static String htmlToPlainText(String source) {
        char[] chars = new char[plainTextToHtml(source).length()];
        TextUtils.getChars(source, 0, source.length(), chars, 0);
        return new String(chars);
    }

    @NonNull
    public static SpannableString createLinkedText(final Context context, final String text, final int startIndex, final int lastIndex, final String linkUrl) {
        SpannableString forgotPassSpanStr = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                PackageManager pm = context.getPackageManager();
                Intent startIntent = new Intent(ACTION_VIEW, Uri.parse(linkUrl));
                if (startIntent.resolveActivity(pm) != null) {
                    context.startActivity(startIntent);
                } else {
                    Log.d("ventsell", TAG + "> No Activity available to open: " + linkUrl);
                }
            }
        };
        forgotPassSpanStr.setSpan(clickableSpan, startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        forgotPassSpanStr.setSpan(new UnderlineSpan(), startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        forgotPassSpanStr.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimaryDark)), startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return forgotPassSpanStr;
    }
}
