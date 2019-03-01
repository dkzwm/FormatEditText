package me.dkzwm.widget.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
        EditText editText = findViewById(R.id.editText_original);
        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        mTextViewLogs.append(
                                "未格式化: beforeTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    count:"
                                        + count
                                        + "   after:"
                                        + after
                                        + "\n");
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mTextViewLogs.append(
                                "未格式化: onTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    before:"
                                        + before
                                        + "   count:"
                                        + count
                                        + "\n");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mTextViewLogs.append("未格式化: afterTextChanged    s:" + s + "\n\n");
                    }
                });
        final EditText editTextComplex = findViewById(R.id.formattedEditText_complex);
        editTextComplex.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        mTextViewLogs.append(
                                "复杂格式化后: beforeTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    count:"
                                        + count
                                        + "   after:"
                                        + after
                                        + "\n");
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mTextViewLogs.append(
                                "复杂格式化后: onTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    before:"
                                        + before
                                        + "   count:"
                                        + count
                                        + "\n");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mTextViewLogs.append("复杂格式化后: afterTextChanged    s:" + s + "\n\n");
                    }
                });
        FormattedEditText editTextSimple = findViewById(R.id.formattedEditText_simple);
        editTextSimple.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        mTextViewLogs.append(
                                "简单格式化后: beforeTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    count:"
                                        + count
                                        + "   after:"
                                        + after
                                        + "\n");
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mTextViewLogs.append(
                                "简单格式化后: onTextChanged    s:"
                                        + s
                                        + "   "
                                        + "start:"
                                        + start
                                        + "    before:"
                                        + before
                                        + "   count:"
                                        + count
                                        + "\n");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mTextViewLogs.append("简单格式化后: afterTextChanged    s:" + s + "\n\n");
                    }
                });
    }
}
