package me.dkzwm.widget.fet;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkzwm on 2017/2/22.
 *
 * @author dkzwm
 */
public class FormattedEditText extends EditText {
    private static final char DEFAULT_PLACE_HOLDER = ' ';
    private char mPlaceHolder;
    private int[] mPlaceHoldersPos;
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
            String formatStyle = ta.getString(R.styleable.FormattedEditText_formatStyle);
            setFormatStyle(formatStyle);
            String placeHolder = ta.getString(R.styleable.FormattedEditText_placeHolder);
            if (placeHolder != null) {
                setPlaceHolder(placeHolder.charAt(0));
            } else {
                mPlaceHolder = DEFAULT_PLACE_HOLDER;
            }
            ta.recycle();
        } else {
            mPlaceHolder = DEFAULT_PLACE_HOLDER;
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
        }
    }

    public void setPlaceHolder(char holder) {
        mPlaceHolder = holder;
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
        final int lastLength = mTextBuilder.length();
        final CharSequence originText = mTextBuilder.toString();
        mTextBuilder.setLength(0);
        int nowPosition = 0;
        int realCount = 0;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                for (int j = nowPosition; j < mPlaceHoldersPos.length; j++) {
                    if (mPlaceHoldersPos[j] > start + realCount)
                        break;
                    if (mPlaceHoldersPos[j] == start + realCount) {
                        realCount++;
                        nowPosition++;
                    }
                }
                if (s.charAt(start + i) != mPlaceHolder)
                    realCount++;
            }
        }
        nowPosition = 0;
        final int originLength = s.length();
        for (int i = 0; i < originLength; i++) {
            if (s.charAt(i) == mPlaceHolder) {
                continue;
            }
            if (nowPosition >= mPlaceHoldersPos.length) {
                mTextBuilder.append(s.charAt(i));
            } else {
                if (mTextBuilder.length() < mPlaceHoldersPos[nowPosition]) {
                    mTextBuilder.append(s.charAt(i));
                } else if (mTextBuilder.length() == mPlaceHoldersPos[nowPosition]) {
                    mTextBuilder.append(mPlaceHolder);
                    mTextBuilder.append(s.charAt(i));
                    nowPosition++;
                } else {
                    mTextBuilder.append(s.charAt(i));
                    nowPosition++;
                }
            }
        }
        mHasBeenFormatted = true;
        final CharSequence text = mTextBuilder.toString();
        if (before > 0) {
            sendBeforeTextChanged(originText, start, before, 0);
            setText(text);
            mHasBeenFormatted = false;
            if (start > text.length()) {
                setSelection(text.length());
                sendOnTextChanged(text, start, lastLength - text.length(), 0);
            } else {
                setSelection(start);
                sendOnTextChanged(text, start, before, 0);
            }
            sendAfterTextChanged(getText());
        } else {
            sendBeforeTextChanged(originText, start, 0, realCount);
            setText(text);
            mHasBeenFormatted = false;
            setSelection(start + realCount);
            sendOnTextChanged(text, start, before, realCount);
            sendAfterTextChanged(getText());
        }
    }
}
