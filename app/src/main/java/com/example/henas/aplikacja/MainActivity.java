package com.example.henas.aplikacja;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import com.example.henas.aplikacja.database.TodoDbAdapter;
import com.example.henas.aplikacja.model.TodoTask;
import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends Activity {
    private Button btnAddNew;
    private Button btnClearCompleted;

    private ListView lvTodos;
    private LinearLayout llControlButtons;
    private LinearLayout llNewTaskButtons;

    private TodoDbAdapter todoDbAdapter;
    private Cursor todoCursor;
    private List<TodoTask> tasks;
    private TodoTasksAdapter listAdapter;
    Intent addNewTask = new Intent(MainActivity.this, NewTaskActivity.class);

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
                    case R.id.btnClearCompleted:
                        clearCompletedTasks();
                        break;
                    default:
                        break;
                }
            }
        };;
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
            }
        });
        btnClearCompleted.setOnClickListener(onClickListener);
    }

    private void addNewTask(){
        MainActivity.this.startActivity(addNewTask);
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