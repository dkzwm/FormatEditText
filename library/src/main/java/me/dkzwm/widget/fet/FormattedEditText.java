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
    private int[] mPlaceHoldersPosition;
    private List<TextWatcher> mWatchers;
    private boolean mIsFormatted = false;
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
                if (mIsFormatted || mPlaceHoldersPosition == null || s.length() == 0) {
                    sendBeforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPlaceHoldersPosition == null) {
                    sendOnTextChanged(s, start, before, count);
                    return;
                }
                if (mIsFormatted || s.length() == 0) {
                    mIsFormatted = false;
                    sendOnTextChanged(s, start, before, count);
                } else {
                    formattedText(s, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsFormatted || mPlaceHoldersPosition == null || s.length() == 0)
                    sendAfterTextChanged(s);
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
            formattedText(getText().toString(), 0, 0, getText().length());
            setSelection(getText().length());
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
                mPlaceHoldersPosition = new int[style.length()];
                mPlaceHoldersPosition[0] = Character.getNumericValue(style.charAt(0));
                for (int i = 1; i < style.length(); i++) {
                    int number = Character.getNumericValue(style.charAt(i));
                    mPlaceHoldersPosition[i] = mPlaceHoldersPosition[i - 1] + 1 + number;
                }
            } else
                throw new IllegalArgumentException("Format style must be numeric");
        }
    }

    public void setPlaceHolder(char holder) {
        mPlaceHolder = holder;
    }

    private void sendBeforeTextChanged(CharSequence text, int start, int before, int after) {
        if (mWatchers != null) {
            final List<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).beforeTextChanged(text, start, before, after);
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

    private void formattedText(final CharSequence s, int start, int before, int count) {
        if (s.length() == 0)
            return;
        mTextBuilder.setLength(0);
        int nowPosition = 0;
        if (count > 0) {
            int realCount = 0;
            for (int i = 0; i < count; i++) {
                if (s.charAt(start + i) != mPlaceHolder)
                    realCount++;
            }
            count = realCount;
        }
        final int originLength = s.length();
        int appendHolderCount = 0;
        for (int i = 0; i < originLength; i++) {
            if (s.charAt(i) == mPlaceHolder)
                continue;
            if (nowPosition >= mPlaceHoldersPosition.length) {
                mTextBuilder.append(s.charAt(i));
            } else {
                if (mTextBuilder.length() < mPlaceHoldersPosition[nowPosition]) {
                    mTextBuilder.append(s.charAt(i));
                } else if (mTextBuilder.length() == mPlaceHoldersPosition[nowPosition]) {
                    mTextBuilder.append(mPlaceHolder);
                    if (i >= start) {
                        appendHolderCount++;
                    }
                    mTextBuilder.append(s.charAt(i));
                } else {
                    mTextBuilder.append(s.charAt(i));
                    nowPosition++;
                }
            }
        }
        mIsFormatted = true;
        if (before > 0) {
            setText(mTextBuilder.toString());
            if (start > mTextBuilder.length())
                setSelection(mTextBuilder.length());
            else
                setSelection(start);
        } else {
            setText(mTextBuilder.toString());
            setSelection(start + count + appendHolderCount);
        }
    }
}
