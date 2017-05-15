package com.example.henas.aplikacja;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.henas.aplikacja.database.TodoDbAdapter;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;

public class NewTaskActivity extends AppCompatActivity {
    private Button btnSave;
    private Button btnCancel;
    private static EditText etNewTask;
    private DatePicker etNewTaskDate;
    private TimePicker etNewTaskTime;
    private TodoDbAdapter todoDbAdapter;
    private GoogleApiClient client;
    public static String description;

    public static String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        initUiElements();
        initButtonsOnClickListeners();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initUiElements() {
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        etNewTask = (EditText) findViewById(R.id.etNewTask);
        etNewTaskDate = (DatePicker) findViewById(R.id.etNewTaskDate);
        etNewTaskTime = (TimePicker) findViewById(R.id.etNewTaskTime);
        etNewTaskTime.setIs24HourView(true);
    }

    private void initButtonsOnClickListeners() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnSave:
                        saveNewTask();
                        break;
                    case R.id.btnCancel:
                        cancelNewTask();
                        break;
                    default:
                        break;
                }
            }
        };
        btnSave.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etNewTask.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etNewTaskDate.getWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void saveNewTask() {
        todoDbAdapter = new TodoDbAdapter(getApplicationContext());
        todoDbAdapter.open();
        Calendar calendar = Calendar.getInstance();

        String taskDescription = etNewTask.getText().toString();
        setDescription(taskDescription);
        String taskYear = String.valueOf(etNewTaskDate.getYear());
        calendar.set(Calendar.YEAR, etNewTaskDate.getYear());

        String taskMonth = String.valueOf(etNewTaskDate.getMonth());
        calendar.set(Calendar.MONTH, etNewTaskDate.getMonth());
        if (taskMonth.length() == 1)
            taskMonth = '0' + taskMonth;

        String taskDay = String.valueOf(etNewTaskDate.getDayOfMonth());
        calendar.set(Calendar.DAY_OF_MONTH, etNewTaskDate.getDayOfMonth());
        if (taskDay.length() == 1)
            taskDay = '0' + taskDay;

        String taskHour = String.valueOf(etNewTaskTime.getCurrentHour());
        calendar.set(Calendar.HOUR_OF_DAY, etNewTaskTime.getCurrentHour());
        if (taskHour.length() == 1)
            taskHour = '0' + taskHour;

        String taskMinute = String.valueOf(etNewTaskTime.getCurrentMinute());
        calendar.set(Calendar.MINUTE, etNewTaskTime.getCurrentMinute());
        if (taskMinute.length() == 1)
            taskMinute = '0' + taskMinute;

        calendar.set(Calendar.SECOND, 0);

        calendar.set(Calendar.AM_PM, Calendar.PM);

        String taskDate = taskYear + '-' + taskMonth + '-' + taskDay + ' ' + taskHour + ':' + taskMinute;
        if (taskDescription.equals("")) {
            etNewTask.setError("Opis zadania nie może być pusty!");
        } else {
            todoDbAdapter.insertTodo(taskDescription, taskDate);
            etNewTask.setText("");
            hideKeyboard();

            Intent myIntent = new Intent(NewTaskActivity.this, MyReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(NewTaskActivity.this, 0, myIntent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);


            startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
        }
    }

    private void cancelNewTask() {
        etNewTask.setText("");
        startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
    }
}
