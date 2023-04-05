package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, View.OnClickListener {
    private RecyclerView rView,rViewRe;
    private LinearLayout home, setting, recycle;
    private RelativeLayout emptyList,emptyListRecyle;
    private NavigationBarView navView;
    private NavigationView settingView;
    private FloatingActionButton addNoteButton;
    private DatabaseReference user;
    private NoteAdapter adapter;
    private NoteRecycleAdapter adapterRe;
    private SearchView searchView,searchViewRe;
    private TextView textRecycle;
    private ImageView ViewList,ViewListRe;
    private GridLayoutManager gridLayoutManager,gridLayoutManagerRe;
    private List<Note> notes  = new ArrayList<Note>();
    private List<NoteRecycle> notesRe  = new ArrayList<NoteRecycle>();;
    private String phone;
    private Button btnRecycle;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ChildEventListener mchild;
    private static int SPAN_COUNT_ONE = 1;
    private static int SPAN_COUNT_THREE = 3;
    private TextInputLayout dateInput;

    private static int position = -1;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();

        home = findViewById(R.id.homeMenu);
        setting = findViewById(R.id.settingMenu);
        recycle = findViewById(R.id.recycleMenu);
        emptyList = findViewById(R.id.emptyList);
        emptyListRecyle = findViewById(R.id.emptyListRecyle);
        ViewList = findViewById(R.id.ViewList);
        ViewListRe = findViewById(R.id.ViewListRecycle);

        rView = findViewById(R.id.reView);
        rViewRe = findViewById(R.id.reViewRecycle);
        SharedPreferences sp1 =this.getSharedPreferences("gridLayoutManager", Context.MODE_PRIVATE);
        String span = sp1.getString("span", "");
        SharedPreferences sp2 =this.getSharedPreferences("gridLayoutManagerRe", Context.MODE_PRIVATE);
        String span1 = sp2.getString("span", "");

        getLayout(span);
        getLayoutRe(span1);

        ViewList.setOnClickListener(this);
        ViewListRe.setOnClickListener(this);

        SharedPreferences sp =MainActivity.this.getSharedPreferences("User", MODE_PRIVATE);
        phone = sp.getString("phone", "");

        navView = findViewById(R.id.bottom_bar);
        getLayoutSelected(savedInstanceState);
        navView.setOnItemSelectedListener(this);

        addNoteButton = findViewById(R.id.addNoteButton);
        addNoteButton.setOnClickListener(this);

        settingView = findViewById(R.id.settingView);
        settingView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        Logout();
                        break;
                    case R.id.changePassword:
                        changePassword();
                    default:
                        break;
                }
                return true;
            }
        });

        textRecycle = findViewById(R.id.textRecycle);
        btnRecycle = findViewById(R.id.buttonRecycle);
        btnRecycle.setOnClickListener(this);

        checkDeleteTime();

    }

    private void changePassword() {
        startActivity(new Intent(MainActivity.this,ChangePasswordActivity.class));
        finish();
    }

    private void Logout() {
        SharedPreferences day = this.getSharedPreferences("setDate", 0);
        day.edit().remove("day").apply();

        SharedPreferences span = this.getSharedPreferences("gridLayoutManager", MODE_PRIVATE);
        span.edit().remove("span").apply();

        SharedPreferences phone  = this.getSharedPreferences("User", MODE_PRIVATE);
        phone.edit().remove("phone").apply();

        sendToLogin();
    }

    private void sendToLogin(){
        startActivity(new Intent(MainActivity.this , LoginActivity.class));
        finish();
    }


    private void checkDeleteTime(){
//        user = FirebaseDatabase.getInstance().getReference().child("users").child(phone);
        user.child("users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    user.child("users").child(phone).child("DeleteDate").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                cancelDelete();
                                try {

                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);

                                    String now = dateFormat.format(cal.getTime());
                                    String dayset = String.valueOf(snapshot.getValue());

                                    Date formatDayset = dateFormat.parse(dayset);
                                    Date formatDaynow = dateFormat.parse(now);

                                    if(getDiffer(formatDaynow,formatDayset) <= 0)
                                    {
                                        startDelete(formatDayset);
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                defaultDeleteTime();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    user.child("users").child(phone).child("DeleteCycle").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot cycle) {
                            if(cycle.exists())
                            {
                                textRecycle.setText("Tự xóa sau: " + cycle.getValue() + " ngày");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void defaultDeleteTime() {
        int day = 1;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cancelDelete();

        String dateString = dateFormat.format(cal.getTime());
        user.child("users").child(phone).child("DeleteDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    user.child("users").child(phone).child("DeleteDate").setValue(dateString).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
//                            startDelete(cal.getTime());
                            textRecycle.setText("Tự xóa sau: " + day + " ngày");
                        }
                    });
                    user.child("users").child(phone).child("DeleteCycle").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void updateSelectedItem(int actionId) {
//        Menu menu = navView.getMenu();
//
//        for (int i = 0, size = menu.size(); i < size; i++) {
//            MenuItem item = menu.getItem(i);
//            Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_SHORT).show();
//            item.setChecked(item.getItemId() == actionId);
//        }
//    }

    private void getLayout(String span)
    {
        if(span.equals("3"))
        {
            gridLayoutManager = new GridLayoutManager(MainActivity.this,3);
        }
        else if(span.equals("1"))
        {
            gridLayoutManager = new GridLayoutManager(MainActivity.this,1);
        }
        else
        {
            gridLayoutManager = new GridLayoutManager(MainActivity.this,SPAN_COUNT_ONE);
        }

        if(gridLayoutManager.getSpanCount() == SPAN_COUNT_ONE)
        {
            ViewList.setImageResource(R.drawable.listview_icon);
        }
        else
        {
            ViewList.setImageResource(R.drawable.gridview_icon);
        }
    }

    private void getLayoutRe(String span)
    {
        if(span.equals("3"))
        {
            gridLayoutManagerRe = new GridLayoutManager(MainActivity.this,3);
        }
        else if(span.equals("1"))
        {
            gridLayoutManagerRe = new GridLayoutManager(MainActivity.this,1);
        }
        else
        {
            gridLayoutManagerRe = new GridLayoutManager(MainActivity.this,SPAN_COUNT_ONE);
        }

        if(gridLayoutManagerRe.getSpanCount() == SPAN_COUNT_ONE)
        {
            ViewListRe.setImageResource(R.drawable.listview_icon);
        }
        else
        {
            ViewListRe.setImageResource(R.drawable.gridview_icon);
        }
    }

    private void getNoteList(String fullPhone) {
        home.setVisibility(View.VISIBLE);
        recycle.setVisibility(View.GONE);
        setting.setVisibility(View.GONE);
        notes = new ArrayList<>();
        rView.setHasFixedSize(true);
        rView.setLayoutManager(gridLayoutManager);
        rView.smoothScrollToPosition(0);

        user.child("users").child(fullPhone).child("notes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    emptyList.setVisibility(View.GONE);
                    rView.setVisibility(View.VISIBLE);
                    notes.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren())
                    {
                        Note newNote = dataSnapshot.getValue(Note.class);
                        notes.add(newNote);
                    }
                    Collections.sort(notes, new Comparator<Note>(){
                    @Override
                        public int compare(Note o1, Note o2) {
                            Date d1 = null;
                            Date d2 = null;
                            DateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
                            try {
                                d1 = dateFormat.parse(o1.getCreatedTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                d2 = dateFormat.parse(o2.getCreatedTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(d1 != null && d2 != null)
                                return d1.compareTo(d2);
                            return -1;
                        }
                    });
                    Collections.reverse(notes);
                    adapter = new NoteAdapter(MainActivity.this,notes);
                    adapter.setNotesList(notes);
                    rView.setAdapter(adapter);

                    if(!notes.isEmpty())
                    {
                        searchNote(adapter);
                    }
                }
                else
                {
                    if(notes.isEmpty())
                    {
                        rView.setVisibility(View.GONE);
                        emptyList.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNoteRecycleList(String fullPhone) {
        recycle.setVisibility(View.VISIBLE);
        setting.setVisibility(View.GONE);
        home.setVisibility(View.GONE);
        notesRe = new ArrayList<>();
        rViewRe.setHasFixedSize(true);
        rViewRe.setLayoutManager(gridLayoutManagerRe);
        rViewRe.smoothScrollToPosition(0);

        mchild=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                NoteRecycle noteRecycle = dataSnapshot.getValue(NoteRecycle.class);
                for (int i = 0; i < notesRe.size(); i++) {
                    // Find the item to remove and then remove it by index
                    if (notesRe.get(i).getId().equals(noteRecycle.getId())) {
                        notesRe.remove(i);
                        break;
                    }
                }
                adapterRe.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        user.child("users").child(fullPhone).child("trash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    rViewRe.setVisibility(View.VISIBLE);
                    emptyListRecyle.setVisibility(View.GONE);
                    notesRe.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren())
                    {
                        NoteRecycle newNote = dataSnapshot.getValue(NoteRecycle.class);
                        notesRe.add(newNote);
                    }
                    Collections.sort(notesRe, new Comparator<NoteRecycle>(){
                        @Override
                        public int compare(NoteRecycle o1, NoteRecycle o2) {
                            Date d1 = null;
                            Date d2 = null;
                            DateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
                            try {
                                d1 = dateFormat.parse(o1.getAddingTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                d2 = dateFormat.parse(o2.getAddingTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(d1 != null && d2 != null)
                                return d1.compareTo(d2);
                            return -1;
                        }
                    });
                    Collections.reverse(notesRe);
                    adapterRe = new NoteRecycleAdapter(MainActivity.this,notesRe);
                    adapterRe.setNotesList(notesRe);
                    rViewRe.setAdapter(adapterRe);

                    if(!notesRe.isEmpty())
                    {
                        searchNoteRe(adapterRe);

//                        if (date1.equals(date)) {
//                            Toast.makeText(MainActivity.this, "Yes", Toast.LENGTH_SHORT).show();
//                        }
//                        user = FirebaseDatabase.getInstance().getReference().child("trash").child("deleteTime");
//                        user.setValue(datetime);
                    }
                }
                else
                {
                    if(notesRe.isEmpty())
                    {
                        rViewRe.setVisibility(View.GONE);
                        emptyListRecyle.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        user.child("users").child(fullPhone).child("trash").addChildEventListener(mchild);
    }

    private void getSetting()
    {
        setting.setVisibility(View.VISIBLE);
        home.setVisibility(View.GONE);
        recycle.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                home.setVisibility(View.VISIBLE);
                recycle.setVisibility(View.GONE);
                setting.setVisibility(View.GONE);
                rViewRe.setAdapter(null);
                getNoteList(phone);
                break;
            case R.id.menu_recycleBin:
                recycle.setVisibility(View.VISIBLE);
                setting.setVisibility(View.GONE);
                home.setVisibility(View.GONE);
                getNoteRecycleList(phone);
                rView.setAdapter(null);
                break;
            case R.id.menu_option:
                setting.setVisibility(View.VISIBLE);
                home.setVisibility(View.GONE);
                recycle.setVisibility(View.GONE);
                rView.setAdapter(null);
                rViewRe.setAdapter(null);
                break;
            default:
                break;
        }
        position = item.getOrder();
        return true;
    }

    private void getLayoutSelected(Bundle savedInstanceState)
    {
        if(savedInstanceState == null)
        {
           position = 0;
        }

        navView.getMenu().getItem(position).setChecked(true);

        if(navView.getMenu().getItem(0).isChecked())
        {
            getNoteList(phone);
        }
        if(navView.getMenu().getItem(1).isChecked())
        {
            getNoteRecycleList(phone);
        }
        if(navView.getMenu().getItem(3).isChecked())
        {
            getSetting();
        }
    }

    private void switchIcon() {
        if(gridLayoutManager.getSpanCount() == SPAN_COUNT_ONE)
        {
            ViewList.setImageResource(R.drawable.listview_icon);
        }
        else
        {
            ViewList.setImageResource(R.drawable.gridview_icon);
        }
    }

    private void switchLayout() {
        if(gridLayoutManager.getSpanCount() == SPAN_COUNT_ONE)
        {
            SharedPreferences sp1= getSharedPreferences("gridLayoutManager", MODE_PRIVATE);
            SharedPreferences.Editor Ed=sp1.edit();
            Ed.putString("span", "3");
            Ed.apply();
            gridLayoutManager.setSpanCount(SPAN_COUNT_THREE);
        }
        else
        {
            SharedPreferences sp1= getSharedPreferences("gridLayoutManager", MODE_PRIVATE);
            SharedPreferences.Editor Ed=sp1.edit();
            Ed.putString("span", "1");
            Ed.apply();
            gridLayoutManager.setSpanCount(SPAN_COUNT_ONE);
        }
        if(!notes.isEmpty())
        {
            adapter.notifyItemRangeChanged(0,adapter.getItemCount());
        }
    }

    private void switchIconRe() {
        if(gridLayoutManagerRe.getSpanCount() == SPAN_COUNT_ONE)
        {
            ViewListRe.setImageResource(R.drawable.listview_icon);
        }
        else
        {
            ViewListRe.setImageResource(R.drawable.gridview_icon);
        }
    }

    private void switchLayoutRe() {
        if(gridLayoutManagerRe.getSpanCount() == SPAN_COUNT_ONE)
        {
            SharedPreferences sp1= getSharedPreferences("gridLayoutManagerRe", MODE_PRIVATE);
            SharedPreferences.Editor Ed=sp1.edit();
            Ed.putString("span", "3");
            Ed.apply();
            gridLayoutManagerRe.setSpanCount(SPAN_COUNT_THREE);
        }
        else
        {
            SharedPreferences sp1= getSharedPreferences("gridLayoutManagerRe", MODE_PRIVATE);
            SharedPreferences.Editor Ed=sp1.edit();
            Ed.putString("span", "1");
            Ed.apply();
            gridLayoutManagerRe.setSpanCount(SPAN_COUNT_ONE);
        }
        if(!notesRe.isEmpty())
        {
            adapterRe.notifyItemRangeChanged(0,adapterRe.getItemCount());
        }
    }

    private void searchNote(NoteAdapter adapter) {

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = findViewById(R.id.searchMenu);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
    private void searchNoteRe(NoteRecycleAdapter adapterRe) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchViewRe = findViewById(R.id.searchMenuRecycle);
        searchViewRe.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchViewRe.setMaxWidth(Integer.MAX_VALUE);

        searchViewRe.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapterRe.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterRe.getFilter().filter(newText);
                return false;
            }
        });
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(position != -1)
//        {
//            navView.getMenu().getItem(position).setChecked(true);
//        }
//    }

    private void startDelete(Date day) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DeleteReceiver.class);
        intent.putExtra("phone",phone);
        intent.putExtra("day",dateFormat.format(day));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, day.getTime(), pendingIntent);
    }

    private void cancelDelete() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DeleteReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(position != -1)
        {
            navView.getMenu().getItem(position).setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.addNoteButton:
                user.child("users").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            User user = snapshot.getValue(User.class);
//                            Toast.makeText(MainActivity.this, String.valueOf(notes.size()), Toast.LENGTH_SHORT).show();
                            if(user.active == false && notes.size() >= 5)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Tài khoản chưa được kích hoạt");
                                builder.setCancelable(true);
                                builder.setPositiveButton("Kích hoạt ngay",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                    @Override
                                                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                                                    }

                                                    @Override
                                                    public void onVerificationFailed(FirebaseException e) {
                                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        Log.d("TAG",e.getMessage());
                                                    }


                                                    @Override
                                                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                        super.onCodeSent(verificationId, forceResendingToken);
                                                        Toast.makeText(MainActivity.this, "Mã OTP đã được gửi đến số điện thoại", Toast.LENGTH_SHORT).show();
                                                        Intent i = new Intent(MainActivity.this, OTPActivity.class);
                                                        i.putExtra("verificationId", verificationId);
                                                        i.putExtra("phone",phone);
                                                        startActivity(i);
                                                        finish();
                                                    }

                                                };

                                                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                                        .setPhoneNumber(phone)
                                                        .setTimeout(60L, TimeUnit.SECONDS)
                                                        .setActivity(MainActivity.this)
                                                        .setCallbacks(mCallbacks)
                                                        .build();
                                                PhoneAuthProvider.verifyPhoneNumber(options);
                                            }
                                        });
                                builder.setNegativeButton("Để sau",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                            else
                            {
                                Intent i = new Intent(MainActivity.this,AddNoteActivity.class);
                                i.putExtra("phoneUser",phone);
                                startActivity(i);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.ViewList:
                switchLayout();
                switchIcon();
                break;
            case R.id.ViewListRecycle:
                switchLayoutRe();
                switchIconRe();
                break;
            case R.id.buttonRecycle:
                showInputDate();
            default:
                break;
        }
    }

    public void showInputDate()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.datetime_set_layout,null);
        dateInput = view.findViewById(R.id.textdate);
        builder.setTitle("Thời gian tự động xóa");
        builder.setView(view);
        dateInput.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDate(dateInput);
            }
        });

        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String datetime = dateInput.getEditText().getText().toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY,0);
                c.set(Calendar.MINUTE,0);
                c.set(Calendar.SECOND,0);
                c.set(Calendar.MILLISECOND,0);
                String createdTime = simpleDateFormat.format(c.getTime());
                try {
                    Date date1 = simpleDateFormat.parse(createdTime);
                    Date date2 = simpleDateFormat.parse(datetime);
                    if(date1 != null && date2 != null) {
                        printDifference(date1, date2);
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        String d1 = dateFormat.format(date1);
                        String d2 = dateFormat.format(date2);
                        printDifference(date1, date2);
                        user.child("users").child(phone);

                        if(getDiffer(date1,date2) < 0)
                        {
                            Toast.makeText(MainActivity.this, "Ngày không được bé hơn ngày hiện tại", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            user.child("users").child(phone).child("DeleteCycle").setValue((Integer)(getDiffer(date1,date2)));

                            user.child("users").child(phone).child("DeleteDate").setValue(d2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
//                                    startDelete(date2);
                                    Toast.makeText(MainActivity.this, "Điều chỉnh thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    public static void addDays(Date d, int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        d.setTime( c.getTime().getTime() );
    }

    public static void addMinutes(Date d, int minutes)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.MINUTE, minutes);
        d.setTime( c.getTime().getTime() );
    }


    public void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long days = different / daysInMilli;
//        different = different % daysInMilli;

        if(days < 0)
        {
            Toast.makeText(this, "Ngày không được bé hơn ngày hiện tại", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String date = days + " ngày";
            textRecycle.setText("Tự xóa sau: " + date);
        }
//        long elapsedSeconds = different / secondsInMilli;

    }

    private int getDiffer(Date startDate, Date endDate)
    {
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        int days = (int) (different / daysInMilli);
        return days;
    }

    private void showDate(TextInputLayout dateInput) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String date = day + "/" + (month + 1) + "/" + year ;
                dateInput.getEditText().setText(date);
            }
        }, year, month, day);
        dialog.show();
    }
}
