/*
 * MIT License
 *
 * Copyright (c) 2017 dkzwm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.dkzwm.widget.fet;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2017/2/22.
 *
 * @author dkzwm
 */
public class FormattedEditText extends EditText {
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_COMPLEX = 1;
    private static final String DEFAULT_PLACE_HOLDER = " ";
    private static final String DEFAULT_MARK = "*";
    private StringBuilder mBuilder = new StringBuilder();
    private Placeholder[] mHolders;
    private String mPlaceholder;
    private String mPlaceholders;
    private int mLastIndex;
    @Mode
    private int mMode = MODE_SIMPLE;
    private boolean mIsFormatted = false;
    private List<TextWatcher> mWatchers;
    private String mMark;
    private InputFilter mFilter;

    public FormattedEditText(Context context) {
        super(context);
        init(context, null, 0);
    }

    public FormattedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FormattedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        super.addTextChangedListener(new FormattedTextWatcher());
        if (attrs != null) {
            TypedArray ta =
                    context.obtainStyledAttributes(
                            attrs, R.styleable.FormattedEditText, defStyleAttr, 0);
            try {
                mMark = ta.getString(R.styleable.FormattedEditText_fet_mark);
                @Mode int mode = ta.getInt(R.styleable.FormattedEditText_fet_mode, MODE_SIMPLE);
                setMode(mode);
                String placeHolder = ta.getString(R.styleable.FormattedEditText_fet_placeholder);
                setPlaceholder(TextUtils.isEmpty(placeHolder) ? DEFAULT_PLACE_HOLDER : placeHolder);
                String formatStyle = ta.getString(R.styleable.FormattedEditText_fet_formatStyle);
                setFormatStyle(formatStyle);
            } finally {
                ta.recycle();
            }
        } else {
            setPlaceholder(DEFAULT_PLACE_HOLDER);
        }
        final CharSequence text = getText();
        if (mHolders != null && mHolders.length > 0 && text.length() > 0)
            formatTextWhenAppend(text, 0, text.length());
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mWatchers == null) mWatchers = new ArrayList<>();
        mWatchers.add(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mWatchers != null) mWatchers.remove(watcher);
    }

    public void setFormatStyle(String style) {
        if (style != null) {
            if (mMode == MODE_SIMPLE) {
                if (TextUtils.isDigitsOnly(style)) {
                    mHolders = new Placeholder[style.length()];
                    Placeholder holder = new Placeholder();
                    int index = Character.getNumericValue(style.charAt(0));
                    holder.index = index;
                    holder.holder = mPlaceholder;
                    mHolders[0] = holder;
                    for (int i = 1; i < style.length(); i++) {
                        int number = Character.getNumericValue(style.charAt(i));
                        holder = new Placeholder();
                        index = mHolders[i - 1].index + 1 + number;
                        holder.index = index;
                        holder.holder = mPlaceholder;
                        mHolders[i] = holder;
                    }
                } else throw new IllegalArgumentException("Format style must be numeric");
            } else {
                if (!style.contains(mMark))
                    throw new IllegalArgumentException("Format style must be have Mark strings");
                final Placeholder[] temp = new Placeholder[style.length()];
                final StringBuilder builder = new StringBuilder();
                int realCount = 0;
                Placeholder holder;
                for (int i = 0; i < style.length(); i++) {
                    final String sub = style.substring(i, i + 1);
                    if (!sub.equals(mMark)) {
                        if (builder.indexOf(sub) < 0) builder.append(sub);
                        holder = new Placeholder();
                        holder.index = i;
                        holder.holder = sub;
                        temp[realCount] = holder;
                        realCount++;
                    }
                }
                mHolders = new Placeholder[realCount];
                mPlaceholders = builder.toString();
                System.arraycopy(temp, 0, mHolders, 0, realCount);
                clearArray(temp);
            }
        } else {
            clearArray(mHolders);
            mHolders = null;
        }
    }

    @Override
    @CallSuper
    public void setFilters(InputFilter[] filters) {
        if (filters == null) throw new IllegalArgumentException();
        InputFilter[] replaceFilters = new InputFilter[filters.length + 1];
        if (mFilter == null) mFilter = new PlaceholderFilter();
        replaceFilters[0] = mFilter;
        System.arraycopy(filters, 0, replaceFilters, 1, filters.length);
        super.setFilters(replaceFilters);
    }

    public void setMode(@Mode int mode) {
        if (mMode != mode) {
            String originalText = getText().toString();
            mMode = mode;
            if (mMode == MODE_COMPLEX && TextUtils.isEmpty(mMark)) mMark = DEFAULT_MARK;
            if (!TextUtils.isEmpty(originalText)) setText(originalText);
        }
    }

    public void setMark(@NonNull String mark) {
        if (mark.length() > 1)
            throw new IllegalArgumentException("Mark only supports length one strings");
        mMark = mark;
    }

    public void setPlaceholder(@NonNull String placeholder) {
        if (!TextUtils.equals(mPlaceholder, placeholder)) {
            if (placeholder.length() > 1)
                throw new IllegalArgumentException("Placeholder only supports length one strings");
            if (mHolders != null) {
                final Placeholder[] placeholders = mHolders;
                for (Placeholder holder : placeholders) holder.holder = placeholder;
            }
            mPlaceholder = placeholder;
            if (mMode == MODE_SIMPLE) mPlaceholders = placeholder;
        }
    }

    private void sendBeforeTextChanged(CharSequence s, int start, int count, int after) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) list.get(i).beforeTextChanged(s, start, count, after);
        }
    }

    private void sendOnTextChanged(CharSequence s, int start, int before, int count) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) list.get(i).onTextChanged(s, start, before, count);
        }
    }

    private void sendAfterTextChanged(Editable s) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) list.get(i).afterTextChanged(s);
        }
    }

    private void clearArray(final Placeholder[] holders) {
        if (holders != null) for (int i = 0; i < holders.length; i++) holders[i] = null;
    }

    private void formatTextWhenDelete(final CharSequence s, int start, int before) {
        final String lastText = mBuilder.toString();
        final String currentText = s.toString();
        final boolean deletedLast = start >= currentText.length();
        mBuilder.delete(start, lastText.length());
        if (!deletedLast) formatTextNoCursor(currentText, start, 0);
        final String tempText = mBuilder.toString();
        mLastIndex = mHolders.length / 2;
        int pos = start;
        for (int i = pos; i > 0; i--) {
            final String sub = tempText.substring(i - 1, i);
            final String place = findPlaceholder(i - 1);
            if (sub.equals(place)) {
                if (deletedLast) mBuilder.delete(i - 1, i);
                pos--;
            } else break;
        }
        mIsFormatted = true;
        final String text = mBuilder.toString();
        final int realCount = lastText.length() - text.length();
        sendBeforeTextChanged(lastText, pos, realCount, 0);
        if (!deletedLast || pos != start || realCount != before) setText(text);
        mIsFormatted = false;
        setSelection(pos);
        sendOnTextChanged(text, pos, realCount, 0);
        sendAfterTextChanged(getText());
    }

    private void formatTextWhenAppend(final CharSequence s, int start, int count) {
        final String lastText = mBuilder.toString();
        final String currentText = s.toString();
        boolean appendedLast = start > mHolders[mHolders.length - 1].index;
        int afterAppendStart;
        if (!appendedLast) {
            mBuilder.delete(start, lastText.length());
            afterAppendStart = formatTextNoCursor(currentText, start, count);
        } else {
            afterAppendStart = start + count;
            mBuilder.insert(start, currentText.substring(start, afterAppendStart));
        }
        mIsFormatted = true;
        final String text = mBuilder.toString();
        final int realCount = text.length() - lastText.length();
        sendBeforeTextChanged(lastText, start, realCount, 0);
        if (!appendedLast || afterAppendStart != start + count || realCount != count) setText(text);
        mIsFormatted = false;
        setSelection(afterAppendStart);
        sendOnTextChanged(text, start, realCount, 0);
        sendAfterTextChanged(getText());
    }

    private int formatTextNoCursor(final String current, final int start, final int count) {
        final int length = current.length();
        int calcCount = count;
        int position = start;
        int afterAppendStart = start;
        final int maxPos = mHolders[mHolders.length - 1].index;
        mLastIndex = mHolders.length / 2;
        for (int i = start; i < length; i++) {
            if (mBuilder.length() > maxPos + 1) {
                afterAppendStart += calcCount < 0 ? 0 : calcCount;
                if (count > 0 && length >= maxPos + count) {
                    final int hasHolderEndIndex = maxPos + count + 1;
                    final int realEndIndex =
                            length >= hasHolderEndIndex ? hasHolderEndIndex : maxPos + count;
                    String substring = current.substring(i, realEndIndex);
                    final int len = substring.length();
                    for (int j = 0; j < len; j++) {
                        final String sub = substring.substring(j, j + 1);
                        if (!mPlaceholders.contains(sub)) mBuilder.append(sub);
                    }
                    mBuilder.append(current.substring(realEndIndex));
                    return afterAppendStart;
                }
                mBuilder.append(current.substring(i));
                return afterAppendStart;
            }
            final String sub = current.substring(i, i + 1);
            if (mPlaceholders.contains(sub)) {
                if (calcCount >= 0) calcCount--;
                continue;
            }
            final String place = findPlaceholder(position);
            if (place != null && !TextUtils.equals(place, sub)) {
                mBuilder.append(place);
                i--;
                position++;
                if (calcCount >= 0) afterAppendStart++;
            } else {
                mBuilder.append(sub);
                position++;
                calcCount--;
                if (calcCount >= 0) afterAppendStart++;
            }
        }
        return afterAppendStart;
    }

    private String findPlaceholder(int index) {
        final int len = mHolders.length;
        final int last = mLastIndex;
        final int centerIndex = mHolders[last].index;
        if (centerIndex == index) {
            return mHolders[last].holder;
        } else if (centerIndex < index) {
            for (int i = last; i < len; i++) {
                mLastIndex = i;
                if (mHolders[i].index == index) {
                    return mHolders[i].holder;
                } else if (mHolders[i].index > index) return null;
            }
        } else {
            for (int i = last; i >= 0; i--) {
                mLastIndex = i;
                if (mHolders[i].index == index) return mHolders[i].holder;
                else if (mHolders[i].index < index) return null;
            }
        }
        return null;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_SIMPLE, MODE_COMPLEX})
    @interface Mode {
    }

    private static class Placeholder {
        int index;
        String holder;
    }

    private class PlaceholderFilter implements InputFilter {
        private StringBuilder mFilterBuilder = new StringBuilder();

        @Override
        public CharSequence filter(
                CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (mPlaceholders == null || mIsFormatted || source.length() == 0) return null;
            mFilterBuilder.setLength(0);
            final int len = source.length();
            for (int i = 0; i < len; i++) {
                CharSequence sub = source.subSequence(i, i + 1);
                if (!mPlaceholders.contains(sub)) mFilterBuilder.append(sub);
            }
            return mFilterBuilder;
        }
    }

    private class FormattedTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mHolders == null || mHolders.length == 0)
                sendBeforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mHolders == null || mHolders.length == 0) {
                sendOnTextChanged(s, start, before, count);
                return;
            }
            if (!mIsFormatted) {
                if (count == 0) formatTextWhenDelete(s, start, before);
                else formatTextWhenAppend(s, start, count);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mHolders == null || mHolders.length == 0) sendAfterTextChanged(s);
            if (s.length() == 0 && mBuilder.length() != 0) mBuilder.setLength(0);
        }
    }
}
