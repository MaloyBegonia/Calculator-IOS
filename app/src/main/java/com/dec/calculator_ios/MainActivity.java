package com.dec.calculator_ios;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.mozilla.javascript.Scriptable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView resultTv, solutionTv;
    MaterialButton buttonC, buttonBrackOpen, buttonBrackClose;
    MaterialButton buttonDivide, buttonMultiply, buttonPlus, buttonMinus, buttonEquals;
    MaterialButton button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    MaterialButton buttonAC, buttonDot;

    boolean isLastInputNumeric = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTv = findViewById(R.id.result_tv);
        solutionTv = findViewById(R.id.solution_tv);

        assignId(buttonC, R.id.button_c);
        assignId(buttonBrackOpen, R.id.button_open_bracket);
        assignId(buttonBrackClose, R.id.button_close_bracket);
        assignId(buttonDivide, R.id.button_divide);
        assignId(buttonMultiply, R.id.button_multiply);
        assignId(buttonPlus, R.id.button_plus);
        assignId(buttonMinus, R.id.button_minus);
        assignId(buttonEquals, R.id.button_equals);
        assignId(button0, R.id.button_0);
        assignId(button1, R.id.button_1);
        assignId(button2, R.id.button_2);
        assignId(button3, R.id.button_3);
        assignId(button4, R.id.button_4);
        assignId(button5, R.id.button_5);
        assignId(button6, R.id.button_6);
        assignId(button7, R.id.button_7);
        assignId(button8, R.id.button_8);
        assignId(button9, R.id.button_9);
        assignId(buttonAC, R.id.button_ac);
        assignId(buttonDot, R.id.button_dot);
    }

    void assignId(MaterialButton btn, int id) {
        btn = findViewById(id);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        MaterialButton button = (MaterialButton) view;
        if (button != null) {
            String buttonText = button.getText().toString();

            // Добавляем вибрацию при нажатии кнопки
            vibrate();

            switch (buttonText) {
                case "AC":
                    clearDisplay();
                    break;
                case "=":
                    displayResult();
                    break;
                case "C":
                    removeLastCharacter();
                    break;
                case "+":
                case "-":
                case "*":
                case "/":
                    // Обработка операторов +, -, *, /
                    if (isLastInputNumeric) {
                        // Если последний символ был числом, добавляем оператор к выражению
                        appendToDisplay(buttonText);
                    }
                    break;
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                    appendToDisplay(buttonText);
                    break;
                case ".":
                    appendDecimalPoint();
                    break;
                default:
                    // Неизвестный символ, игнорируем
                    break;
            }
        }
    }

    private void vibrate() {
        // Получаем доступ к сервису вибрации
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Создаем вибрацию
            final int VIBRATION_DURATION_MS = 10; // Добавляем константу для длительности вибрации
            VibrationEffect effect = VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE);
            // Запускаем вибрацию
            vibrator.vibrate(effect);
        }
    }

    private void clearDisplay() {
        solutionTv.setText("");
        resultTv.setText("0");
        isLastInputNumeric = false;
    }

    private void displayResult() {
        String expression = solutionTv.getText().toString();
        if (!expression.isEmpty()) {
            String result = getResult(expression);
            // Добавляем проверку на "Ошибка вычисления" и неполное выражение
            if (!result.equals("Ошибка вычисления") && !result.equals(expression)) {
                // Проверяем, был ли последний введенный символ числом перед выводом результата
                if (isLastInputNumeric) {
                    // Если последний символ был числом, выводим результат как числовое значение
                    double numericResult = Double.parseDouble(result);
                    if (numericResult == (long) numericResult) {
                        // Если результат является целым числом, конвертируем его в long и выводим без запятой
                        resultTv.setText(String.valueOf((long) numericResult));
                    } else {
                        // В противном случае, выводим результат с десятичной точкой
                        resultTv.setText(result);
                    }
                } else {
                    // Иначе, выводим результат как строку
                    resultTv.setText(result);
                }
            }
        }
    }

    private void removeLastCharacter() {
        String dataToCalculate = solutionTv.getText().toString();
        if (!dataToCalculate.isEmpty()) {
            solutionTv.setText(dataToCalculate.substring(0, dataToCalculate.length() - 1));
            // Проверяем, был ли последний введенный символ числом
            if (isLastInputNumeric && dataToCalculate.length() > 0) {
                isLastInputNumeric = Character.isDigit(dataToCalculate.charAt(dataToCalculate.length() - 1));
            } else {
                isLastInputNumeric = false;
            }
        }
    }

    private void appendToDisplay(String text) {
        // Проверяем, был ли последний введенный символ числом
        if (isLastInputNumeric) {
            String dataToCalculate = solutionTv.getText().toString() + text;
            solutionTv.setText(dataToCalculate);
            // Выполняем автоподсчет после каждого ввода
            displayResult();
        } else {
            solutionTv.setText(text);
        }
        isLastInputNumeric = true;
    }

    private void appendDecimalPoint() {
        String dataToCalculate = solutionTv.getText().toString();
        if (!dataToCalculate.isEmpty() && !dataToCalculate.contains(".")) {
            solutionTv.setText(dataToCalculate + ".");
            isLastInputNumeric = false;
        }
    }

    String getResult(String data) {
        try {
            // Создаем и входим в новый контекст Rhino
            org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();
            // Отключаем оптимизацию для совместимости с Android
            context.setOptimizationLevel(-1);
            // Инициализируем стандартные объекты Rhino
            Scriptable scriptable = context.initStandardObjects();
            // Выполняем скрипт и получаем результат
            Object result = context.evaluateString(scriptable, data, "JavaScript", 1, null);

            if (result != null) {
                String finalResult = result.toString();
                // Удаляем окончание ".0" для целых чисел
                if (finalResult.endsWith(".0")) {
                    finalResult = finalResult.replace(".0", "");
                }
                return finalResult;
            } else {
                return "Ошибка вычисления";
            }
        } catch (Exception e) {
            // Логируем ошибку
            Log.e("CalculatorError", "Ошибка при вычислении: ", e);
            return "Ошибка вычисления";
        } finally {
            // Выходим из контекста Rhino
            org.mozilla.javascript.Context.exit();
        }
    }

    public void buttonClicked(View view) {
        Toast myToast = Toast.makeText(getApplicationContext(),
                "Author @MaloyBegonia",
                Toast.LENGTH_LONG);
        myToast.show();
    }
}
