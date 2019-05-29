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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
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
    public static final int GRAVITY_TOP = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_BOTTOM = 2;
    private static final String DEFAULT_PLACE_HOLDER = " ";
    private static final String DEFAULT_MARK = "*";
    protected int mTouchSlop;
    private StringBuilder mFormattedText = new StringBuilder();
    private Placeholder[] mHolders;
    private String mPlaceholder;
    private String mPlaceholders;
    private int mLastIndex;
    @Mode private int mMode = MODE_SIMPLE;
    private boolean mIsFormatted = false;
    private List<TextWatcher> mWatchers;
    private String mMark;
    private InputFilter mFilter;
    private Drawable mClearDrawable;
    private int mGravity = GRAVITY_CENTER;
    private int mRealPaddingRight;
    private int mDrawablePadding = 0;
    private float[] mDownPoint = new float[2];
    private OnClearClickListener mClearClickListener;

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
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        if (attrs != null) {
            TypedArray ta =
                    context.obtainStyledAttributes(
                            attrs, R.styleable.FormattedEditText, defStyleAttr, 0);
            try {
                mMark = ta.getString(R.styleable.FormattedEditText_fet_mark);
                @Mode int mode = ta.getInt(R.styleable.FormattedEditText_fet_mode, MODE_SIMPLE);
                setMode(mode);
                String placeHolder = ta.getString(R.styleable.FormattedEditText_fet_placeholder);
                setPlaceholder(
                        (placeHolder == null || placeHolder.length() == 0)
                                ? DEFAULT_PLACE_HOLDER
                                : placeHolder);
                String formatStyle = ta.getString(R.styleable.FormattedEditText_fet_formatStyle);
                setFormatStyle(formatStyle);
                mClearDrawable = ta.getDrawable(R.styleable.FormattedEditText_fet_clearDrawable);
                mGravity =
                        ta.getInt(
                                R.styleable.FormattedEditText_fet_drawableGravity, GRAVITY_CENTER);
                mDrawablePadding =
                        ta.getDimensionPixelSize(
                                R.styleable.FormattedEditText_fet_drawablePadding, 0);
            } finally {
                ta.recycle();
            }
        } else {
            setPlaceholder(DEFAULT_PLACE_HOLDER);
        }
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        final CharSequence text = getText();
        if (mHolders != null && mHolders.length > 0 && text.length() > 0)
            formatTextWhenAppend(text, 0, text.length());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                throw new UnsupportedOperationException(
                        "We can not support this feature when the layout is right-to-left");
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mClearDrawable != null) {
            final int top = getPaddingTop() + mDrawablePadding;
            final int bottom = getPaddingBottom() + mDrawablePadding;
            int width = mClearDrawable.getIntrinsicWidth();
            int height = mClearDrawable.getIntrinsicHeight();
            final int newRight = w - mRealPaddingRight - mDrawablePadding;
            switch (mGravity) {
                case GRAVITY_TOP:
                    mClearDrawable.setBounds(newRight - width, top, newRight, top + height);
                    break;
                case GRAVITY_CENTER:
                    int newTop = top + (h - top - bottom - height) / 2;
                    mClearDrawable.setBounds(newRight - width, newTop, newRight, newTop + height);
                    break;
                case GRAVITY_BOTTOM:
                default:
                    int newBottom = h - bottom;
                    mClearDrawable.setBounds(
                            newRight - width, newBottom - height, newRight, newBottom);
                    break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mClearDrawable != null) {
            int width = mClearDrawable.getIntrinsicWidth() + mDrawablePadding * 2;
            int height = mClearDrawable.getIntrinsicHeight() + mDrawablePadding * 2;
            int measuredWidth = getMeasuredWidth();
            int measuredHeight = getMeasuredHeight();
            int remeasuredWidth = measuredWidth, remeasuredHeight = measuredHeight;
            if (measuredWidth < width) {
                int specMode = MeasureSpec.getMode(widthMeasureSpec);
                int specSize = MeasureSpec.getSize(widthMeasureSpec);
                if (specMode != MeasureSpec.EXACTLY) {
                    remeasuredWidth = Math.max(width, measuredWidth);
                    if (specMode == MeasureSpec.AT_MOST) {
                        remeasuredWidth = Math.min(remeasuredWidth, specSize);
                    }
                }
            }
            if (measuredHeight < height) {
                int specMode = MeasureSpec.getMode(heightMeasureSpec);
                int specSize = MeasureSpec.getSize(heightMeasureSpec);
                if (specMode != MeasureSpec.EXACTLY) {
                    remeasuredHeight = Math.max(height, measuredHeight);
                    if (specMode == MeasureSpec.AT_MOST) {
                        remeasuredHeight = Math.min(remeasuredHeight, specSize);
                    }
                }
            }
            if (remeasuredWidth != measuredWidth || remeasuredHeight != measuredHeight) {
                setMeasuredDimension(remeasuredWidth, remeasuredHeight);
            }
        }
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

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mRealPaddingRight = right;
        if (mClearDrawable != null)
            right += mClearDrawable.getIntrinsicWidth() + mDrawablePadding * 2;
        super.setPadding(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClearDrawable != null && isFocused() && length() > 0) {
            canvas.save();
            canvas.translate(getScrollX(), getScrollY());
            mClearDrawable.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void drawableStateChanged() {
        if (mClearDrawable != null) {
            final int[] state = getDrawableState();
            if (mClearDrawable.isStateful() && mClearDrawable.setState(state)) {
                final Rect dirty = mClearDrawable.getBounds();
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();
                invalidate(
                        dirty.left + scrollX,
                        dirty.top + scrollY,
                        dirty.right + scrollX,
                        dirty.bottom + scrollY);
            }
        }
        super.drawableStateChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClearDrawable != null) {
            final float x = event.getX();
            final float y = event.getY();
            final int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownPoint[0] = x;
                    mDownPoint[1] = y;
                    break;
                case MotionEvent.ACTION_UP:
                    final Rect rect = mClearDrawable.getBounds();
                    if (rect.top - mDrawablePadding <= y
                            && rect.bottom + mDrawablePadding >= y
                            && rect.left - mDrawablePadding <= x
                            && rect.right + mDrawablePadding >= x) {
                        if (Math.abs(mDownPoint[0] - x) <= mTouchSlop
                                && Math.abs(mDownPoint[1] - y) <= mTouchSlop) {
                            if (mClearClickListener != null) {
                                if (!mClearClickListener.onClearClick(this, mClearDrawable))
                                    setText("");
                            } else setText("");
                            super.onTouchEvent(event);
                            return true;
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setClearDrawable(Drawable drawable) {
        if (mClearDrawable != drawable) {
            mClearDrawable = drawable;
            requestLayout();
        }
    }

    public void setClearDrawablePadding(int pad) {
        if (mDrawablePadding != pad) {
            mDrawablePadding = pad;
            if (mClearDrawable != null) requestLayout();
        }
    }

    public void setOnClearClickListener(OnClearClickListener clickListener) {
        mClearClickListener = clickListener;
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
                        if (builder.indexOf(sub) < 0 && !TextUtils.isDigitsOnly(sub))
                            builder.append(sub);
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
        if (filters == null) throw new IllegalArgumentException("filters can not be null");
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

    public String getRealText() {
        final String formattedText = mFormattedText.toString();
        final StringBuilder realText = new StringBuilder();
        int holderIndex = 0;
        for (int i = 0; i < formattedText.length(); i++) {
            if (holderIndex >= mHolders.length) {
                realText.append(formattedText.substring(i));
                return realText.toString();
            }
            if (mHolders[holderIndex].index == i) {
                holderIndex++;
                continue;
            }
            realText.append(formattedText.substring(i, i + 1));
        }
        return realText.toString();
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
        final String lastText = mFormattedText.toString();
        final String currentText = s.toString();
        final boolean deletedLast = start >= currentText.length();
        mFormattedText.delete(start, lastText.length());
        if (!deletedLast) formatTextNoCursor(currentText, start, 0);
        final String tempText = mFormattedText.toString();
        mLastIndex = mHolders.length / 2;
        int pos = start;
        for (int i = pos; i > 0; i--) {
            final String sub = tempText.substring(i - 1, i);
            final String place = findPlaceholder(i - 1);
            if (sub.equals(place)) {
                if (deletedLast) mFormattedText.delete(i - 1, i);
                pos--;
            } else break;
        }
        mIsFormatted = true;
        final String text = mFormattedText.toString();
        final int realCount = lastText.length() - text.length();
        sendBeforeTextChanged(lastText, pos, realCount, 0);
        if (!deletedLast || pos != start || realCount != before) {
            setText(text);
            if (length() >= pos) setSelection(pos);
            else setSelection(length());
        }
        mIsFormatted = false;
        sendOnTextChanged(text, pos, realCount, 0);
        sendAfterTextChanged(getText());
    }

    private void formatTextWhenAppend(final CharSequence s, int start, int count) {
        final String lastText = mFormattedText.toString();
        final String currentText = s.toString();
        boolean appendedLast = start > mHolders[mHolders.length - 1].index;
        int afterAppendStart;
        if (!appendedLast) {
            mFormattedText.delete(start, lastText.length());
            afterAppendStart = formatTextNoCursor(currentText, start, count);
        } else {
            afterAppendStart = start + count;
            mFormattedText.insert(start, currentText.substring(start, afterAppendStart));
        }
        mIsFormatted = true;
        final String text = mFormattedText.toString();
        final int realCount = text.length() - lastText.length();
        sendBeforeTextChanged(lastText, start, realCount, 0);
        if (!appendedLast || afterAppendStart != start + count || realCount != count) {
            setText(text);
            if (length() >= afterAppendStart) setSelection(afterAppendStart);
            else setSelection(length());
        }
        mIsFormatted = false;
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
            if (mFormattedText.length() > maxPos + 1) {
                afterAppendStart += calcCount < 0 ? 0 : calcCount;
                if (count > 0 && length >= maxPos + count) {
                    final int hasHolderEndIndex = maxPos + count + 1;
                    final int realEndIndex =
                            length >= hasHolderEndIndex ? hasHolderEndIndex : maxPos + count;
                    String substring = current.substring(i, realEndIndex);
                    final int len = substring.length();
                    for (int j = 0; j < len; j++) {
                        final String sub = substring.substring(j, j + 1);
                        if (!mPlaceholders.contains(sub)) mFormattedText.append(sub);
                    }
                    mFormattedText.append(current.substring(realEndIndex));
                    return afterAppendStart;
                }
                mFormattedText.append(current.substring(i));
                return afterAppendStart;
            }
            final String sub = current.substring(i, i + 1);
            if (mPlaceholders.contains(sub)) {
                if (calcCount >= 0) calcCount--;
                continue;
            }
            final String place = findPlaceholder(position);
            if (place != null && (count > 0 || !TextUtils.equals(place, sub))) {
                mFormattedText.append(place);
                i--;
                position++;
                if (calcCount >= 0) afterAppendStart++;
            } else {
                mFormattedText.append(sub);
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
    @interface Mode {}

    public interface OnClearClickListener {
        boolean onClearClick(FormattedEditText editText, Drawable drawable);
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
            if (s.length() == 0 && mFormattedText.length() != 0) mFormattedText.setLength(0);
        }
    }
}
