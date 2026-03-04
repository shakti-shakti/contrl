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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);

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

            // Create QR code with parent ID
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(parentId, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            ivQRCode.setImageBitmap(bitmap);
            Toast.makeText(this, "QR Code generated! Show this to child to pair.", Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
