package me.dkzwm.formatedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by dkzwm on 2017/2/22.
 *
 * @author dkzwm
 */
public class FormattedEditText extends EditText {
    private static final char DEFAULT_PLACE_HOLDER = ' ';
    private char mPlaceHolder;
    private int[] mPlaceHoldersPosition;
    private ArrayList<TextWatcher> mWatchers;
    private boolean mIsFormatted = false;

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
                if (mIsFormatted || mPlaceHoldersPosition == null) {
                    sendBeforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPlaceHoldersPosition == null) {
                    sendOnTextChanged(s, start, before, count);
                    return;
                }
                if (mIsFormatted) {
                    mIsFormatted = false;
                    sendOnTextChanged(s, start, before, count);
                } else {
                    formattedText(s, start, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsFormatted || mPlaceHoldersPosition == null)
                    sendAfterTextChanged(s);
            }
        };
        super.addTextChangedListener(textWatcher);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FormattedEditText,
                    defStyleAttr, 0);
            String formatStyle = ta.getString(R.styleable.FormattedEditText_formatStyle);
            if (formatStyle != null) {
                boolean isNumeric = isNumeric(formatStyle);
                if (isNumeric) {
                    mPlaceHoldersPosition = new int[formatStyle.length()];
                    mPlaceHoldersPosition[0] = Character.getNumericValue(formatStyle.charAt(0));
                    for (int i = 1; i < formatStyle.length(); i++) {
                        int number = Character.getNumericValue(formatStyle.charAt(i));
                        mPlaceHoldersPosition[i] = mPlaceHoldersPosition[i - 1] + 1 + number;
                    }
                } else
                    throw new IllegalArgumentException("format style must be numeric");
            }
            String placeHolder = ta.getString(R.styleable.FormattedEditText_placeHolder);
            if (placeHolder != null) {
                mPlaceHolder = placeHolder.charAt(0);
            } else {
                mPlaceHolder = DEFAULT_PLACE_HOLDER;
            }
            ta.recycle();
        } else {
            mPlaceHolder = DEFAULT_PLACE_HOLDER;
        }
        if (getText().length() > 0) {
            formattedText(getText().toString(), 0, getText().length());
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
            int i = mWatchers.indexOf(watcher);
            if (i >= 0) {
                mWatchers.remove(i);
            }
        }
    }

    private void sendBeforeTextChanged(CharSequence text, int start, int before, int after) {
        if (mWatchers != null) {
            final ArrayList<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).beforeTextChanged(text, start, before, after);
            }
        }
    }

    private void sendOnTextChanged(CharSequence s, int start, int before, int count) {
        if (mWatchers != null) {
            final ArrayList<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).onTextChanged(s, start, before, count);
            }
        }
    }

    private void sendAfterTextChanged(Editable s) {
        if (mWatchers != null) {
            final ArrayList<TextWatcher> list = mWatchers;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).afterTextChanged(s);
            }
        }
    }

    private void formattedText(CharSequence s, int start, int count) {
        if (s.length() == 0)
            return;
        StringBuilder sb = new StringBuilder();
        int nowPosition = 0;
        if (count > 0) {
            int realCount = 0;
            for (int i = 0; i < count; i++) {
                if (s.charAt(start + i) != mPlaceHolder)
                    realCount++;
            }
            count = realCount;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == mPlaceHolder)
                continue;
            if (nowPosition >= mPlaceHoldersPosition.length) {
                sb.append(s.charAt(i));
            } else {
                if (sb.length() < mPlaceHoldersPosition[nowPosition]) {
                    sb.append(s.charAt(i));
                } else if (sb.length() == mPlaceHoldersPosition[nowPosition]) {
                    sb.append(mPlaceHolder);
                    sb.append(s.charAt(i));
                } else {
                    sb.append(s.charAt(i));
                    nowPosition++;
                }
            }
        }
        int selection = getSelectionStart();
        mIsFormatted = true;
        setText(sb.toString());
        if (start == selection && selection <= sb.length()) {
            for (int mP : mPlaceHoldersPosition) {
                if (selection == mP + 1) {
                    setSelection(mP);
                    return;
                }
            }
            setSelection(selection);
        } else if (selection <= sb.length() && selection > start
                && count > 0 && start + count <= sb.length()) {
            int fistPos = mPlaceHoldersPosition.length;
            for (int i = 0; i < mPlaceHoldersPosition.length; i++) {
                if (start <= mPlaceHoldersPosition[i]) {
                    fistPos = i;
                    break;
                }
            }
            int addPlaceHolderCount = 0;
            if (fistPos != mPlaceHoldersPosition.length) {
                int used = 0;
                for (int i = fistPos; i < mPlaceHoldersPosition.length; i++) {
                    if (used > count)
                        break;
                    for (int j = used; j < count; j++) {
                        if (start + j + addPlaceHolderCount
                                == mPlaceHoldersPosition[i]) {
                            addPlaceHolderCount++;
                            used = j;
                            break;
                        }
                    }
                }
            }
            setSelection(start + count + addPlaceHolderCount);
        } else {
            setSelection(sb.length());
        }
    }

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


}
