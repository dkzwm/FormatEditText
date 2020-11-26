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
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2017/2/22.
 *
 * @author dkzwm
 */
public class FormattedEditText extends ClearEditText {
    public static final int MODE_NONE = -1;
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_COMPLEX = 1;
    public static final int MODE_MASK = 2;
    public static final int MODE_HINT = 3;
    private static final Object SELECTION_SPAN = new Object();
    private static final InputFilter[] EMPTY_FILTERS = new InputFilter[0];
    private static final Spanned EMPTY_SPANNED = new SpannedString("");
    private static final String DEFAULT_PLACE_HOLDER = " ";
    private static final String DEFAULT_MARK = "*";
    private static final int DIGIT_MASK_POINT = "0".codePointAt(0);
    private static final int LETTER_MASK_POINT = "A".codePointAt(0);
    private static final int DIGIT_OR_LETTER_MASK_POINT = "*".codePointAt(0);
    private static final int CHARACTER_MASK_POINT = "?".codePointAt(0);
    private static final int ESCAPE_MASK_POINT = "\\".codePointAt(0);
    @Mode private int mMode = MODE_NONE;
    private String mPlaceholder;
    private String mEmptyPlaceholder;
    private String mMark;
    private String mHintText;
    private String mFormatStyle;
    private String mOriginalFormatStyle;
    private boolean mShowHintWhileEmpty = false;
    private int mHintColor = -1;
    private boolean mIsFormatted = false;
    private List<TextWatcher> mWatchers;
    private FormattedTextWatcher mTextWatcher;
    private LengthFilterDelegate mLengthFilterDelegate;
    private boolean mRestoring = false;
    private boolean mFilterRestoreTextChangeEvent = false;
    private SparseArray<Matcher> mMaskFilters;
    private SparseArray<PlaceholderConverter> mPlaceholderFilters;

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
        mTextWatcher = new FormattedTextWatcher();
        super.addTextChangedListener(mTextWatcher);
        if (attrs != null) {
            TypedArray ta =
                    context.obtainStyledAttributes(
                            attrs, R.styleable.FormattedEditText, defStyleAttr, 0);
            try {
                @Mode int mode = ta.getInt(R.styleable.FormattedEditText_fet_mode, MODE_NONE);
                String mark = ta.getString(R.styleable.FormattedEditText_fet_mark);
                int hintColor = ta.getColor(R.styleable.FormattedEditText_fet_hintTextColor, -1);
                String placeHolder = ta.getString(R.styleable.FormattedEditText_fet_placeholder);
                String emptyPlaceHolder =
                        ta.getString(R.styleable.FormattedEditText_fet_emptyPlaceholder);
                String formatStyle = ta.getString(R.styleable.FormattedEditText_fet_formatStyle);
                String hintText = ta.getString(R.styleable.FormattedEditText_fet_hintText);
                boolean showHintWhileEmpty =
                        ta.getBoolean(R.styleable.FormattedEditText_fet_showHintWhileEmpty, false);
                setConfig(
                        Config.create()
                                .mode(mode)
                                .placeholder(
                                        (placeHolder == null || placeHolder.length() == 0)
                                                ? DEFAULT_PLACE_HOLDER
                                                : placeHolder)
                                .hintColor(hintColor)
                                .hintText(hintText)
                                .mark((mark == null || mark.length() == 0) ? DEFAULT_MARK : mark)
                                .emptyPlaceholder(
                                        (emptyPlaceHolder == null || emptyPlaceHolder.length() == 0)
                                                ? null
                                                : emptyPlaceHolder)
                                .formatStyle(formatStyle)
                                .showHintWhileEmpty(showHintWhileEmpty),
                        true);
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mWatchers == null) {
            mWatchers = new ArrayList<>();
        }
        mWatchers.add(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mWatchers != null) {
            mWatchers.remove(watcher);
        }
    }

    @Override
    @CallSuper
    public void setFilters(InputFilter[] filters) {
        if (filters == null) {
            throw new IllegalArgumentException("filters can not be null");
        }
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] instanceof InputFilter.LengthFilter) {
                mLengthFilterDelegate = new LengthFilterDelegate(filters[i]);
                filters[i] = mLengthFilterDelegate;
            }
        }
        super.setFilters(filters);
    }

    @Mode
    public int getMode() {
        return mMode;
    }

    public String getFormatStyle() {
        return mFormatStyle;
    }

    public String getPlaceholder() {
        return mPlaceholder;
    }

    public String getEmptyPlaceholder() {
        return mEmptyPlaceholder;
    }

    public String getMark() {
        return mMark;
    }

    public String getHintText() {
        return mHintText;
    }

    public boolean isShowHintWhileEmpty() {
        return mShowHintWhileEmpty;
    }

    public int getHintColor() {
        return mHintColor;
    }

    private void setConfig(Config config, boolean create) {
        if (config.mMode != null) {
            mMode = config.mMode;
        }
        if (mMode == MODE_NONE) {
            return;
        }
        if (config.mFormatStyle != null && config.mFormatStyle.length() > 0) {
            mOriginalFormatStyle = config.mFormatStyle;
            if (mMode == MODE_SIMPLE) {
                if (config.mPlaceholder != null) {
                    if (config.mPlaceholder.codePointCount(0, config.mPlaceholder.length()) > 1) {
                        throw new IllegalArgumentException(
                                "emptyPlaceholder must be null or length one character");
                    }
                    mPlaceholder = config.mPlaceholder;
                }
                parseSimpleFormatStyle();
            } else if (mMode == MODE_COMPLEX) {
                if (config.mMark != null) {
                    if (config.mMark.codePointCount(0, config.mMark.length()) > 1) {
                        throw new IllegalArgumentException(
                                "mark must be null or length one character");
                    }
                    mMark = config.mMark;
                }
                parseComplexFormatStyle();
            } else {
                mFormatStyle = mOriginalFormatStyle;
                if (mMode == MODE_HINT) {
                    checkHintStyleIsRight(config.mHintText);
                }
            }
        } else if (mFormatStyle != null) {
            if (mMode == MODE_SIMPLE) {
                if (config.mPlaceholder != null && !mPlaceholder.equals(config.mPlaceholder)) {
                    if (config.mPlaceholder.codePointCount(0, config.mPlaceholder.length()) > 1) {
                        throw new IllegalArgumentException(
                                "emptyPlaceholder must be null or length one character");
                    }
                    mPlaceholder = config.mPlaceholder;
                    parseSimpleFormatStyle();
                }
            } else if (mMode == MODE_COMPLEX) {
                if (config.mMark != null && !mMark.equals(config.mMark)) {
                    if (config.mMark.codePointCount(0, config.mMark.length()) > 1) {
                        throw new IllegalArgumentException(
                                "mark must be null or length one character");
                    }
                    mMark = config.mMark;
                    parseComplexFormatStyle();
                }
            } else if (mMode == MODE_HINT) {
                checkHintStyleIsRight(config.mHintText);
            }
        } else {
            throw new IllegalArgumentException("formatStyle can not be empty");
        }
        if (config.mShowHintWhileEmpty != null) {
            mShowHintWhileEmpty = config.mShowHintWhileEmpty;
        }
        if (config.mEmptyPlaceholder != null) {
            mEmptyPlaceholder = config.mEmptyPlaceholder;
        }
        if (config.mHintColor != null) {
            mHintColor = config.mHintColor;
        }
        if (config.mMaskFilters != null) {
            mMaskFilters = config.mMaskFilters;
        }
        if (config.mPlaceholderFilters != null) {
            mPlaceholderFilters = config.mPlaceholderFilters;
        }
        Editable text = getText();
        if (!create) {
            setText(getRealText());
            text = getText();
        } else {
            if (text == null) {
                setText("");
                return;
            }
            formatText(text, true);
        }
        if (text != null) {
            Selection.setSelection(text, text.length());
        }
    }

    private void parseSimpleFormatStyle() {
        final int length = mOriginalFormatStyle.length();
        StringBuilder builder = new StringBuilder();
        for (int cp, cc, i = 0; i < length; i += cc) {
            cp = Character.codePointAt(mOriginalFormatStyle, i);
            if (!Character.isDigit(cp)) {
                throw new IllegalArgumentException("formatStyle must be numeric");
            }
            int count = Character.getNumericValue(cp);
            while (count > 0) {
                builder.appendCodePoint(DIGIT_MASK_POINT);
                count -= 1;
            }
            cc = Character.charCount(cp);
            if (i + cc < length) builder.append(mPlaceholder);
        }
        mFormatStyle = builder.toString();
    }

    private void parseComplexFormatStyle() {
        if (!mOriginalFormatStyle.contains(mMark)) {
            throw new IllegalArgumentException("formatStyle must be have Mark strings");
        }
        final int length = mOriginalFormatStyle.length();
        StringBuilder builder = new StringBuilder();
        for (int cp, i = 0; i < length; i += Character.charCount(cp)) {
            cp = Character.codePointAt(mOriginalFormatStyle, i);
            if (cp == mMark.codePointAt(0)) {
                builder.appendCodePoint(DIGIT_MASK_POINT);
            } else if (cp == DIGIT_OR_LETTER_MASK_POINT
                    || cp == DIGIT_MASK_POINT
                    || cp == LETTER_MASK_POINT
                    || cp == ESCAPE_MASK_POINT) {
                builder.appendCodePoint(ESCAPE_MASK_POINT);
                builder.append(cp);
            } else {
                builder.appendCodePoint(cp);
            }
        }
        mFormatStyle = builder.toString();
    }

    public String getRealText() {
        return getRealText(false);
    }

    private String getRealText(boolean saved) {
        if (saved && mMode == MODE_NONE) {
            return null;
        }
        Editable editable = getText();
        if (editable == null || editable.length() == 0) {
            return "";
        }
        SpannableStringBuilder value = new SpannableStringBuilder(editable);
        if (mMode == MODE_NONE) {
            if (saved) {
                value.clear();
                return null;
            }
        } else {
            clearPlaceholders(value);
        }
        final String realText = value.toString();
        value.clear();
        return realText;
    }

    private void clearPlaceholders(Editable value) {
        IPlaceholderSpan[] spans = value.getSpans(0, value.length(), IPlaceholderSpan.class);
        for (IPlaceholderSpan span : spans) {
            value.delete(value.getSpanStart(span), value.getSpanEnd(span));
        }
    }

    private void checkHintStyleIsRight(String hintText) {
        if (hintText != null) {
            mHintText = hintText;
            int indexInStyle = 0;
            int indexInText = 0;
            boolean nextCharIsText = false;
            while (indexInStyle < mFormatStyle.length()) {
                if (indexInText >= mHintText.length()) {
                    throw new IllegalArgumentException(
                            "hintText style must be conform to formatting style");
                }
                int charInStyle = mFormatStyle.codePointAt(indexInStyle);
                int charInText = mHintText.codePointAt(indexInText);
                if (!nextCharIsText && isMaskChar(charInStyle)) {
                    if (isMismatchMask(mHintText, indexInText, charInStyle, charInText)) {
                        throw new IllegalArgumentException(
                                "hintText style must be conform to formatting style");
                    } else {
                        indexInText += Character.charCount(charInText);
                        indexInStyle += Character.charCount(indexInStyle);
                    }
                } else if (!nextCharIsText && charInStyle == ESCAPE_MASK_POINT) {
                    nextCharIsText = true;
                    indexInStyle += Character.charCount(ESCAPE_MASK_POINT);
                } else {
                    if (charInStyle != charInText) {
                        throw new IllegalArgumentException(
                                "hintText style must be conform to formatting style");
                    }
                    nextCharIsText = false;
                    indexInText += 1;
                    indexInStyle += 1;
                }
            }
            if (mHintText.length() != indexInText) {
                throw new IllegalArgumentException(
                        "hintText style must be conform to formatting style");
            }
        }
    }

    private void sendBeforeTextChanged(CharSequence s, int start, int count, int after) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).beforeTextChanged(s, start, count, after);
            }
        }
    }

    private void sendOnTextChanged(CharSequence s, int start, int before, int count) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).onTextChanged(s, start, before, count);
            }
        }
    }

    private void sendAfterTextChanged(Editable s) {
        final List<TextWatcher> list = mWatchers;
        if (list != null) {
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).afterTextChanged(s);
            }
        }
    }

    private void formatText(final Editable editable, boolean append) {
        mIsFormatted = true;
        final boolean filter = mFilterRestoreTextChangeEvent;
        super.removeTextChangedListener(mTextWatcher);
        InputFilter[] filters = editable.getFilters();
        editable.setFilters(EMPTY_FILTERS);
        int selectionStart, selectionEnd;
        if (!filter) {
            selectionStart = Selection.getSelectionStart(editable);
            selectionEnd = Selection.getSelectionEnd(editable);
            editable.setSpan(SELECTION_SPAN, selectionStart, selectionEnd, Spanned.SPAN_MARK_MARK);
        }
        formatMask(editable);
        if (!filter) {
            selectionStart = editable.getSpanStart(SELECTION_SPAN);
            selectionEnd = editable.getSpanEnd(SELECTION_SPAN);
            editable.removeSpan(SELECTION_SPAN);
            editable.setFilters(filters);
            if (append) {
                if (mLengthFilterDelegate != null) {
                    CharSequence out =
                            mLengthFilterDelegate.mFilter.filter(
                                    editable, 0, editable.length(), EMPTY_SPANNED, 0, 0);
                    if (out != null) {
                        editable.delete(out.length(), editable.length());
                    }
                }
            }
            Selection.setSelection(
                    editable,
                    Math.min(selectionStart, editable.length()),
                    Math.min(selectionEnd, editable.length()));
        } else {
            editable.setFilters(filters);
        }
        mIsFormatted = false;
        super.addTextChangedListener(mTextWatcher);
    }

    private void formatMask(final Editable editable) {
        clearPlaceholders(editable);
        if (editable.length() == 0 && isNeedClearText()) {
            return;
        }
        int indexInStyle = 0;
        int indexInText = 0;
        boolean nextCharIsText = false;
        int indexOfLastLiteral = 0;
        int indexOfEmptyOrHintStart = 0;
        boolean havingEmptyOrHint = false;
        while (indexInStyle < mFormatStyle.length()) {
            int charInStyle = mFormatStyle.codePointAt(indexInStyle);
            int charInStyleCount = Character.charCount(charInStyle);
            if (!nextCharIsText && isMaskChar(charInStyle)) {
                if (indexInText >= editable.length()) {
                    if (mMode == MODE_MASK) {
                        if (mEmptyPlaceholder != null) {
                            if (!havingEmptyOrHint) {
                                indexOfEmptyOrHintStart = indexInText;
                            }
                            editable.insert(indexInText, mEmptyPlaceholder);
                            editable.setSpan(
                                    new EmptyPlaceholderSpan(),
                                    indexInText,
                                    indexInText + mEmptyPlaceholder.length(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            indexInText += mEmptyPlaceholder.length();
                            indexInStyle += charInStyleCount;
                            havingEmptyOrHint = true;
                        } else {
                            break;
                        }
                    } else {
                        if (mHintText == null) {
                            break;
                        }
                        if (!havingEmptyOrHint) {
                            indexOfEmptyOrHintStart = indexInText;
                        }
                        int charInText = mHintText.codePointAt(indexInText);
                        int charCount = Character.charCount(charInText);
                        editable.insert(
                                indexInText,
                                mHintText.subSequence(indexInText, indexInText + charCount));
                        editable.setSpan(
                                new HintPlaceholderSpan(
                                        mHintColor == -1 ? getCurrentHintTextColor() : mHintColor),
                                indexInText,
                                indexInText + charCount,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        indexInText += charCount;
                        indexInStyle += Character.charCount(charInStyle);
                        havingEmptyOrHint = true;
                    }
                    continue;
                }
                int charInText = Character.codePointAt(editable, indexInText);
                int charCount = Character.charCount(charInText);
                if (isMismatchMask(editable, indexInText, charInStyle, charInText)) {
                    editable.delete(indexInText, indexInText + charCount);
                } else {
                    indexInText += charCount;
                    indexInStyle += Character.charCount(charInStyle);
                    indexOfLastLiteral = indexInText;
                }
            } else if (!nextCharIsText && charInStyle == ESCAPE_MASK_POINT) {
                nextCharIsText = true;
                indexInStyle += Character.charCount(ESCAPE_MASK_POINT);
            } else {
                String cur = new StringBuilder().appendCodePoint(charInStyle).toString();
                if (mPlaceholderFilters != null) {
                    PlaceholderConverter converter = mPlaceholderFilters.get(charInStyle);
                    if (converter != null) {
                        char[] chars = new char[indexInText];
                        editable.getChars(0, indexInText, chars, 0);
                        String textInStyle = converter.convert(String.valueOf(chars), cur);
                        if (textInStyle == null
                                || textInStyle.codePointCount(0, textInStyle.length()) != 1) {
                            throw new IllegalArgumentException(
                                    "the converted must be length one character");
                        }
                        cur = textInStyle;
                    }
                }
                int charCount = cur.length();
                editable.insert(indexInText, cur);
                editable.setSpan(
                        new PlaceholderSpan(),
                        indexInText,
                        indexInText + charCount,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                nextCharIsText = false;
                indexInText += charCount;
                indexInStyle += charCount;
            }
        }
        if (!havingEmptyOrHint && indexOfLastLiteral != editable.length()) {
            editable.delete(indexOfLastLiteral, editable.length());
        } else if (havingEmptyOrHint
                && indexOfEmptyOrHintStart != 0
                && editable.getSpanStart(SELECTION_SPAN) == 0) {
            editable.removeSpan(SELECTION_SPAN);
            editable.setSpan(
                    SELECTION_SPAN,
                    indexOfEmptyOrHintStart,
                    indexOfEmptyOrHintStart,
                    Spanned.SPAN_MARK_MARK);
        }
    }

    private boolean isNeedClearText() {
        return (mMode == MODE_MASK && (mShowHintWhileEmpty || mEmptyPlaceholder == null))
                || (mMode == MODE_HINT && (mShowHintWhileEmpty || mHintText == null));
    }

    private boolean isMismatchMask(CharSequence sequence, int end, int mask, int value) {
        if (mMaskFilters != null) {
            Matcher matcher = mMaskFilters.get(mask);
            if (matcher != null) {
                String pre;
                if (sequence instanceof Editable) {
                    char[] chars = new char[end];
                    ((Editable) sequence).getChars(0, end, chars, 0);
                    pre = String.valueOf(chars);
                } else {
                    pre = sequence.subSequence(0, end).toString();
                }
                String cur = new StringBuilder().appendCodePoint(value).toString();
                return !matcher.hasMatch(pre, cur);
            }
            return false;
        }
        return mask != CHARACTER_MASK_POINT
                && (mask != LETTER_MASK_POINT || !Character.isLetter(value))
                && (mask != DIGIT_MASK_POINT || !Character.isDigit(value))
                && (mask != DIGIT_OR_LETTER_MASK_POINT
                        || (!Character.isDigit(value) && !Character.isLetter(value)));
    }

    private boolean isMaskChar(int mask) {
        if (mMaskFilters != null) {
            return mMaskFilters.indexOfKey(mask) >= 0;
        }
        return mask == DIGIT_MASK_POINT
                || mask == LETTER_MASK_POINT
                || mask == DIGIT_OR_LETTER_MASK_POINT
                || mask == CHARACTER_MASK_POINT;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mMode = mMode;
        savedState.mPlaceholder = mPlaceholder;
        savedState.mEmptyPlaceholder = mEmptyPlaceholder;
        savedState.mMark = mMark;
        savedState.mHintText = mHintText;
        savedState.mFormatStyle = mFormatStyle;
        savedState.mShowHintWhileEmpty = mShowHintWhileEmpty;
        savedState.mHintColor = mHintColor;
        savedState.mSelectionStart = start;
        savedState.mSelectionEnd = end;
        savedState.mRealText = getRealText(true);
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        mMode = savedState.mMode;
        mPlaceholder = savedState.mPlaceholder;
        mEmptyPlaceholder = savedState.mEmptyPlaceholder;
        mMark = savedState.mMark;
        mHintText = savedState.mHintText;
        mFormatStyle = savedState.mFormatStyle;
        mOriginalFormatStyle = savedState.mOriginalFormatStyle;
        mShowHintWhileEmpty = savedState.mShowHintWhileEmpty;
        mHintColor = savedState.mHintColor;
        if (savedState.mRealText != null) {
            mRestoring = true;
            super.onRestoreInstanceState(savedState.getSuperState());
            mRestoring = false;
            mFilterRestoreTextChangeEvent = true;
            setText(savedState.mRealText);
            mFilterRestoreTextChangeEvent = false;
            Editable text = getText();
            Selection.setSelection(
                    text,
                    Math.min(savedState.mSelectionStart, text.length()),
                    Math.min(savedState.mSelectionEnd, text.length()));
        } else {
            super.onRestoreInstanceState(savedState.getSuperState());
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_NONE, MODE_SIMPLE, MODE_COMPLEX, MODE_MASK, MODE_HINT})
    @interface Mode {}

    public interface Matcher {
        boolean hasMatch(String previousText, String value);
    }

    public interface PlaceholderConverter {
        String convert(String previousText, String value);
    }

    private interface IPlaceholderSpan {}

    private interface IEmptyPlaceholderSpan extends IPlaceholderSpan {}

    private static class PlaceholderSpan implements IPlaceholderSpan {}

    private static class EmptyPlaceholderSpan implements IEmptyPlaceholderSpan {}

    private static class HintPlaceholderSpan extends ForegroundColorSpan
            implements IEmptyPlaceholderSpan {
        HintPlaceholderSpan(int color) {
            super(color);
        }
    }

    public static class Config {
        private Integer mMode;
        private Integer mHintColor;
        private String mMark;
        private String mPlaceholder;
        private String mEmptyPlaceholder;
        private Boolean mShowHintWhileEmpty;
        private String mHintText;
        private String mFormatStyle;
        private SparseArray<Matcher> mMaskFilters;
        private SparseArray<PlaceholderConverter> mPlaceholderFilters;

        private Config() {}

        public static Config create() {
            return new Config();
        }

        public Config mode(int mode) {
            mMode = mode;
            return this;
        }

        public Config hintText(String hintText) {
            mHintText = hintText;
            return this;
        }

        public Config mark(String mark) {
            mMark = mark;
            return this;
        }

        public Config placeholder(String placeholder) {
            mPlaceholder = placeholder;
            return this;
        }

        public Config showHintWhileEmpty(boolean showHintWhileEmpty) {
            mShowHintWhileEmpty = showHintWhileEmpty;
            return this;
        }

        public Config formatStyle(String formatStyle) {
            mFormatStyle = formatStyle;
            return this;
        }

        public Config hintColor(int hintColor) {
            mHintColor = hintColor;
            return this;
        }

        public Config emptyPlaceholder(String emptyPlaceholder) {
            mEmptyPlaceholder = emptyPlaceholder;
            return this;
        }

        public Config maskFilter(String mark, Matcher matcher) {
            if (mark.codePointCount(0, mark.length()) > 1) {
                throw new IllegalArgumentException("mark must be length one character");
            }
            if (mMaskFilters == null) {
                mMaskFilters = new SparseArray<>();
            }
            mMaskFilters.put(mark.codePointAt(0), matcher);
            return this;
        }

        public Config placeholderFilter(String mark, PlaceholderConverter converter) {
            if (mark.codePointCount(0, mark.length()) > 1) {
                throw new IllegalArgumentException("mark must be length one character");
            }
            if (mPlaceholderFilters == null) {
                mPlaceholderFilters = new SparseArray<>();
            }
            mPlaceholderFilters.put(mark.codePointAt(0), converter);
            return this;
        }

        public void config(FormattedEditText editText) {
            editText.setConfig(this, false);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        private int mMode = MODE_NONE;
        private String mPlaceholder;
        private String mEmptyPlaceholder;
        private String mMark;
        private String mHintText;
        private String mFormatStyle;
        private String mOriginalFormatStyle;
        private String mRealText;
        private boolean mShowHintWhileEmpty = false;
        private int mHintColor = -1;
        private int mSelectionStart;
        private int mSelectionEnd;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mMode = in.readInt();
            mPlaceholder = in.readString();
            mEmptyPlaceholder = in.readString();
            mMark = in.readString();
            mRealText = in.readString();
            mHintText = in.readString();
            mFormatStyle = in.readString();
            mOriginalFormatStyle = in.readString();
            mShowHintWhileEmpty = in.readInt() != 0;
            mHintColor = in.readInt();
            mSelectionStart = in.readInt();
            mSelectionEnd = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mMode);
            out.writeString(mPlaceholder);
            out.writeString(mEmptyPlaceholder);
            out.writeString(mMark);
            out.writeString(mRealText);
            out.writeString(mHintText);
            out.writeString(mFormatStyle);
            out.writeString(mOriginalFormatStyle);
            out.writeInt(mShowHintWhileEmpty ? 1 : 0);
            out.writeInt(mHintColor);
            out.writeInt(mSelectionStart);
            out.writeInt(mSelectionEnd);
        }
    }

    private class LengthFilterDelegate implements InputFilter {
        private final InputFilter mFilter;

        private LengthFilterDelegate(InputFilter filter) {
            mFilter = filter;
        }

        @Override
        public CharSequence filter(
                CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (mRestoring) {
                return null;
            }
            if (!mIsFormatted && (mMode >= MODE_MASK)) {
                IEmptyPlaceholderSpan[] spans =
                        dest.getSpans(0, dest.length(), IEmptyPlaceholderSpan.class);
                if (spans.length == 0) {
                    return mFilter.filter(source, start, end, dest, dstart, dend);
                }
                return null;
            }
            return mFilter.filter(source, start, end, dest, dstart, dend);
        }
    }

    private class FormattedTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mRestoring) {
                return;
            }
            sendBeforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mRestoring) {
                return;
            }
            sendOnTextChanged(s, start, before, count);
            if (mMode == MODE_NONE) {
                return;
            }
            if (!mIsFormatted && s instanceof Editable) {
                formatText((Editable) s, count != 0);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mRestoring) {
                return;
            }
            sendAfterTextChanged(s);
        }
    }
}
