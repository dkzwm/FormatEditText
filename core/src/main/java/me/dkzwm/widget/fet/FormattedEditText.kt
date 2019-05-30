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
package me.dkzwm.widget.fet

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.annotation.IntDef
import java.util.*

/**
 * Created by dkzwm on 2017/2/22.
 *
 * @author dkzwm
 */
class FormattedEditText : EditText {
    private val mFormattedText = StringBuilder()
    private var mHolders: Array<Placeholder?>? = null
    private var mPlaceholder: String? = null
    private var mPlaceholders: String? = null
    private var mLastIndex: Int = 0
    @Mode
    private var mMode = MODE_SIMPLE
    private var mIsFormatted = false
    private var mWatchers: MutableList<TextWatcher>? = null
    private var mMark: String? = null
    private var mFilter: InputFilter? = null
    private var mClearDrawable: Drawable? = null
    private var mGravity = GRAVITY_CENTER
    private var mRealPaddingRight: Int = 0
    private var mTouchSlop: Int = 0
    private var mDrawablePadding = 0
    private val mDownPoint = FloatArray(2)
    private var mClearClickListener: OnClearClickListener? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        super.addTextChangedListener(FormattedTextWatcher())
        val viewConfiguration = ViewConfiguration.get(getContext())
        mTouchSlop = viewConfiguration.scaledTouchSlop
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                    attrs, R.styleable.FormattedEditText, defStyleAttr, 0)
            try {
                mMark = ta.getString(R.styleable.FormattedEditText_fet_mark)
                @Mode val mode = ta.getInt(R.styleable.FormattedEditText_fet_mode, MODE_SIMPLE)
                setMode(mode)
                val placeHolder = ta.getString(R.styleable.FormattedEditText_fet_placeholder)
                setPlaceholder(if (TextUtils.isEmpty(placeHolder)) DEFAULT_PLACE_HOLDER else placeHolder)
                val formatStyle = ta.getString(R.styleable.FormattedEditText_fet_formatStyle)
                setFormatStyle(formatStyle)
                mClearDrawable = ta.getDrawable(R.styleable.FormattedEditText_fet_clearDrawable)
                mGravity = ta.getInt(
                        R.styleable.FormattedEditText_fet_drawableGravity, GRAVITY_CENTER)
                mDrawablePadding = ta.getDimensionPixelSize(
                        R.styleable.FormattedEditText_fet_drawablePadding, 0)
            } finally {
                ta.recycle()
            }
        } else {
            setPlaceholder(DEFAULT_PLACE_HOLDER)
        }
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        val text = text
        val holders = mHolders
        if (holders != null && holders.isNotEmpty() && text.isNotEmpty())
            formatTextWhenAppend(text, 0, text.length)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                throw UnsupportedOperationException(
                        "We can not support this feature when the layout is right-to-left")
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetClearDrawableBound()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val clearDrawable = mClearDrawable
        if (clearDrawable != null) {
            val width = clearDrawable.intrinsicWidth + mDrawablePadding * 2
            val height = clearDrawable.intrinsicHeight + mDrawablePadding * 2
            val measuredWidth = measuredWidth
            val measuredHeight = measuredHeight
            var remeasuredWidth = measuredWidth
            var remeasuredHeight = measuredHeight
            if (measuredWidth < width) {
                val specMode = MeasureSpec.getMode(widthMeasureSpec)
                val specSize = MeasureSpec.getSize(widthMeasureSpec)
                if (specMode != MeasureSpec.EXACTLY) {
                    remeasuredWidth = Math.max(width, measuredWidth)
                    if (specMode == MeasureSpec.AT_MOST) {
                        remeasuredWidth = Math.min(remeasuredWidth, specSize)
                    }
                }
            }
            if (measuredHeight < height) {
                val specMode = MeasureSpec.getMode(heightMeasureSpec)
                val specSize = MeasureSpec.getSize(heightMeasureSpec)
                if (specMode != MeasureSpec.EXACTLY) {
                    remeasuredHeight = Math.max(height, measuredHeight)
                    if (specMode == MeasureSpec.AT_MOST) {
                        remeasuredHeight = Math.min(remeasuredHeight, specSize)
                    }
                }
            }
            if (remeasuredWidth != measuredWidth || remeasuredHeight != measuredHeight)
                setMeasuredDimension(remeasuredWidth, remeasuredHeight)
        }
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        if (mWatchers == null) mWatchers = ArrayList()
        mWatchers!!.add(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher) {
        mWatchers?.remove(watcher)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mRealPaddingRight = right
        var newRight = right
        val clearDrawable = mClearDrawable
        if (clearDrawable != null)
            newRight += clearDrawable.intrinsicWidth + mDrawablePadding * 2
        super.setPadding(left, top, newRight, bottom)
        resetClearDrawableBound()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        mRealPaddingRight = end
        var newEnd = end
        val clearDrawable = mClearDrawable
        if (clearDrawable != null)
            newEnd += clearDrawable.intrinsicWidth + mDrawablePadding * 2
        super.setPaddingRelative(start, top, newEnd, bottom)
        resetClearDrawableBound()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val clearDrawable = mClearDrawable
        if (clearDrawable != null && isFocused && length() > 0) {
            canvas.save()
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            clearDrawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun drawableStateChanged() {
        val clearDrawable = mClearDrawable
        if (clearDrawable != null) {
            val state = drawableState
            if (clearDrawable.isStateful && clearDrawable.setState(state)) {
                val dirty = clearDrawable.bounds
                val scrollX = scrollX
                val scrollY = scrollY
                invalidate(
                        dirty.left + scrollX,
                        dirty.top + scrollY,
                        dirty.right + scrollX,
                        dirty.bottom + scrollY)
            }
        }
        super.drawableStateChanged()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val clearDrawable = mClearDrawable
        if (clearDrawable != null) {
            val x = event.x
            val y = event.y
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mDownPoint[0] = x
                    mDownPoint[1] = y
                }
                MotionEvent.ACTION_UP -> {
                    val rect = clearDrawable.bounds
                    if (rect.top - mDrawablePadding <= y
                            && rect.bottom + mDrawablePadding >= y
                            && rect.left - mDrawablePadding <= x
                            && rect.right + mDrawablePadding >= x) {
                        if (Math.abs(mDownPoint[0] - x) <= mTouchSlop && Math.abs(mDownPoint[1] - y) <= mTouchSlop) {
                            val clearClickListener = mClearClickListener;
                            if (clearClickListener != null) {
                                if (!clearClickListener.onClearClick(this, clearDrawable))
                                    setText("")
                            } else
                                setText("")
                            super.onTouchEvent(event)
                            return true
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setClearDrawable(drawable: Drawable) {
        if (mClearDrawable !== drawable) {
            mClearDrawable = drawable
            requestLayout()
        }
    }

    fun setClearDrawablePadding(pad: Int) {
        if (mDrawablePadding != pad) {
            mDrawablePadding = pad
            if (mClearDrawable != null) requestLayout()
        }
    }

    fun setOnClearClickListener(clickListener: OnClearClickListener) {
        mClearClickListener = clickListener
    }

    fun setFormatStyle(style: String?) {
        if (style != null) {
            if (mMode == MODE_SIMPLE) {
                if (TextUtils.isDigitsOnly(style)) {
                    val holders = arrayOfNulls<Placeholder>(style.length)
                    var holder = Placeholder()
                    var index = Character.getNumericValue(style[0])
                    holder.index = index
                    holder.holder = mPlaceholder
                    holders[0] = holder
                    for (i in 1 until style.length) {
                        val number = Character.getNumericValue(style[i])
                        holder = Placeholder()
                        index = (holders[i - 1]?.index ?: 0) + 1 + number
                        holder.index = index
                        holder.holder = mPlaceholder
                        holders[i] = holder
                    }
                    mHolders = holders
                } else
                    throw IllegalArgumentException("Format style must be numeric")
            } else {
                val mark = mMark
                        ?: throw IllegalArgumentException("You must set mark before setting format style")
                if (!style.contains(mark))
                    throw IllegalArgumentException("Format style must be have Mark strings")
                val temp = arrayOfNulls<Placeholder>(style.length)
                val builder = StringBuilder()
                var realCount = 0
                var holder: Placeholder
                for (i in 0 until style.length) {
                    val sub = style.substring(i, i + 1)
                    if (sub != mMark) {
                        if (builder.indexOf(sub) < 0 && !TextUtils.isDigitsOnly(sub))
                            builder.append(sub)
                        holder = Placeholder()
                        holder.index = i
                        holder.holder = sub
                        temp[realCount] = holder
                        realCount++
                    }
                }
                val holders = arrayOfNulls<Placeholder>(realCount)
                mPlaceholders = builder.toString()
                System.arraycopy(temp, 0, holders, 0, realCount)
                clearArray(temp)
                mHolders = holders
            }
        } else {
            clearArray(mHolders)
            mHolders = null
        }
    }

    @CallSuper
    override fun setFilters(filters: Array<InputFilter>?) {
        if (filters == null) throw IllegalArgumentException("Filters can not be null")
        val replaceFilters = arrayOfNulls<InputFilter>(filters.size + 1)
        if (mFilter == null) mFilter = PlaceholderFilter()
        replaceFilters[0] = mFilter
        System.arraycopy(filters, 0, replaceFilters, 1, filters.size)
        super.setFilters(replaceFilters)
    }

    fun setMode(@Mode mode: Int) {
        if (mMode != mode) {
            val originalText = text.toString()
            mMode = mode
            if (mMode == MODE_COMPLEX && TextUtils.isEmpty(mMark)) mMark = DEFAULT_MARK
            if (!TextUtils.isEmpty(originalText)) setText(originalText)
        }
    }

    fun setMark(mark: String) {
        if (mark.length > 1)
            throw IllegalArgumentException("Mark only supports length one strings")
        mMark = mark
    }

    fun setPlaceholder(placeholder: String) {
        if (!TextUtils.equals(mPlaceholder, placeholder)) {
            if (placeholder.length > 1)
                throw IllegalArgumentException("Placeholder only supports length one strings")
            val placeholders = mHolders
            if (placeholders != null) {
                for (holder in placeholders) holder?.holder = placeholder
            }
            mPlaceholder = placeholder
            if (mMode == MODE_SIMPLE) mPlaceholders = placeholder
        }
    }

    fun getRealText(): String {
        val formattedText = mFormattedText.toString()
        var holderIndex = 0
        val holders = mHolders ?: return formattedText
        val realText = StringBuilder()
        for (i in 0 until formattedText.length) {
            if (holderIndex >= holders.size) {
                realText.append(formattedText.substring(i))
                return realText.toString()
            }
            if (holders[holderIndex]?.index ?: 0 == i) {
                holderIndex++
                continue
            }
            realText.append(formattedText.substring(i, i + 1))
        }
        return realText.toString()
    }

    private fun resetClearDrawableBound() {
        val clearDrawable = mClearDrawable
        if (clearDrawable != null) {
            val top = paddingTop + mDrawablePadding
            val bottom = paddingBottom + mDrawablePadding
            val width = clearDrawable.intrinsicWidth
            val height = clearDrawable.intrinsicHeight
            val newRight = getWidth() - mRealPaddingRight - mDrawablePadding
            val h = getHeight()
            when (mGravity) {
                GRAVITY_TOP -> clearDrawable.setBounds(newRight - width, top, newRight, top + height)
                GRAVITY_CENTER -> {
                    val newTop = top + (h - top - bottom - height) / 2
                    clearDrawable.setBounds(newRight - width, newTop, newRight, newTop + height)
                }
                else -> {
                    val newBottom = h - bottom
                    clearDrawable.setBounds(newRight - width, newBottom - height, newRight, newBottom)
                }
            }
        }
    }

    private fun sendBeforeTextChanged(s: CharSequence,
                                      start: Int,
                                      count: Int,
                                      after: Int) {
        val list = mWatchers
        if (list != null) {
            val size = list.size
            for (i in 0 until size) list[i].beforeTextChanged(s, start, count, after)
        }
    }

    private fun sendOnTextChanged(s: CharSequence,
                                  start: Int,
                                  before: Int,
                                  count: Int) {
        val list = mWatchers
        if (list != null) {
            val size = list.size
            for (i in 0 until size) list[i].onTextChanged(s, start, before, count)
        }
    }

    private fun sendAfterTextChanged(s: Editable) {
        val list = mWatchers
        if (list != null) {
            val size = list.size
            for (i in 0 until size) list[i].afterTextChanged(s)
        }
    }

    private fun clearArray(holders: Array<Placeholder?>?) {
        if (holders != null) for (i in holders.indices) holders[i] = null
    }

    private fun formatTextWhenDelete(s: CharSequence,
                                     start: Int,
                                     before: Int) {
        val lastText = mFormattedText.toString()
        val currentText = s.toString()
        val deletedLast = start >= currentText.length
        val holders = mHolders ?: return
        mFormattedText.delete(start, lastText.length)
        if (!deletedLast) formatTextNoCursor(currentText, start, 0)
        val tempText = mFormattedText.toString()
        mLastIndex = holders.size / 2
        var pos = start
        for (i in pos downTo 1) {
            val sub = tempText.substring(i - 1, i)
            val place = findPlaceholder(i - 1)
            if (sub == place) {
                if (deletedLast) mFormattedText.delete(i - 1, i)
                pos--
            } else break
        }
        mIsFormatted = true
        val text = mFormattedText.toString()
        val realCount = lastText.length - text.length
        sendBeforeTextChanged(lastText, pos, realCount, 0)
        if (!deletedLast || pos != start || realCount != before) {
            setText(text)
            if (length() >= pos)
                setSelection(pos)
            else
                setSelection(length())
        }
        mIsFormatted = false
        sendOnTextChanged(text, pos, realCount, 0)
        sendAfterTextChanged(getText())
    }

    private fun formatTextWhenAppend(s: CharSequence,
                                     start: Int,
                                     count: Int) {
        val lastText = mFormattedText.toString()
        val currentText = s.toString()
        val holders = mHolders ?: return
        val appendedLast = start > (holders[holders.size - 1]?.index ?: 0)
        val afterAppendStart: Int
        if (!appendedLast) {
            mFormattedText.delete(start, lastText.length)
            afterAppendStart = formatTextNoCursor(currentText, start, count)
        } else {
            afterAppendStart = start + count
            mFormattedText.insert(start, currentText.substring(start, afterAppendStart))
        }
        mIsFormatted = true
        val text = mFormattedText.toString()
        val realCount = text.length - lastText.length
        sendBeforeTextChanged(lastText, start, realCount, 0)
        if (!appendedLast || afterAppendStart != start + count || realCount != count) {
            setText(text)
            if (length() >= afterAppendStart)
                setSelection(afterAppendStart)
            else
                setSelection(length())
        }
        mIsFormatted = false
        sendOnTextChanged(text, start, realCount, 0)
        sendAfterTextChanged(getText())
    }

    private fun formatTextNoCursor(current: String,
                                   start: Int,
                                   count: Int): Int {
        val length = current.length
        var calcCount = count
        var position = start
        var afterAppendStart = start
        val holders = mHolders ?: return afterAppendStart
        val placeholders = mPlaceholders ?: return afterAppendStart
        val maxPos = holders[holders.size - 1]?.index ?: 0
        mLastIndex = holders.size / 2
        var i = start
        while (i < length) {
            if (mFormattedText.length > maxPos + 1) {
                afterAppendStart += if (calcCount < 0) 0 else calcCount
                if (count > 0 && length >= maxPos + count) {
                    val hasHolderEndIndex = maxPos + count + 1
                    val realEndIndex = if (length >= hasHolderEndIndex) hasHolderEndIndex else maxPos + count
                    val substring = current.substring(i, realEndIndex)
                    val len = substring.length
                    for (j in 0 until len) {
                        val sub = substring.substring(j, j + 1)
                        if (!placeholders.contains(sub)) mFormattedText.append(sub)
                    }
                    mFormattedText.append(current.substring(realEndIndex))
                    return afterAppendStart
                }
                mFormattedText.append(current.substring(i))
                return afterAppendStart
            }
            val sub = current.substring(i, i + 1)
            if (placeholders.contains(sub)) {
                if (calcCount >= 0) calcCount--
                i++
                continue
            }
            val place = findPlaceholder(position)
            if (place != null && (count > 0 || !TextUtils.equals(place, sub))) {
                mFormattedText.append(place)
                i--
                position++
                if (calcCount >= 0) afterAppendStart++
            } else {
                mFormattedText.append(sub)
                position++
                calcCount--
                if (calcCount >= 0) afterAppendStart++
            }
            i++
        }
        return afterAppendStart
    }

    private fun findPlaceholder(index: Int): String? {
        val holders = mHolders ?: return null
        val len = holders.size
        val last = mLastIndex
        val centerIndex = holders[last]?.index ?: 0
        when {
            centerIndex == index -> return holders[last]?.holder
            centerIndex < index -> for (i in last until len) {
                mLastIndex = i
                if (holders[i]?.index == index) {
                    return holders[i]?.holder
                } else if ((holders[i]?.index ?: 0) > index) return null
            }
            else -> for (i in last downTo 0) {
                mLastIndex = i
                if (holders[i]?.index == index)
                    return holders[i]?.holder
                else if ((holders[i]?.index ?: 0) < index) return null
            }
        }
        return null
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(MODE_SIMPLE, MODE_COMPLEX)
    internal annotation class Mode

    private class Placeholder {
        internal var index: Int = 0
        internal var holder: String? = null
    }

    interface OnClearClickListener {
        fun onClearClick(editText: FormattedEditText, drawable: Drawable): Boolean
    }

    private inner class PlaceholderFilter : InputFilter {
        private val mFilterBuilder = StringBuilder()

        override fun filter(source: CharSequence,
                            start: Int,
                            end: Int,
                            dest: Spanned,
                            dstart: Int,
                            dend: Int): CharSequence? {
            val placeholders = mPlaceholders
            if (placeholders == null || mIsFormatted || source.isEmpty()) return null
            mFilterBuilder.setLength(0)
            val len = source.length
            for (i in 0 until len) {
                val sub = source.subSequence(i, i + 1)
                if (!placeholders.contains(sub)) mFilterBuilder.append(sub)
            }
            return mFilterBuilder
        }
    }

    private inner class FormattedTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence,
                                       start: Int,
                                       count: Int,
                                       after: Int) {
            val holders = mHolders
            if (holders == null || holders.isEmpty())
                sendBeforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence,
                                   start: Int,
                                   before: Int,
                                   count: Int) {
            val holders = mHolders
            if (holders == null || holders.isEmpty()) {
                sendOnTextChanged(s, start, before, count)
                return
            }
            if (!mIsFormatted) {
                if (count == 0)
                    formatTextWhenDelete(s, start, before)
                else
                    formatTextWhenAppend(s, start, count)
            }
        }

        override fun afterTextChanged(s: Editable) {
            val holders = mHolders
            if (holders == null || holders.isEmpty()) sendAfterTextChanged(s)
            if (s.isEmpty() && mFormattedText.isNotEmpty()) mFormattedText.setLength(0)
        }
    }

    companion object {
        const val MODE_SIMPLE = 0
        const val MODE_COMPLEX = 1
        const val GRAVITY_TOP = 0
        const val GRAVITY_CENTER = 1
        const val GRAVITY_BOTTOM = 2
        private const val DEFAULT_PLACE_HOLDER = " "
        private const val DEFAULT_MARK = "*"
    }
}
