package me.dkzwm.widget.fet;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.Editable;
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
    private int[] mPlaceHoldersPos;
    private String[] mPlaceHolders;
    private String mMark;
    @Mode
    private int mMode = MODE_SIMPLE;
    private List<TextWatcher> mWatchers;
    private boolean mHasBeenFormatted = false;
    private StringBuilder mTextBuilder = new StringBuilder();

    public FormattedEditText(Context context) {
        this(context, null, android.R.attr.editTextStyle);
    }

    public FormattedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public FormattedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!mHasBeenFormatted && (mPlaceHoldersPos == null
                        || (s.length() > 0 && s.length() - count == 0))) {
                    sendBeforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPlaceHoldersPos == null || s.length() == 0) {
                    sendOnTextChanged(s, start, before, count);
                    return;
                }
                if (!mHasBeenFormatted) formatText(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPlaceHoldersPos == null || s.length() == 0)
                    sendAfterTextChanged(s);
                if (s.length() == 0 && mTextBuilder.length() != 0)
                    mTextBuilder.setLength(0);
            }
        };
        super.addTextChangedListener(textWatcher);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FormattedEditText,
                    defStyleAttr, 0);
            mMark = ta.getString(R.styleable.FormattedEditText_fet_mark);
            @Mode
            int mode = ta.getInt(R.styleable.FormattedEditText_fet_mode, MODE_SIMPLE);
            setMode(mode);
            String formatStyle = ta.getString(R.styleable.FormattedEditText_fet_formatStyle);
            setFormatStyle(formatStyle);
            if (mMode == MODE_SIMPLE) {
                String placeHolder = ta.getString(R.styleable.FormattedEditText_fet_placeholder);
                if (placeHolder != null) {
                    if (placeHolder.length() > 1)
                        throw new IllegalArgumentException("PlaceHolder only can support one char");
                    setPlaceholder(placeHolder);
                } else {
                    mPlaceHolders = new String[1];
                    mPlaceHolders[0] = DEFAULT_PLACE_HOLDER;
                }
            }
            ta.recycle();
        }
        if (getText().length() > 0) {
            formatText(getText().toString(), 0, 0, getText().length());
        }
    }

    private static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
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
            int i = mWatchers.indexOf(watcher);
            if (i >= 0) {
                mWatchers.remove(i);
            }
        }
    }

    public void setFormatStyle(String style) {
        if (style != null) {
            if (mMode == MODE_SIMPLE) {
                boolean isNumeric = isNumeric(style);
                if (isNumeric) {
                    mPlaceHoldersPos = new int[style.length()];
                    mPlaceHoldersPos[0] = Character.getNumericValue(style.charAt(0));
                    for (int i = 1; i < style.length(); i++) {
                        int number = Character.getNumericValue(style.charAt(i));
                        mPlaceHoldersPos[i] = mPlaceHoldersPos[i - 1] + 1 + number;
                    }
                } else
                    throw new IllegalArgumentException("Format style must be numeric");
            } else {
                if (!style.contains(mMark))
                    throw new IllegalArgumentException("Format style must be have Mark strings");
                final String[] tempHolders = new String[style.length()];
                final int[] tempHoldersPos = new int[style.length()];
                int realCount = 0;
                for (int i = 0; i < style.length(); i++) {
                    final String sub = style.substring(i, i + 1);
                    if (!sub.equals(mMark)) {
                        tempHolders[realCount] = sub;
                        tempHoldersPos[realCount] = i;
                        realCount++;
                    }
                }
                mPlaceHoldersPos = new int[realCount];
                System.arraycopy(tempHoldersPos, 0, mPlaceHoldersPos, 0, realCount);
                mPlaceHolders = new String[realCount];
                System.arraycopy(tempHolders, 0, mPlaceHolders, 0, realCount);
            }
        } else {
            mPlaceHolders = null;
            mPlaceHoldersPos = null;
        }
    }

    public void setMode(@Mode int mode) {
        if (mMode != mode) {
            String originalText = getText().toString();
            mMode = mode;
            if (mMode == MODE_COMPLEX && TextUtils.isEmpty(mMark))
                mMark = DEFAULT_MARK;
            if (!TextUtils.isEmpty(originalText)) {
                setText("");
                setText(originalText);
            }
        }
    }

    public void setMark(@NonNull String mark) {
        if (mark.length() > 1)
            throw new IllegalArgumentException("Mark only supports length one strings");
        mMark = mark;
    }

    public void setPlaceholder(@NonNull String holder) {
        if (mMode == MODE_SIMPLE) {
            if (holder.length() > 1)
                throw new IllegalArgumentException("Placeholder only supports length one strings");
            mPlaceHolders = new String[1];
            mPlaceHolders[0] = holder;
        } else {
            throw new IllegalArgumentException("Placeholder only supports mode is MODE_SIMPLE");
        }
    }

    private void sendBeforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mWatchers != null) {
            final List<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).beforeTextChanged(s, start, count, after);
            }
        }
    }

    private void sendOnTextChanged(CharSequence s, int start, int before, int count) {
        if (mWatchers != null) {
            final List<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).onTextChanged(s, start, before, count);
            }
        }
    }

    private void sendAfterTextChanged(Editable s) {
        if (mWatchers != null) {
            final List<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).afterTextChanged(s);
            }
        }
    }

    private void formatText(final CharSequence s, int start, int before, int count) {
        if (before > 0) {
            formatTextWhenDelete(s, start);
        } else if (count > 0) {
            formatTextWhenAppend(s, start, count);
        }
    }


    private void formatTextWhenDelete(final CharSequence s, int start) {
        final String lastText = mTextBuilder.toString();
        int startPos = mPlaceHoldersPos.length - 1;
        for (int i = 0; i < mPlaceHoldersPos.length; i++) {
            if (start <= mPlaceHoldersPos[i]) {
                startPos = i - 1;
                break;
            }
        }
        if (startPos < 0) startPos = 0;
        if (start < s.length()) {
            mTextBuilder.delete(start, mTextBuilder.length());
            formatTextNoCursor(s.toString(), start, 0, startPos);
        } else {
            mTextBuilder.delete(start, mTextBuilder.length());
        }
        final int length = mTextBuilder.length();
        for (int i = length; i > 0; i--) {
            final String sub = mTextBuilder.substring(i - 1, i);
            if (((mMode == MODE_COMPLEX && sub.equals(mPlaceHolders[startPos]) ||
                    (mMode == MODE_SIMPLE && sub.equals(mPlaceHolders[0]))))
                    && i - 1 == mPlaceHoldersPos[startPos]) {
                mTextBuilder.delete(i - 1, i);
                startPos--;
                continue;
            }
            break;
        }
        mHasBeenFormatted = true;
        final CharSequence text = mTextBuilder.toString();
        final int realCount = lastText.length() - text.length();
        sendBeforeTextChanged(lastText, start, realCount, 0);
        setText(text);
        mHasBeenFormatted = false;
        if (start > text.length())
            setSelection(text.length());
        else
            setSelection(start);
        sendOnTextChanged(text, start, realCount, 0);
        sendAfterTextChanged(getText());
    }

    private void formatTextWhenAppend(final CharSequence s, int start, int count) {
        final String lastText = mTextBuilder.toString();
        int startPos = 0;
        for (int i = 0; i < mPlaceHoldersPos.length; i++) {
            if (start <= mPlaceHoldersPos[i]) {
                startPos = i;
                break;
            }
        }
        int tailLength = mTextBuilder.length() - start;
        if (tailLength > 0) {
            mTextBuilder.delete(start, mTextBuilder.length());
        }
        int realPos = formatTextNoCursor(s.toString(), start, count, startPos);
        mHasBeenFormatted = true;
        final CharSequence text = mTextBuilder.toString();
        sendBeforeTextChanged(lastText, start, 0, realPos - start);
        setText(text);
        mHasBeenFormatted = false;
        setSelection(realPos);
        sendOnTextChanged(text, start, 0, realPos - start);
        sendAfterTextChanged(getText());
    }

    private int formatTextNoCursor(final String text, int start, int count, int position) {
        final int length = text.length();
        int afterAppendStart = 0;
        int lastI = -1;
        for (int i = start; i < length; i++) {
            boolean found = false;
            final String sub = text.substring(i, i + 1);
            for (String placeholder : mPlaceHolders) {
                if (sub.equals(placeholder)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (count > 0 && i >= start && count > 0 && lastI != i) {
                    count--;
                    if (count == 0) {
                        afterAppendStart = mTextBuilder.length();
                    }
                }
                lastI = i;
                continue;
            }
            if (position >= mPlaceHoldersPos.length || mTextBuilder.length() !=
                    mPlaceHoldersPos[position]) {
                mTextBuilder.append(sub);
                if (count > 0 && i >= start && count > 0 && lastI != i) {
                    count--;
                    if (count == 0) {
                        afterAppendStart = mTextBuilder.length();
                    }
                }
                if (position < mPlaceHoldersPos.length && mTextBuilder.length() >
                        mPlaceHoldersPos[position])
                    position++;
            } else {
                if (mMode == MODE_SIMPLE) {
                    mTextBuilder.append(mPlaceHolders[0]);
                } else {
                    mTextBuilder.append(mPlaceHolders[position]);
                }
                position++;
                i--;
            }
            lastI = i;
        }
        return afterAppendStart;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_SIMPLE, MODE_COMPLEX})
    @interface Mode {
    }
}
