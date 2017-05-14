package com.example.henas.aplikacja;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.EditText;

import com.example.henas.aplikacja.database.TodoDbAdapter;

public class NewTaskActivity extends AppCompatActivity {
    private Button btnSave;
    private Button btnCancel;
    private EditText etNewTask;
    private DatePicker etNewTaskDate;
    private TimePicker etNewTaskTime;
    private TodoDbAdapter todoDbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        initUiElements();
        initButtonsOnClickListeners();
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
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etNewTask.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etNewTaskDate.getWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addNotification(String text) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle("Menadżer zadań")
                        .setContentText(text);
        Intent resultIntent = new Intent(this, NotificationView.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationView.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        PendingIntent.FLAG_UPDATE_CURRENT,
                        0
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3000, mBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void saveNewTask(){
        todoDbAdapter = new TodoDbAdapter(getApplicationContext());
        todoDbAdapter.open();

        String taskDescription = etNewTask.getText().toString();
        String taskYear = String.valueOf(etNewTaskDate.getYear());

        String taskMonth = String.valueOf(etNewTaskDate.getMonth());
        if(taskMonth.length() == 1)
            taskMonth = '0' + taskMonth;

        String taskDay = String.valueOf(etNewTaskDate.getDayOfMonth());
        if(taskDay.length() == 1)
            taskDay = '0' + taskDay;

        String taskHour = String.valueOf(etNewTaskTime.getCurrentHour());
        if(taskHour.length() == 1)
            taskHour = '0' + taskHour;

        String taskMinute = String.valueOf(etNewTaskTime.getCurrentMinute());
        if(taskMinute.length() == 1)
            taskMinute = '0' + taskMinute;

        String taskDate = taskYear + '-' + taskMonth + '-' + taskDay + ' ' + taskHour + ':' + taskMinute;
        if(taskDescription.equals("")){
            etNewTask.setError("Opis zadania nie może być pusty!");
        } else {
            todoDbAdapter.insertTodo(taskDescription, taskDate);
            etNewTask.setText("");
            hideKeyboard();
            addNotification(taskDescription);
            startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
        }
    }

    private void cancelNewTask() {
        etNewTask.setText("");
        startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
    }
}
