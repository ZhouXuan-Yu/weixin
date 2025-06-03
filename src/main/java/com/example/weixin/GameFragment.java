package com.example.weixin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class GameFragment extends Fragment {

    private TextView textView;
    private EditText editText;
    private Button button;
    private TextView resultTextView;
    private TextView attemptsTextView;
    private int randomNumber;
    private int attempts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_activity, container, false);

        textView = view.findViewById(R.id.textView);
        editText = view.findViewById(R.id.editText);
        button = view.findViewById(R.id.button);
        resultTextView = view.findViewById(R.id.resultTextView);
        attemptsTextView = view.findViewById(R.id.attemptsTextView);
        attempts = 0;

        // 生成一个1到100之间的随机数
        Random random = new Random();
        randomNumber = random.nextInt(100) + 1;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的数字
                String guessText = editText.getText().toString();

                if (!guessText.isEmpty()) {
                    int guess = Integer.parseInt(guessText);
                    attempts++;

                    // 检查用户猜测的数字
                    if (guess > randomNumber) {
                        resultTextView.setText("猜大了！");
                    } else if (guess < randomNumber) {
                        resultTextView.setText("猜小了！");
                    } else {
                        resultTextView.setText("恭喜你，猜对了！");

                        // 重新设置随机数
                        Random random = new Random();
                        randomNumber = random.nextInt(100) + 1;

                        // 清空输入框
                        editText.setText("");

                        // 重置猜测次数
                        attempts = 0;
                    }
                } else {
                    Toast.makeText(getActivity(), "请输入一个数字", Toast.LENGTH_SHORT).show();
                }

                // 更新猜测次数显示
                attemptsTextView.setText("猜测次数：" + attempts);
            }
        });

        return view;
    }
}
