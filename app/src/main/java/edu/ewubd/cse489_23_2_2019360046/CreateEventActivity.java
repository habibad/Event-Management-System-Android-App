package edu.ewubd.cse489_23_2_2019360046;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateEventActivity extends Activity {
    EditText etName, etPlace, etDate, etCapacity, etBudget, etEmail, etPhone, etDsc, etReminderTime;
    TextView errorTv;
    RadioButton rIndoor, rOutdoor, rOnline;
    private String eventID = "";
    private EventDB eventDB;
    private CheckBox chkReminder;
    private LinearLayout layoutReminder;
    private Long getTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventDB = new EventDB(this);

        etName = findViewById(R.id.etName);
        etPlace= findViewById(R.id.etPlace);
        etDate = findViewById(R.id.etDateTime);
        etCapacity = findViewById(R.id.etCapacity);
        etBudget = findViewById(R.id.etBudget);
        etEmail= findViewById(R.id.etEmail);
        etPhone= findViewById(R.id.etPhone);
        etDsc = findViewById(R.id.etDes);
        rIndoor = findViewById(R.id.rdIndoor);
        rOutdoor = findViewById(R.id.rdOutdoor);
        rOnline = findViewById(R.id.rdOnline);

        chkReminder = findViewById(R.id.chkReminder);
        layoutReminder = findViewById(R.id.layoutReminder);
        etReminderTime = findViewById(R.id.etReminderTime);

        chkReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutReminder.setVisibility(View.VISIBLE);
                } else {
                    layoutReminder.setVisibility(View.GONE);
                }
            }
        });

        Intent intent = getIntent();
        if(intent.hasExtra("EventID")){
            eventID = intent.getStringExtra("EventID");
            etName.setText(intent.getStringExtra("name"));
            etPlace.setText(intent.getStringExtra("place"));

            long longValue = 123456789L;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date(intent.getLongExtra("datetime", longValue));
            etDate.setText(sdf.format(date));

            getTime = intent.getLongExtra("datetime", longValue);

            etCapacity.setText(String.valueOf(intent.getIntExtra("capacity", 1)));
            etBudget.setText(String.valueOf(intent.getDoubleExtra("budget", 1)));
            etEmail.setText(intent.getStringExtra("email"));
            etPhone.setText(intent.getStringExtra("phone"));
            etDsc.setText(intent.getStringExtra("des"));
            String type = intent.getStringExtra("type");

            rIndoor.setChecked("IN".equals(type));
            rOnline.setChecked("ON".equals(type));
            rOutdoor.setChecked("OUT".equals(type));
        }

        errorTv = findViewById(R.id.tvErrorMsg);
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rIndoor = findViewById(R.id.rdIndoor);
                rOutdoor = findViewById(R.id.rdOutdoor);
                rOnline = findViewById(R.id.rdOnline);

                String name = etName.getText().toString();
                String capacity = etCapacity.getText().toString();
                String place = etPlace.getText().toString();
                String date = etDate.getText().toString();
                String budget = etBudget.getText().toString();
                String email = etEmail.getText().toString();
                String phone = etPhone.getText().toString();
                String desc = etDsc.getText().toString();
                String eventType = "";
                String err = "";

                int _capacity = Integer.parseInt(capacity);
                double _budget = Double.parseDouble(budget);
                long _date = 0;

                if(!name.isEmpty() && !place.isEmpty() && !date.isEmpty() && !capacity.isEmpty() && !budget.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !desc.isEmpty()){
                    if(name.length() < 4 || name.length() > 12 || !name.matches("^[a-zA-Z ]+$")){
                        err += "Invalid Name (4-12 long and only alphabets)\n";
                    }

                    if(place.length() >= 6 && place.length() <= 64 && !place.matches("^[a-zA-Z0-9, ]+$")){
                        err += "Invalid Place (only alpha-numeric and , and 6-64 characters)\n";
                    }

                    boolean isIndoor = rIndoor.isChecked();
                    boolean isOutdoor = rOutdoor.isChecked();
                    boolean isOnline = rOnline.isChecked();

                    if(!isIndoor && !isOutdoor && !isOnline){
                        err += "Please select event type\n";
                    } else {
                        eventType = isIndoor ? "IN" : (isOutdoor ? "OUT" : "ON");
                    }

                    int cap;
                    double event_budget;

                    cap = _capacity;
                    event_budget = _budget;

                    if(cap <= 0){
                        err += "Invalid capacity (number greater than zero)\n";
                    }

                    if(event_budget < 1000){
                        err += "Invalid budget (number greater than 1000.00)\n";
                    }

                    String format = "yyyy-MM-dd HH:mm";
                    try {
                        DateTimeFormatter formatter = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            formatter = DateTimeFormatter.ofPattern(format);

                            LocalDateTime inputDate = LocalDateTime.parse(date, formatter);
                            LocalDateTime curDate = LocalDateTime.now();

                            if (inputDate.isBefore(curDate)) {
                                err += "Input date and time is before the current date and time\n" +
                                        "or Invalid date format (yyyy-MM-dd HH:mm)\n";
                            }
                            else {
                                _date = inputDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        err += "Invalid date format (yyyy-MM-dd HH:mm)";
                    }

                    System.out.println("________Create Date New________");
                    System.out.println(_date);
                    getTime = _date;

                    String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
                    if(!email.matches(EMAIL_REGEX)){
                        err += "Invalid email format\n";
                    }

                    Pattern pattern = Pattern.compile("^\\+\\d{13}$");
                    Matcher matcher = pattern.matcher(phone);
                    if(!matcher.matches()){
                        err += "Invalid phone number (format +8801234556789)\n";
                    }

                    if(desc.length() < 10 || desc.length() > 1000){
                        err += "Invalid description format (10-1000 characters)\n";
                    }
                }
                else{
                    err += "Fill all the fields\n";
                }

                if(err.length() > 0){
                    showErrorDialog(err);
                    errorTv.setText(err);
                }

                if(eventID.isEmpty()){
                    eventID = name + System.currentTimeMillis();
                    eventDB.insertEvent(eventID, name, place, _date, _capacity, _budget, email, phone, desc, eventType);
                    Toast.makeText(CreateEventActivity.this, "Event Insert successfully!", Toast.LENGTH_SHORT).show();
                }
                else {
                    eventDB.updateEvent(eventID, name, place, _date, _capacity, _budget, email, phone, desc, eventType);
                    Toast.makeText(CreateEventActivity.this, "Event Update successfully!", Toast.LENGTH_SHORT).show();
                }

                if (!etReminderTime.getText().toString().isEmpty()) {
                    int reminderTimeInMinutes = Integer.parseInt(etReminderTime.getText().toString());
                    if (reminderTimeInMinutes > 0) {
                        scheduleReminderNotification(getApplicationContext(), reminderTimeInMinutes);
                    }
                }

                String keys[] = {"action", "sid", "semester", "id", "title", "place", "type", "date_time", "capacity", "budget", "email", "phone", "des"};
                String values[] = {"backup", "2019-3-60-046", "2023-2", eventID, name, place, eventType , ""+_date, ""+_capacity, ""+_budget, email, phone, desc};
                httpRequest(keys, values);
                finish();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, "event details");
                i.setType("text/plain");
                startActivity(i);
            }
        });
    }

    private void showErrorDialog(String errMsg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errMsg);
        builder.setCancelable(true);
        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void httpRequest(final String keys[],final String values[]){
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                for (int i=0; i<keys.length; i++){
                    params.add(new BasicNameValuePair(keys[i],values[i]));
                }
                String url= "https://www.muthosoft.com/univ/cse489/index.php";
                String data="";
                try {
                    data=JSONParser.getInstance().makeHttpRequest(url,"POST",params);
                    System.out.println(data);
                    return data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(String data){
                if(data!=null){
                    System.out.println(data);
                    System.out.println("Ok2");
                    Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void scheduleReminderNotification(Context context, int reminderTimeInSeconds) {
        Intent notificationIntent = new Intent(context, MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueRequestCode(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long reminderMillis = getTime - (reminderTimeInSeconds * 60 * 1000);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderMillis, pendingIntent);
    }

    private int uniqueRequestCode() {
        return (int) System.currentTimeMillis();
    }
}