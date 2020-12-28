package com.upt.cti.smartwallet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.upt.cti.smartwallet.model.Payment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppState {
    private static AppState singletonObject;

    public static synchronized AppState get() {
        if (singletonObject == null) {
            singletonObject = new AppState();
        }
        return singletonObject;
    }

    // reference to Firebase used for reading and writing data
    private DatabaseReference databaseReference;
    // current payment to be edited or deleted
    private Payment currentPayment;

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void setCurrentPayment(Payment currentPayment) {
        this.currentPayment = currentPayment;
    }

    public Payment getCurrentPayment() {
        return currentPayment;
    }

    public static String getCurrentTimeDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdfDate.format(now);
    }

    public void updateLocalBackup(Context context, Payment payment, boolean toAdd) {
        String fileName = payment.timestamp;

        try {
            if (toAdd) {
                FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(payment);
                os.close();
                fos.close();
            } else {
                context.deleteFile(fileName);
            }
        } catch (IOException e) {
            Toast.makeText(context, "Cannot access local data.", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean hasLocalStorage(Context context) {
        return context.getFilesDir().listFiles().length > 0;
    }

    public List<Payment> loadFromLocalBackup(Context context, int month) {
        try {
            List<Payment> payments = new ArrayList<>();

            for (File file : context.getFilesDir().listFiles()) {
                try{
                FileInputStream fis = context.openFileInput(file.getName());
                ObjectInputStream is = new ObjectInputStream(fis);
                Payment payment = (Payment) is.readObject();

                if (month == (Month.monthFromTimestamp(payment.timestamp)))
                    payments.add(payment);
                is.close();
                fis.close();
            }catch (Exception e){//check if we can read file, if file is not created by us it will throw exception
                    e.printStackTrace();
                }
            }

            return payments;
        } catch (Exception e) {
            Toast.makeText(context, "Cannot access local data.", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =

                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}