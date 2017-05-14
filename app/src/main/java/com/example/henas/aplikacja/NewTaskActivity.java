package com.example.henas.aplikacja;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
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
import java.util.Calendar;
import java.util.Date;

import com.example.henas.aplikacja.database.TodoDbAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static java.util.Calendar.getInstance;

public class NewTaskActivity extends AppCompatActivity {
    private Button btnSave;
    private Button btnCancel;
    private EditText etNewTask;
    private DatePicker etNewTaskDate;
    private TimePicker etNewTaskTime;
    private TodoDbAdapter todoDbAdapter;
    private GoogleApiClient client;

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
    private void addNotification(String text) {
        Calendar cal = getInstance();
        System.out.println("SART"+cal.getTimeInMillis());
        NotificationCompat.Builder Builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle("Menadżer zadań")
                        .setContentText(text)
                        .setShowWhen(cal.getTimeInMillis() == cal.getTimeInMillis()+30000)
                        .setDefaults(Notification.DEFAULT_SOUND |
                                Notification.DEFAULT_VIBRATE)
                        .setSound(
                                RingtoneManager.getDefaultUri(
                                        RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.GREEN, 100, 100);
        Intent resultIntent = new Intent(this, NotificationView.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationView.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        PendingIntent.FLAG_UPDATE_CURRENT,
                        0
                );
        Builder.setContentIntent(resultPendingIntent);
        NotificationManager NotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationManager.notify(0, Builder.build());
        System.out.println("STOP"+cal.getTimeInMillis());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void saveNewTask() {
        todoDbAdapter = new TodoDbAdapter(getApplicationContext());
        todoDbAdapter.open();

        String taskDescription = etNewTask.getText().toString();
        String taskYear = String.valueOf(etNewTaskDate.getYear());

        String taskMonth = String.valueOf(etNewTaskDate.getMonth());
        if (taskMonth.length() == 1)
            taskMonth = '0' + taskMonth;

        String taskDay = String.valueOf(etNewTaskDate.getDayOfMonth());
        if (taskDay.length() == 1)
            taskDay = '0' + taskDay;

        String taskHour = String.valueOf(etNewTaskTime.getCurrentHour());
        if (taskHour.length() == 1)
            taskHour = '0' + taskHour;

        String taskMinute = String.valueOf(etNewTaskTime.getCurrentMinute());
        if (taskMinute.length() == 1)
            taskMinute = '0' + taskMinute;

        String taskDate = taskYear + '-' + taskMonth + '-' + taskDay + ' ' + taskHour + ':' + taskMinute;
        if (taskDescription.equals("")) {
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("NewTask Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
