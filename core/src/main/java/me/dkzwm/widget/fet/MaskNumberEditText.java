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
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2020/10/25.
 *
 * @author dkzwm
 */
public class MaskNumberEditText extends ClearEditText {
    private static final Object SELECTION_SPAN = new Object();
    private static final InputFilter[] EMPTY_FILTERS = new InputFilter[0];
    private static final String DECIMAL_POINT = ".";
    private static final char DECIMAL_POINT_CHAR = DECIMAL_POINT.charAt(0);
    private static final String THOUSANDS_SEPARATOR = ",";
    private static final char THOUSANDS_SEPARATOR_CHAR = THOUSANDS_SEPARATOR.charAt(0);
    private static final String ZERO_PLACEHOLDER = "0";
    private static final char ZERO_PLACEHOLDER_CHAR = ZERO_PLACEHOLDER.charAt(0);
    private boolean mIsFormatted = false;
    private List<TextWatcher> mWatchers;
    private MaskNumberTextWatcher mTextWatcher;
    private boolean mRestoring = false;
    private boolean mFilterRestoreTextChangeEvent = false;
    private String mCurrencySymbol;
    private int mCurrencySymbolTextColor = -1;
    private int mDecimalLength = -1;
    private boolean mAutoFillNumbers = false;
    private int mAutoFillNumbersTextColor = -1;
    private boolean mShowThousandsSeparator = true;

    public MaskNumberEditText(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MaskNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MaskNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTextWatcher = new MaskNumberTextWatcher();
        super.addTextChangedListener(mTextWatcher);
        if (attrs != null) {
            TypedArray ta =
                    context.obtainStyledAttributes(
                            attrs, R.styleable.MaskNumberEditText, defStyleAttr, 0);
            try {
                setCurrencySymbol(ta.getString(R.styleable.MaskNumberEditText_fet_currencySymbol));
                setCurrencySymbolTextColor(
                        ta.getInt(R.styleable.MaskNumberEditText_fet_currencySymbolTextColor, -1));
                setDecimalLength(ta.getInt(R.styleable.MaskNumberEditText_fet_decimalLength, -1));
                setAutoFillNumbers(
                        ta.getBoolean(R.styleable.MaskNumberEditText_fet_autoFillNumbers, false));
                setAutoFillNumbersTextColor(
                        ta.getInt(R.styleable.MaskNumberEditText_fet_autoFillNumbersTextColor, -1));
                setShowThousandsSeparator(
                        ta.getBoolean(
                                R.styleable.MaskNumberEditText_fet_showThousandsSeparator, true));
            } finally {
                ta.recycle();
            }
        }
        Editable text = getText();
        if (text == null || text.length() == 0) {
            if (mAutoFillNumbers || mCurrencySymbol != null) {
                setText("");
            }
            return;
        }
        formatNumber(text);
        Selection.setSelection(text, text.length());
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

    public String getCurrencySymbol() {
        return mCurrencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        if (currencySymbol != null && currencySymbol.length() > 1) {
            throw new IllegalArgumentException(
                    "currencySymbol must be null or length one character");
        }
        mCurrencySymbol = currencySymbol;
    }

    public int getCurrencySymbolTextColor() {
        return mCurrencySymbolTextColor;
    }

    public void setCurrencySymbolTextColor(int currencySymbolTextColor) {
        mCurrencySymbolTextColor = currencySymbolTextColor;
    }

    public int getDecimalLength() {
        return mDecimalLength;
    }

    public void setDecimalLength(int decimalLength) {
        mDecimalLength = decimalLength;
    }

    public boolean isAutoFillNumbers() {
        return mAutoFillNumbers;
    }

    public void setAutoFillNumbers(boolean autoFillNumbers) {
        mAutoFillNumbers = autoFillNumbers;
    }

    public int getAutoFillNumbersTextColor() {
        return mAutoFillNumbersTextColor;
    }

    public void setAutoFillNumbersTextColor(int autoFillNumbersTextColor) {
        mAutoFillNumbersTextColor = autoFillNumbersTextColor;
    }

    public boolean isShowThousandsSeparator() {
        return mShowThousandsSeparator;
    }

    public void setShowThousandsSeparator(boolean showThousandsSeparator) {
        mShowThousandsSeparator = showThousandsSeparator;
    }

    public String getRealNumber() {
        return getRealNumber(false);
    }

    private String getRealNumber(boolean saved) {
        Editable editable = getText();
        if (editable == null || editable.length() == 0) {
            return "";
        }
        SpannableStringBuilder value = new SpannableStringBuilder(editable);
        clearPlaceholders(value);
        final String realText = value.toString();
        value.clear();
        if (!saved) {
            if (realText.length() > 0) {
                if (realText.charAt(realText.length() - 1) == DECIMAL_POINT_CHAR) {
                    return realText.substring(0, realText.length() - 1);
                } else if (realText.charAt(0) == DECIMAL_POINT_CHAR) {
                    return ZERO_PLACEHOLDER + realText;
                }
            }
        }
        return realText;
    }

    private void clearPlaceholders(Editable value) {
        IPlaceholderSpan[] spans = value.getSpans(0, value.length(), IPlaceholderSpan.class);
        for (IPlaceholderSpan span : spans) {
            value.delete(value.getSpanStart(span), value.getSpanEnd(span));
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

    private void formatNumber(final Editable editable) {
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
        clearPlaceholders(editable);
        DecimalPointSpan[] spans = editable.getSpans(0, editable.length(), DecimalPointSpan.class);
        if (spans.length > 0) {
            DecimalPointSpan span = spans[0];
            int start = editable.getSpanStart(span);
            int index = editable.toString().indexOf(DECIMAL_POINT_CHAR);
            if (index != start) {
                editable.delete(start, start + 1);
                span = new DecimalPointSpan(getCurrentTextColor());
                editable.setSpan(span, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            formatInteger(editable, editable.getSpanStart(span));
            int decimalPointIndex = editable.getSpanStart(span);
            boolean havingDecimal = formatDecimal(editable, decimalPointIndex);
            int spanColor = span.mColor;
            int color = havingDecimal ? getCurrentTextColor() : getFilledTextColorForSpan();
            if (spanColor != color) {
                start = editable.getSpanStart(span);
                editable.removeSpan(span);
                span = new DecimalPointSpan(color);
                editable.setSpan(span, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ZeroIntegerSpan[] zeroSpans =
                    editable.getSpans(0, decimalPointIndex, ZeroIntegerSpan.class);
            if (zeroSpans.length > 0) {
                ZeroIntegerSpan zeroSpan = zeroSpans[0];
                spanColor = zeroSpan.mColor;
                if (spanColor != color) {
                    start = editable.getSpanStart(zeroSpan);
                    editable.removeSpan(zeroSpan);
                    zeroSpan = new ZeroIntegerSpan(color);
                    editable.setSpan(zeroSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } else {
            int index = editable.toString().indexOf(DECIMAL_POINT_CHAR);
            if (index == -1) {
                formatInteger(editable, editable.length());
                if (mAutoFillNumbers && mDecimalLength > 0) {
                    index = editable.length();
                    editable.insert(editable.length(), DECIMAL_POINT);
                    editable.setSpan(
                            new DecimalPointSpan(getFilledTextColorForSpan()),
                            index,
                            index + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    formatDecimal(editable, editable.length());
                }
            } else {
                editable.setSpan(
                        new DecimalPointSpan(getCurrentTextColor()),
                        index,
                        index + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                formatInteger(editable, index);
                formatDecimal(editable, editable.toString().indexOf(DECIMAL_POINT_CHAR));
            }
        }
        formatCurrencySymbol(editable);
        if (!filter) {
            selectionStart = editable.getSpanStart(SELECTION_SPAN);
            selectionEnd = editable.getSpanEnd(SELECTION_SPAN);
            editable.removeSpan(SELECTION_SPAN);
            editable.setFilters(filters);
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

    private void formatCurrencySymbol(Editable editable) {
        if (mCurrencySymbol != null) {
            editable.insert(0, mCurrencySymbol);
            editable.setSpan(
                    new CurrencySymbolSpan(
                            mCurrencySymbolTextColor == -1
                                    ? getCurrentTextColor()
                                    : mCurrencySymbolTextColor),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int start = editable.getSpanStart(SELECTION_SPAN);
            if (start == 0) {
                editable.removeSpan(SELECTION_SPAN);
                editable.setSpan(SELECTION_SPAN, 1, 1, Spanned.SPAN_MARK_MARK);
            }
        }
    }

    private void formatInteger(Editable editable, int decimalPointIndex) {
        int position = 0;
        int index = decimalPointIndex;
        while (index > 0) {
            char integer = editable.charAt(index - 1);
            if (!Character.isDigit(integer)) {
                editable.delete(index - 1, index);
            } else {
                if (position != 0 && position % 3 == 0 && mShowThousandsSeparator) {
                    editable.insert(index, THOUSANDS_SEPARATOR);
                    editable.setSpan(
                            new ThousandsSeparatorSpan(),
                            index,
                            index + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                position++;
            }
            index--;
        }
        if (editable.length() > 0) {
            if (editable.charAt(0) == THOUSANDS_SEPARATOR_CHAR) {
                editable.delete(0, 1);
            } else if (editable.charAt(0) == DECIMAL_POINT_CHAR) {
                editable.insert(0, ZERO_PLACEHOLDER);
                editable.setSpan(
                        new ZeroIntegerSpan(getFilledTextColorForSpan()),
                        0,
                        1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (editable.charAt(0) == ZERO_PLACEHOLDER_CHAR) {
                if (editable.length() > 1 && Character.isDigit(editable.charAt(1))) {
                    index = 1;
                    while (index < editable.length()) {
                        char c = editable.charAt(index);
                        if (c != ZERO_PLACEHOLDER_CHAR) {
                            if (c == DECIMAL_POINT_CHAR) {
                                index -= 1;
                            }
                            break;
                        }
                        index++;
                    }
                    editable.delete(0, Math.min(index, editable.length() - 1));
                }
            }
        } else if (mAutoFillNumbers) {
            editable.insert(0, ZERO_PLACEHOLDER);
            editable.setSpan(
                    new ZeroIntegerSpan(getFilledTextColorForSpan()),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean formatDecimal(Editable editable, int decimalPointIndex) {
        boolean havingDecimal = false;
        int index = decimalPointIndex + 1;
        while (index < editable.length()) {
            char decimal = editable.charAt(index);
            if (!Character.isDigit(decimal)) {
                editable.delete(index, index + 1);
            } else {
                havingDecimal = true;
                index++;
            }
        }
        if (mDecimalLength >= 0) {
            final int decimalLength = editable.length() - 1 - decimalPointIndex;
            if (decimalLength > mDecimalLength) {
                editable.delete(decimalPointIndex + 1 + mDecimalLength, editable.length());
            } else if (decimalLength < mDecimalLength && mAutoFillNumbers) {
                index = editable.length();
                int count = decimalLength <= 0 ? mDecimalLength : mDecimalLength - decimalLength;
                while (count > 0) {
                    editable.insert(index, ZERO_PLACEHOLDER);
                    editable.setSpan(
                            new ZeroDecimalSpan(getFilledTextColorForSpan()),
                            index,
                            index + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    count--;
                }
            }
        }
        return havingDecimal && mDecimalLength > 0;
    }

    private int getFilledTextColorForSpan() {
        return mAutoFillNumbersTextColor == -1
                ? getCurrentHintTextColor()
                : mAutoFillNumbersTextColor;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mCurrencySymbol = mCurrencySymbol;
        savedState.mDecimalLength = mDecimalLength;
        savedState.mAutoFillNumbersTextColor = mAutoFillNumbersTextColor;
        savedState.mAutoFillNumbers = mAutoFillNumbers;
        savedState.mShowThousandsSeparator = mShowThousandsSeparator;
        savedState.mSelectionStart = start;
        savedState.mSelectionEnd = end;
        savedState.mRealText = getRealNumber(true);
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        mCurrencySymbol = savedState.mCurrencySymbol;
        mDecimalLength = savedState.mDecimalLength;
        mAutoFillNumbersTextColor = savedState.mAutoFillNumbersTextColor;
        mAutoFillNumbers = savedState.mAutoFillNumbers;
        mShowThousandsSeparator = savedState.mShowThousandsSeparator;
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

    private interface IPlaceholderSpan {}

    private static class DecimalPointSpan extends ForegroundColorSpan {
        private final int mColor;

        DecimalPointSpan(int color) {
            super(color);
            mColor = color;
        }
    }

    private static class CurrencySymbolSpan extends ForegroundColorSpan
            implements IPlaceholderSpan {
        CurrencySymbolSpan(int color) {
            super(color);
        }
    }

    private static class ThousandsSeparatorSpan implements IPlaceholderSpan {}

    private static class ZeroIntegerSpan extends ForegroundColorSpan implements IPlaceholderSpan {
        private final int mColor;

        ZeroIntegerSpan(int color) {
            super(color);
            mColor = color;
        }
    }

    private static class ZeroDecimalSpan extends ForegroundColorSpan implements IPlaceholderSpan {
        ZeroDecimalSpan(int color) {
            super(color);
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
        private String mRealText;
        private String mCurrencySymbol;
        private int mCurrencySymbolTextColor;
        private int mDecimalLength;
        private int mAutoFillNumbersTextColor;
        private boolean mAutoFillNumbers = true;
        private boolean mShowThousandsSeparator = true;
        private int mSelectionStart;
        private int mSelectionEnd;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mRealText = in.readString();
            mCurrencySymbol = in.readString();
            mCurrencySymbolTextColor = in.readInt();
            mDecimalLength = in.readInt();
            mAutoFillNumbersTextColor = in.readInt();
            mAutoFillNumbers = in.readInt() != 0;
            mShowThousandsSeparator = in.readInt() != 0;
            mSelectionStart = in.readInt();
            mSelectionEnd = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(mRealText);
            out.writeString(mCurrencySymbol);
            out.writeInt(mCurrencySymbolTextColor);
            out.writeInt(mDecimalLength);
            out.writeInt(mAutoFillNumbersTextColor);
            out.writeInt(mAutoFillNumbers ? 1 : 0);
            out.writeInt(mShowThousandsSeparator ? 1 : 0);
            out.writeInt(mSelectionStart);
            out.writeInt(mSelectionEnd);
        }
    }

    private class MaskNumberTextWatcher implements TextWatcher {
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
            if (!mIsFormatted && s instanceof Editable) {
                formatNumber((Editable) s);
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
