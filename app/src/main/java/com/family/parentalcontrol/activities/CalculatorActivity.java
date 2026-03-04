package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.journeyapps.barcodescanner.CaptureActivity;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private StringBuilder calculatorValue = new StringBuilder();
    private String hiddenInput = ""; // Track 1234# input
    private static final String HIDDEN_SEQUENCE = "1234#";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        tvDisplay = findViewById(R.id.tvDisplay);

        // Number buttons
        int[] numberButtons = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, 
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

        for (int buttonId : numberButtons) {
            findViewById(buttonId).setOnClickListener(v -> {
                Button btn = (Button) v;
                String number = btn.getText().toString();
                calculatorValue.append(number);
                tvDisplay.setText(calculatorValue.toString());
                trackHiddenInput(number);
            });
        }

        // Operations
        findViewById(R.id.btnAdd).setOnClickListener(v -> appendOperator("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> appendOperator("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendOperator("*"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendOperator("/"));
        
        // Equals
        findViewById(R.id.btnEqual).setOnClickListener(v -> calculateResult());
        
        // Clear
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            calculatorValue.setLength(0);
            hiddenInput = "";
            tvDisplay.setText("0");
        });

        // Dot
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!calculatorValue.toString().contains(".")) {
                calculatorValue.append(".");
                tvDisplay.setText(calculatorValue.toString());
            }
        });

        // Hash button (for hidden menu trigger)
        findViewById(R.id.btnHash).setOnClickListener(v -> {
            calculatorValue.append("#");
            tvDisplay.setText(calculatorValue.toString());
            trackHiddenInput("#");
        });
    }

    private void trackHiddenInput(String input) {
        hiddenInput += input;
        
        // Check if user typed the hidden sequence "1234#"
        if (hiddenInput.contains(HIDDEN_SEQUENCE)) {
            launchQRScanner();
            hiddenInput = "";
        }
        
        // Keep only last 10 characters to avoid memory waste
        if (hiddenInput.length() > 10) {
            hiddenInput = hiddenInput.substring(hiddenInput.length() - 10);
        }
    }

    private void appendOperator(String operator) {
        if (calculatorValue.length() > 0) {
            calculatorValue.append(operator);
            tvDisplay.setText(calculatorValue.toString());
        }
    }

    private void calculateResult() {
        try {
            String expression = calculatorValue.toString();
            
            // Simple expression evaluator (for demo purposes)
            // In production, use a proper math library
            double result = evaluateExpression(expression);
            calculatorValue.setLength(0);
            calculatorValue.append(result);
            tvDisplay.setText(String.valueOf(result));
        } catch (Exception e) {
            tvDisplay.setText("Error");
            calculatorValue.setLength(0);
        }
    }

    private double evaluateExpression(String expression) {
        // Simple evaluator - handles basic math
        try {
            // Remove spaces
            expression = expression.replaceAll("\\s+", "");
            
            // Use JavaScript engine to evaluate
            return eval(expression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Simple expression evaluator
    private double eval(String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }

    private void launchQRScanner() {
        Toast.makeText(this, "Opening QR Scanner for child pairing...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }
}
