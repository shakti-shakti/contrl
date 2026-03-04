package com.family.parentalcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.utils.SupabaseClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.UUID;

public class QRScannerActivity extends AppCompatActivity {

    private TextView tvResult;
    private Button btnScan;
    private Button btnConfirm;
    private String scannedParentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        tvResult = findViewById(R.id.tvQRResult);
        btnScan = findViewById(R.id.btnScanQR);
        btnConfirm = findViewById(R.id.btnConfirmPairing);

        btnScan.setOnClickListener(v -> startQRScan());

        btnConfirm.setOnClickListener(v -> {
            if (!scannedParentId.isEmpty()) {
                confirmPairing();
            } else {
                Toast.makeText(this, "Please scan QR code first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQRScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan parent's QR code to pair");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                scannedParentId = result.getContents();
                tvResult.setText("Parent ID: " + scannedParentId);
                Toast.makeText(this, "QR Code scanned successfully!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void confirmPairing() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String childId = prefs.getString("child_id", UUID.randomUUID().toString());
            String childName = prefs.getString("child_name", "Unknown");
            String childAge = prefs.getString("child_age", "0");

            // Save child ID if not exists
            if (!childId.isEmpty() && childId.equals(UUID.randomUUID().toString())) {
                prefs.edit().putString("child_id", childId).apply();
            }

            // Create relationship in Supabase
            SupabaseClient.getInstance(this).pairChild(
                    scannedParentId,
                    childId,
                    childName,
                    Integer.parseInt(childAge),
                    new SupabaseClient.SupabaseCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            Toast.makeText(QRScannerActivity.this, 
                                    "Successfully paired with parent!", 
                                    Toast.LENGTH_SHORT).show();
                            
                            // Save parent ID
                            prefs.edit().putString("parent_id", scannedParentId).apply();
                            
                            // Redirect to child dashboard
                            Intent intent = new Intent(QRScannerActivity.this, ChildDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(QRScannerActivity.this, 
                                    "Pairing failed: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
