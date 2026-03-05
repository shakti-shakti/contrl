package com.family.parentalcontrol.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.family.parentalcontrol.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRGeneratorActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private Button btnGenerateQR;
    private TextView tvParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        tvParentId = findViewById(R.id.tvParentId);

        btnGenerateQR.setOnClickListener(v -> generateQRCode());

        // Auto-generate on load
        generateQRCode();
    }

    private void generateQRCode() {
        try {
            SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
            String parentId = prefs.getString("parent_id", "");

            if (parentId.isEmpty()) {
                Toast.makeText(this, "Parent ID not found. Please complete setup.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify parent profile exists in Supabase before generating QR
            com.family.parentalcontrol.utils.SupabaseClient.getInstance(this).getUser(parentId, new com.family.parentalcontrol.utils.SupabaseClient.SupabaseCallback<com.family.parentalcontrol.models.User>() {
                @Override
                public void onSuccess(com.family.parentalcontrol.models.User user) {
                    if (user != null) {
                        // Parent profile exists, generate QR code
                        generateQRCodeInternal(parentId);
                    } else {
                        Toast.makeText(QRGeneratorActivity.this, "Parent profile not found in database. Please restart setup.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(QRGeneratorActivity.this, "Failed to verify parent profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQRCodeInternal(String parentId) {
        try {
            // Create QR code with parent ID
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(parentId, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            ivQRCode.setImageBitmap(bitmap);            tvParentId.setText("Parent ID: " + parentId);            Toast.makeText(this, "QR Code generated! Parent ID: " + parentId.substring(0, 8) + "...", Toast.LENGTH_LONG).show();

        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
