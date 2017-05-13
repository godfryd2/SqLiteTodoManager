package com.example.henas.aplikacja;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
            }
        });
    }
    private void setVisibilityOf(View v, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        v.setVisibility(visibility);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etNewTask.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etNewTaskDate.getWindowToken(), 0);
    }
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

        String taskDate = taskYear + '-' + taskMonth + '-' + taskDay;
        if(taskDescription.equals("")){
            etNewTask.setError("Your task description couldn't be empty string.");
        } else {
            todoDbAdapter.insertTodo(taskDescription, taskDate);
            etNewTask.setText("");
            hideKeyboard();
            startActivity(new Intent(NewTaskActivity.this, MainActivity.class));
        }
    }

    private void cancelNewTask() {
        etNewTask.setText("");
    }
}
