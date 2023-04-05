package vn.edu.tdtu.finalexam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DeleteReceiver extends BroadcastReceiver {
    private String phone;
    private DatabaseReference user;
    private String day;

    @Override
    public void onReceive(Context context, Intent intent) {
        phone = intent.getStringExtra("phone");
        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference().child("users").child(phone);
        day = intent.getStringExtra("day");
        user.child("trash").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user.child("trash").removeValue();
//                SharedPreferences preferences = context.getSharedPreferences("setDate", 0);
//                preferences.edit().remove("start").apply();
//                preferences.edit().remove("end").apply();
                Log.d("TAG","DAY ALARM " + day);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        user.child("DeleteCycle").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cycle) {
                if(cycle.exists())
                {
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                    try {
                        Date next = dateFormat.parse(day);
                        Calendar nextCycle = Calendar.getInstance();
                        nextCycle.setTime(next);
                        nextCycle.add(Calendar.DATE, Math.toIntExact((Long) cycle.getValue()));
                        nextCycle.set(Calendar.HOUR_OF_DAY, 0);
                        nextCycle.set(Calendar.MINUTE, 0);
                        nextCycle.set(Calendar.SECOND, 0);
                        nextCycle.set(Calendar.MILLISECOND, 0);

                        String set = dateFormat.format(nextCycle.getTime());
                        Log.d("TAG","Next Cycle " + set);
                        user.child("DeleteDate").setValue(set);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
