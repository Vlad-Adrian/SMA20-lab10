package com.upt.cti.smartwallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upt.cti.smartwallet.model.Payment;
import com.upt.cti.smartwallet.ui.PaymentAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Payments extends AppCompatActivity implements Serializable {
    private TextView tStatus;
    private Button bPrevious, bNext;
    private DatabaseReference databaseReference;
    private FloatingActionButton fabAdd;
    private ListView listPayments;
    private int currentMonth;
    private List<Payment> payments = new ArrayList<>();
    private PaymentAdapter adapter;
    SharedPreferences prefs;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_activity);

        tStatus = (TextView) findViewById(R.id.ttStatus);
        bPrevious = (Button) findViewById(R.id.bPrevious);
        bNext = (Button) findViewById(R.id.bNext);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        listPayments = (ListView) findViewById(R.id.listPayments);

        bPrevious.setOnClickListener(v -> onPrev());
        bNext.setOnClickListener(v -> onNext());
        fabAdd.setOnClickListener(v -> onFab());
        prefs = getSharedPreferences("lastMonth", MODE_PRIVATE);
        currentMonth = prefs.getInt("month", -1);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (currentMonth == -1) {
            database.setPersistenceEnabled(true);
            currentMonth = Month.monthFromTimestamp(AppState.getCurrentTimeDate());
        }
        databaseReference = database.getReference();
        AppState.get().setDatabaseReference(databaseReference);

        if (!AppState.isNetworkAvailable(this)) {
            if (AppState.get().hasLocalStorage(this)) {
                try {
                    payments = AppState.get().loadFromLocalBackup(this, currentMonth);
                    tStatus.setText("Found " + payments.size() + " payments for " + Month.intToMonthName(currentMonth) + ".");
                    adapter = new PaymentAdapter(this, R.layout.item_payment, payments);
                    listPayments.setAdapter(adapter);
                    if (payments.size() == 0) {
                        tStatus.setText("Found 0 payments for " + Month.intToMonthName(currentMonth));
                    }

                    listPayments.setOnItemClickListener((parent, view, position, id) -> {
                        AppState.get().setCurrentPayment(payments.get(position));

                        startActivity(new Intent(getApplicationContext(), AddPaymentActivity.class));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // setup firebase
            addPayments(p -> {
                adapter = new PaymentAdapter(this, R.layout.item_payment, p);
                listPayments.setAdapter(adapter);
                if (p.size() == 0) {
                    tStatus.setText("Found 0 payments for " + Month.intToMonthName(currentMonth));
                }
                listPayments.setOnItemClickListener((parent, view, position, id) -> {
                    AppState.get().setCurrentPayment(payments.get(position));

                    startActivity(new Intent(getApplicationContext(), AddPaymentActivity.class));
                });
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AppState.isNetworkAvailable(this)) {
            if (AppState.get().hasLocalStorage(this)) {
                payments = AppState.get().loadFromLocalBackup(this, currentMonth);
                adapter.change(payments);
            }
        }
    }

    private void addPayments(FirebaseCallback callback) {
        databaseReference.child("wallet").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    if (currentMonth == Month.monthFromTimestamp(snapshot.getKey())) {
                        Payment payment = snapshot.getValue(Payment.class);
                        payment.timestamp = snapshot.getKey();

                        payments.add(payment);
                        callback.onCallBack(payments);
                        AppState.get().updateLocalBackup(getApplicationContext(), payment, true);

                        tStatus.setText("Found " + payments.size() + " payments for " + Month.intToMonthName(currentMonth) + ".");
                    } else {
                        callback.onCallBack(payments);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < payments.size(); i++) {
                    if (payments.get(i).timestamp.equals(snapshot.getKey().toString()))
                        try {
                            Payment updatePayment = snapshot.getValue(Payment.class);
                            updatePayment.setTimestamp(snapshot.getKey());

                            payments.set(i, updatePayment);
                            AppState.get().updateLocalBackup(getApplicationContext(), payments.get(i), true);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < payments.size(); i++) {
                    if (payments.get(i).timestamp.equals(snapshot.getKey())) {
                        AppState.get().updateLocalBackup(getApplicationContext(), payments.get(i), false);
                        payments.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onPrev() {
        if (currentMonth == 0) {
            currentMonth = 11;
        } else {
            currentMonth -= 1;
        }
        prefs.edit().putInt("month", currentMonth).apply();
        recreate();
    }

    public void onNext() {
        if (currentMonth == 11) {
            currentMonth = 0;
        } else {
            currentMonth += 1;
        }
        prefs.edit().putInt("month", currentMonth).apply();
        recreate();
    }

    public void onFab() {
        AppState.get().setCurrentPayment(null);
        startActivity(new Intent(this, AddPaymentActivity.class));
    }

    private interface FirebaseCallback {
        void onCallBack(List<Payment> p);
    }
}
