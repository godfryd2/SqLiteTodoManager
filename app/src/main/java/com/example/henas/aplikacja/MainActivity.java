package com.example.henas.aplikacja;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.henas.aplikacja.database.TodoDbAdapter;
import com.example.henas.aplikacja.model.TodoTask;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends Activity {
    private Button btnAddNew;
    private Button btnClearCompleted;
    private Button btnSave;
    private Button btnCancel;
    private EditText etNewTask;
    private DatePicker etNewTaskDate;
    private ListView lvTodos;
    private LinearLayout llControlButtons;
    private LinearLayout llNewTaskButtons;

    private TodoDbAdapter todoDbAdapter;
    private Cursor todoCursor;
    private List<TodoTask> tasks;
    private TodoTasksAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUiElements();
        initListView();
        initButtonsOnClickListeners();
    }

    private void initUiElements() {
        btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnClearCompleted = (Button) findViewById(R.id.btnClearCompleted);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        etNewTask = (EditText) findViewById(R.id.etNewTask);
        etNewTaskDate = (DatePicker) findViewById(R.id.etNewTaskDate);
        lvTodos = (ListView) findViewById(R.id.lvTodos);
        llControlButtons = (LinearLayout) findViewById(R.id.llControlButtons);
        llNewTaskButtons = (LinearLayout) findViewById(R.id.llNewTaskButtons);
    }

    private void initListView() {
        fillListViewData();
        initListViewOnItemClick();
    }

    private void fillListViewData() {
        todoDbAdapter = new TodoDbAdapter(getApplicationContext());
        todoDbAdapter.open();
        getAllTasks();
        listAdapter = new TodoTasksAdapter(this, tasks);
        lvTodos.setAdapter(listAdapter);
    }

    private void getAllTasks() {
        tasks = new ArrayList<TodoTask>();
        todoCursor = getAllEntriesFromDb();
        updateTaskList();
    }

    private Cursor getAllEntriesFromDb() {
        todoCursor = todoDbAdapter.getAllTodos();
        if(todoCursor != null) {
            startManagingCursor(todoCursor);
            todoCursor.moveToFirst();
        }
        return todoCursor;
    }

    private void updateTaskList() {
        if(todoCursor != null && todoCursor.moveToFirst()) {
            do {
                long id = todoCursor.getLong(TodoDbAdapter.ID_COLUMN);
                String description = todoCursor.getString(TodoDbAdapter.DESCRIPTION_COLUMN);
                String date = todoCursor.getString(TodoDbAdapter.DATE_COLUMN);
                boolean completed = todoCursor.getInt(TodoDbAdapter.COMPLETED_COLUMN) > 0 ? true : false;
                tasks.add(new TodoTask(id, description, date, completed));
            } while(todoCursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        if(todoDbAdapter != null)
            todoDbAdapter.close();
        super.onDestroy();
    }

    private void initListViewOnItemClick() {
        lvTodos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                TodoTask task = tasks.get(position);
                if(task.isCompleted()){
                    todoDbAdapter.updateTodo(task.getId(), task.getDescription(), task.getDate(), false);
                } else {
                    todoDbAdapter.updateTodo(task.getId(), task.getDescription(), task.getDate(), true);
                }
                updateListViewData();
            }
        });
    }

    private void updateListViewData() {
        todoCursor.requery();
        tasks.clear();
        updateTaskList();
        listAdapter.notifyDataSetChanged();
    }

    private void initButtonsOnClickListeners() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnAddNew:
                        addNewTask();
                        break;
                    case R.id.btnSave:
                        saveNewTask();
                        break;
                    case R.id.btnCancel:
                        cancelNewTask();
                        break;
                    case R.id.btnClearCompleted:
                        clearCompletedTasks();
                        break;
                    default:
                        break;
                }
            }
        };
        btnAddNew.setOnClickListener(onClickListener);
        btnClearCompleted.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    private void showOnlyNewTaskPanel() {
        setVisibilityOf(llControlButtons, false);
        setVisibilityOf(llNewTaskButtons, true);
        setVisibilityOf(etNewTask, true);
        setVisibilityOf(etNewTaskDate, true);
    }

    private void showOnlyControlPanel() {
        setVisibilityOf(llControlButtons, true);
        setVisibilityOf(llNewTaskButtons, false);
        setVisibilityOf(etNewTask, false);
        setVisibilityOf(etNewTaskDate, false);
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

    private void addNewTask(){
        showOnlyNewTaskPanel();
    }

    private void saveNewTask(){
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
            //etNewTaskDate.setText("");
            hideKeyboard();
            showOnlyControlPanel();
        }

        updateListViewData();
    }

    private void cancelNewTask() {
        etNewTask.setText("");
        //etNewTaskDate.setText("");
        showOnlyControlPanel();
    }

    private void clearCompletedTasks(){
        if(todoCursor != null && todoCursor.moveToFirst()) {
            do {
                if(todoCursor.getInt(TodoDbAdapter.COMPLETED_COLUMN) == 1) {
                    long id = todoCursor.getLong(TodoDbAdapter.ID_COLUMN);
                    todoDbAdapter.deleteTodo(id);
                }
            } while (todoCursor.moveToNext());
        }
        updateListViewData();
    }
}