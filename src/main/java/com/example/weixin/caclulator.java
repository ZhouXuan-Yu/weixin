package com.example.weixin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class caclulator extends AppCompatActivity implements View.OnClickListener {

    // 按钮组件
    private Button[] numberButtons = new Button[10]; // 0-9数字按钮
    private Button btnPoint, btnAdd, btnSub, btnMul, btnDiv, btnClear, btnDelete, btnEquals, btnMinus;
    private EditText etInput;
    private boolean clearFlag = false; // 是否需要清空输入框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caclulator);

        initViews();
        setClickListeners();
        
        // 修复可能的布局问题
        fixLayoutForAllDevices();
    }

    /**
     * 初始化所有视图组件
     */
    private void initViews() {
        // 初始化数字按钮
        numberButtons[0] = findViewById(R.id.btn_0);
        numberButtons[1] = findViewById(R.id.btn_1);
        numberButtons[2] = findViewById(R.id.btn_2);
        numberButtons[3] = findViewById(R.id.btn_3);
        numberButtons[4] = findViewById(R.id.btn_4);
        numberButtons[5] = findViewById(R.id.btn_5);
        numberButtons[6] = findViewById(R.id.btn_6);
        numberButtons[7] = findViewById(R.id.btn_7);
        numberButtons[8] = findViewById(R.id.btn_8);
        numberButtons[9] = findViewById(R.id.btn_9);

        // 初始化功能按钮
        btnPoint = findViewById(R.id.btn_pt);
        btnAdd = findViewById(R.id.btn_add);
        btnSub = findViewById(R.id.btn_sub);
        btnMul = findViewById(R.id.btn_mul);
        btnDiv = findViewById(R.id.btn_div);
        btnClear = findViewById(R.id.btn_clr);
        btnDelete = findViewById(R.id.btn_del);
        btnEquals = findViewById(R.id.btn_eq);
        btnMinus = findViewById(R.id.btn_minus);
        etInput = findViewById(R.id.result);
    }

    /**
     * 修复在某些设备上可能存在的布局问题
     */
    private void fixLayoutForAllDevices() {
        // 确保最后一行按钮正确显示
        if (numberButtons[0] != null) {
            numberButtons[0].post(() -> {
                // 主动触发布局刷新以解决某些设备上的显示问题
                numberButtons[0].requestLayout();
                if (btnPoint != null) btnPoint.requestLayout();
                if (btnEquals != null) btnEquals.requestLayout();
            });
        }
    }

    /**
     * 为所有按钮设置点击监听器
     */
    private void setClickListeners() {
        // 数字按钮
        for (Button btn : numberButtons) {
            if (btn != null) {
                btn.setOnClickListener(this);
            }
        }

        // 功能按钮
        if (btnPoint != null) btnPoint.setOnClickListener(this);
        if (btnAdd != null) btnAdd.setOnClickListener(this);
        if (btnSub != null) btnSub.setOnClickListener(this);
        if (btnMul != null) btnMul.setOnClickListener(this);
        if (btnDiv != null) btnDiv.setOnClickListener(this);
        if (btnClear != null) btnClear.setOnClickListener(this);
        if (btnDelete != null) btnDelete.setOnClickListener(this);
        if (btnEquals != null) btnEquals.setOnClickListener(this);
        if (btnMinus != null) btnMinus.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (etInput == null) return;

        String currentText = etInput.getText().toString();
        int viewId = v.getId();

        // 数字和小数点输入
        if (isNumberOrPoint(viewId)) {
            handleNumberInput(v, currentText);
        }
        // 运算符输入
        else if (isOperator(viewId)) {
            handleOperatorInput(v, currentText);
        }
        // 功能按键
        else if (viewId == R.id.btn_clr) {
            handleClear();
        }
        else if (viewId == R.id.btn_del) {
            handleDelete(currentText);
        }
        else if (viewId == R.id.btn_eq) {
            calculateResult();
        }
    }

    /**
     * 判断是否为数字或小数点按钮
     */
    private boolean isNumberOrPoint(int viewId) {
        return viewId == R.id.btn_0 || viewId == R.id.btn_1 || viewId == R.id.btn_2 ||
                viewId == R.id.btn_3 || viewId == R.id.btn_4 || viewId == R.id.btn_5 ||
                viewId == R.id.btn_6 || viewId == R.id.btn_7 || viewId == R.id.btn_8 ||
                viewId == R.id.btn_9 || viewId == R.id.btn_pt;
    }

    /**
     * 判断是否为运算符按钮
     */
    private boolean isOperator(int viewId) {
        return viewId == R.id.btn_add || viewId == R.id.btn_sub ||
                viewId == R.id.btn_mul || viewId == R.id.btn_div ||
                viewId == R.id.btn_minus; // 添加另一个减号按钮
    }

    /**
     * 处理数字和小数点输入
     */
    private void handleNumberInput(View v, String currentText) {
        if (clearFlag) {
            clearFlag = false;
            currentText = "";
            etInput.setText("");
        }

        String buttonText = ((Button) v).getText().toString();

        // 防止重复输入小数点
        if (buttonText.equals(".")) {
            String[] parts = currentText.split(" ");
            String lastPart = parts[parts.length - 1];
            if (lastPart.contains(".")) {
                return; // 已经有小数点了，不再添加
            }
        }

        etInput.setText(currentText + buttonText);
    }

    /**
     * 处理运算符输入
     */
    private void handleOperatorInput(View v, String currentText) {
        if (clearFlag) {
            clearFlag = false;
        }

        // 如果已经有运算符，替换之前的运算符
        if (currentText.contains("+") || currentText.contains("-") ||
                currentText.contains("×") || currentText.contains("÷")) {
            int spaceIndex = currentText.indexOf(" ");
            if (spaceIndex != -1) {
                currentText = currentText.substring(0, spaceIndex);
            }
        }

        // 如果输入框为空，不添加运算符
        if (currentText.trim().isEmpty()) {
            return;
        }

        String operator = ((Button) v).getText().toString();
        etInput.setText(currentText + " " + operator + " ");
    }

    /**
     * 处理清空按钮
     */
    private void handleClear() {
        clearFlag = false;
        etInput.setText("");
    }

    /**
     * 处理删除按钮
     */
    private void handleDelete(String currentText) {
        if (clearFlag) {
            clearFlag = false;
            etInput.setText("");
        } else if (currentText != null && !currentText.isEmpty()) {
            etInput.setText(currentText.substring(0, currentText.length() - 1));
        }
    }

    /**
     * 计算结果
     */
    private void calculateResult() {
        String expression = etInput.getText().toString().trim();

        if (expression == null || expression.isEmpty()) {
            return;
        }

        // 如果没有运算符，不进行计算
        if (!expression.contains(" ")) {
            return;
        }

        if (clearFlag) {
            clearFlag = false;
            return;
        }

        try {
            double result = evaluateExpression(expression);
            clearFlag = true;

            // 如果结果是整数，显示为整数
            if (result == (int) result) {
                etInput.setText(String.valueOf((int) result));
            } else {
                etInput.setText(String.valueOf(result));
            }
        } catch (Exception e) {
            etInput.setText("错误");
            clearFlag = true;
        }
    }

    /**
     * 计算表达式的值
     */
    private double evaluateExpression(String expression) throws ArithmeticException {
        String[] parts = expression.split(" ");

        if (parts.length != 3) {
            throw new ArithmeticException("表达式格式错误");
        }

        String leftOperand = parts[0];
        String operator = parts[1];
        String rightOperand = parts[2];

        // 处理空操作数的情况
        if (leftOperand.isEmpty() && rightOperand.isEmpty()) {
            return 0;
        }

        double left = leftOperand.isEmpty() ? 0 : Double.parseDouble(leftOperand);
        double right = rightOperand.isEmpty() ? 0 : Double.parseDouble(rightOperand);

        switch (operator) {
            case "+":
                return left + right;
            case "-":
                return leftOperand.isEmpty() ? -right : left - right;
            case "×":
                return left * right;
            case "÷":
                if (right == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                return left / right;
            default:
                throw new ArithmeticException("未知运算符: " + operator);
        }
    }
}