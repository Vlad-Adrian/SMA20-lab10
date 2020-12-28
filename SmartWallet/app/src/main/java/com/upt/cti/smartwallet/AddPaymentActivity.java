package com.upt.cti.smartwallet;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.upt.cti.smartwallet.model.Payment;
import com.upt.cti.smartwallet.model.PaymentType;

import java.util.Arrays;

public class AddPaymentActivity extends AppCompatActivity {
    private EditText description, cost;
    private Spinner spinner;
    private TextView paymentTime;
    private Button saveButton, deleteButtton;
    private DatabaseReference databaseReference;
    Payment payment;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fab_activity_layout);
        description = findViewById(R.id.paymentDescription);
        cost = findViewById(R.id.paymentCost);
        spinner = findViewById(R.id.paymentSpinner);
        paymentTime = findViewById(R.id.paymentTime);
        saveButton = findViewById(R.id.saveButton);
        deleteButtton = findViewById(R.id.deleteButton);

        //adapter initialization
        String[] types = PaymentType.getTypes();
        final ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sAdapter);

        payment = AppState.get().getCurrentPayment();
        if (payment != null) {
            description.setText(payment.getName());
            cost.setText(String.valueOf(payment.getCost()));
            paymentTime.setText("Time of payment" + payment.timestamp);
            try {
                spinner.setSelection(Arrays.asList(types).indexOf(payment.getType()));
            } catch (Exception e) {
            }
        } else {
            paymentTime.setText(AppState.getCurrentTimeDate());
        }
    }

    public void clicked(View view) {
        String currentMonth;
        switch (view.getId()) {
            case R.id.saveButton:
                if (payment != null) {
                    save(payment.timestamp);
                } else {
                    save(AppState.getCurrentTimeDate());
                }
                break;
            case R.id.deleteButton:
                if(payment != null)
                    delete(payment.timestamp);
                else
                    Toast.makeText(this, "payment does not exist", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void delete(String timestamp) {
        databaseReference = AppState.get().getDatabaseReference();
        databaseReference.child("wallet").child(timestamp).removeValue();
        this.finish();
    }

    private void save(String timestamp ) {
        if (cost.getText().toString().isEmpty() || description.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please complete all fields in order to update", Toast.LENGTH_SHORT);
        } else {
            String time = timestamp;
            Double c = Double.parseDouble(cost.getText().toString());
            String n = description.getText().toString();
            String t = spinner.getSelectedItem().toString();
            //databaseReference.child("wallet").child(time).setValue(new Payment(c, n, t));
            //this.finish();
            AppState.get().getDatabaseReference().child("wallet").child(time).setValue(new Payment(c, n, t));
            this.finish();


        }
    }


}
