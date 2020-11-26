package me.dkzwm.widget.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import me.dkzwm.widget.fet.FormattedEditText;
import me.dkzwm.widget.fet.MaskNumberEditText;

/**
 * Created by dkzwm on 2017/2/23.
 *
 * @author dkzwm
 */
public class MainActivity extends AppCompatActivity {
    private TextView mTextViewLogs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewLogs = findViewById(R.id.textView_logs);
        findViewById(R.id.button_clear)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTextViewLogs.setText("");
                            }
                        });
        final TextView textViewSimple = findViewById(R.id.textView_simple);
        final FormattedEditText editTextSimple = findViewById(R.id.formattedEditText_simple);
        editTextSimple.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        appendBeforeTextChangedLog(s, start, count, after);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        appendOnTextChangedLog(s, start, before, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        appendAfterTextChangedLog(s);
                        textViewSimple.setText(
                                String.format(
                                        getString(R.string.mode_simple_desc),
                                        editTextSimple.getRealText()));
                    }
                });
        textViewSimple.setText(
                String.format(getString(R.string.mode_simple_desc), editTextSimple.getRealText()));
        final TextView textViewComplex = findViewById(R.id.textView_complex);
        final FormattedEditText editTextComplex = findViewById(R.id.formattedEditText_complex);
        editTextComplex.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        appendBeforeTextChangedLog(s, start, count, after);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        appendOnTextChangedLog(s, start, before, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        appendAfterTextChangedLog(s);
                        textViewComplex.setText(
                                String.format(
                                        getString(R.string.mode_complex_desc),
                                        editTextComplex.getRealText()));
                    }
                });
        textViewComplex.setText(
                String.format(
                        getString(R.string.mode_complex_desc), editTextComplex.getRealText()));
        editTextSimple.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            checkSimpleValid(editTextSimple);
                        } else {
                            editTextSimple.setCompoundDrawablesWithIntrinsicBounds(
                                    null, null, null, null);
                        }
                    }
                });
        final TextView textViewMask = findViewById(R.id.textView_mask);
        final FormattedEditText editTextMask = findViewById(R.id.formattedEditText_mask);
        editTextMask.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        appendBeforeTextChangedLog(s, start, count, after);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        appendOnTextChangedLog(s, start, before, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        appendAfterTextChangedLog(s);
                        textViewMask.setText(
                                String.format(
                                        getString(R.string.mode_mask_desc),
                                        editTextMask.getRealText()));
                    }
                });
        textViewMask.setText(
                String.format(getString(R.string.mode_mask_desc), editTextMask.getRealText()));
        final TextView textViewHint = findViewById(R.id.textView_hint);
        final FormattedEditText editTextHint = findViewById(R.id.formattedEditText_hint);
        FormattedEditText.Config.create()
                .mode(FormattedEditText.MODE_HINT) // 提示模式
                .formatStyle("000 000 0000 0000 000X") // 格式化样式
                .maskFilter(
                        "X",
                        new FormattedEditText.Matcher() {
                            @Override
                            public boolean hasMatch(String previousText, String value) {
                                return TextUtils.isDigitsOnly(value)
                                        || value.toUpperCase().equals("X");
                            }
                        })
                .maskFilter(
                        "0",
                        new FormattedEditText.Matcher() {
                            @Override
                            public boolean hasMatch(String previousText, String value) {
                                return TextUtils.isDigitsOnly(value);
                            }
                        }) // 自定义掩码匹配
                .hintText(
                        "100 000 2020 0101 000X") // 提示文字，不同于默认提示文字（android:hint），会在输入字符后依然显示，必须和格式化样式格式一致
                .hintColor(Color.GRAY) // 提示文字颜色
                .showHintWhileEmpty(true) // 是否清除数据后显示默认提示文字（android:hint），开启后不会用空白数据占位符填充数据位
                .config(editTextHint);
        editTextHint.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        appendBeforeTextChangedLog(s, start, count, after);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        appendOnTextChangedLog(s, start, before, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        appendAfterTextChangedLog(s);
                        textViewHint.setText(
                                String.format(
                                        getString(R.string.mode_hint_desc),
                                        editTextHint.getRealText()));
                    }
                });
        textViewHint.setText(
                String.format(getString(R.string.mode_hint_desc), editTextHint.getRealText()));
        final TextView textViewNumber = findViewById(R.id.textView_number);
        final MaskNumberEditText editTextNumber = findViewById(R.id.maskNumberEditText_number);
        editTextNumber.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        appendBeforeTextChangedLog(s, start, count, after);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        appendOnTextChangedLog(s, start, before, count);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        appendAfterTextChangedLog(s);
                        textViewNumber.setText(
                                String.format(
                                        getString(R.string.mask_number_hint),
                                        editTextNumber.getRealNumber()));
                    }
                });
        textViewNumber.setText(
                String.format(
                        getString(R.string.mask_number_hint), editTextNumber.getRealNumber()));
    }

    private void checkSimpleValid(EditText editText) {
        if (editText.length() == 0) {
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else if (editText.length() == 13) {
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.icon_valid),
                    null);
        } else {
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.icon_invalid),
                    null);
        }
    }

    private void appendBeforeTextChangedLog(CharSequence s, int start, int count, int after) {
        mTextViewLogs.append(
                String.format(
                        Locale.getDefault(),
                        "beforeTextChanged: s: %s, start: %d, count: %d, after: %d \n",
                        s,
                        start,
                        count,
                        after));
    }

    private void appendOnTextChangedLog(CharSequence s, int start, int before, int count) {
        mTextViewLogs.append(
                String.format(
                        Locale.getDefault(),
                        "onTextChanged: s: %s, start: %d, before: %d, count: %d \n",
                        s,
                        start,
                        before,
                        count));
    }

    private void appendAfterTextChangedLog(Editable s) {
        mTextViewLogs.append(String.format(Locale.getDefault(), "afterTextChanged: s: %s \n\n", s));
    }
}
