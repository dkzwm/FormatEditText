package me.dkzwm.widget.demo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import me.dkzwm.widget.fet.FormattedEditText;

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
        TextWatcher textWatcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        mTextViewLogs.append(
                                String.format(
                                        Locale.CHINA,
                                        "beforeTextChanged: s: %s, start: %d, "
                                                + "count: %d, after: %d \n",
                                        s,
                                        start,
                                        count,
                                        after));
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mTextViewLogs.append(
                                String.format(
                                        Locale.CHINA,
                                        "onTextChanged: s: %s, start: %d, "
                                                + "before: %d, count: %d \n",
                                        s,
                                        start,
                                        before,
                                        count));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mTextViewLogs.append(
                                String.format(Locale.CHINA, "afterTextChanged: s: %s \n", s));
                    }
                };
        final FormattedEditText editTextComplex = findViewById(R.id.formattedEditText_complex);
        editTextComplex.addTextChangedListener(textWatcher);
        final FormattedEditText editTextSimple = findViewById(R.id.formattedEditText_simple);
        editTextSimple.addTextChangedListener(textWatcher);
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
        final FormattedEditText editTextMask = findViewById(R.id.formattedEditText_mask);
        editTextMask.addTextChangedListener(textWatcher);
        final FormattedEditText editTextHint = findViewById(R.id.formattedEditText_hint);
        editTextHint.addTextChangedListener(textWatcher);
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
}
