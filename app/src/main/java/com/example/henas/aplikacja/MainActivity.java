package com.example.henas.aplikacja;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.henas.aplikacja.database.TodoDbAdapter;
import com.example.henas.aplikacja.model.TodoTask;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {
    private Button btnAddNew;
    private Button btnClearCompleted;
    private Button btnSynch;

    private ListView lvTodos;
    private LinearLayout llControlButtons;
    private LinearLayout llNewTaskButtons;

    private TodoDbAdapter todoDbAdapter;
    private Cursor todoCursor;
    private List<TodoTask> tasks;
    private TodoTasksAdapter listAdapter;
    Intent addNewTask = new Intent(MainActivity.this, NewTaskActivity.class);

    private GoogleApiClient client;
    ProgressDialog prgDialog;
    TodoDbAdapter controller = new TodoDbAdapter(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUiElements();
        initListView();
        initButtonsOnClickListeners();
        ArrayList<HashMap<String, String>> todoList =  controller.getAllTasks();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
        //Inicjalizacja okna dialogowego
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Trwa synchrnizacja z zewnętrzną bazą danych. Proszę czekać...");
        prgDialog.setCancelable(false);
    }

    //Inicjalizacja elementów interfejsu
    private void initUiElements() {
        btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnClearCompleted = (Button) findViewById(R.id.btnClearCompleted);
        btnSynch = (Button) findViewById(R.id.refresh);
        lvTodos = (ListView) findViewById(R.id.lvTodos);
        llControlButtons = (LinearLayout) findViewById(R.id.llControlButtons);
        llNewTaskButtons = (LinearLayout) findViewById(R.id.llNewTaskButtons);
    }

    //Inicjalizacja klikalnej listy zadań
    private void initListView() {
        fillListViewData();
        initListViewOnItemClick();
    }

    //Wypełnianie listy zadań
    private void fillListViewData() {
        todoDbAdapter = new TodoDbAdapter(getApplicationContext());
        todoDbAdapter.open();
        getAllTasks();
        listAdapter = new TodoTasksAdapter(this, tasks);
        lvTodos.setAdapter(listAdapter);
    }

    //Pobieranie listy zadań
    private void getAllTasks() {
        tasks = new ArrayList<TodoTask>();
        todoCursor = getAllEntriesFromDb();
        updateTaskList();
    }

    //Pobieranie wszystkich zadań z wewnętrznej bazy danych
    private Cursor getAllEntriesFromDb() {
        todoCursor = todoDbAdapter.getAllTodos();
        if (todoCursor != null) {
            startManagingCursor(todoCursor);
            todoCursor.moveToFirst();
        }
        return todoCursor;
    }

    //Aktualizacja listy zadań
    private void updateTaskList() {
        if (todoCursor != null && todoCursor.moveToFirst()) {
            do {
                long id = todoCursor.getLong(TodoDbAdapter.ID_COLUMN);
                String description = todoCursor.getString(TodoDbAdapter.DESCRIPTION_COLUMN);
                String date = todoCursor.getString(TodoDbAdapter.DATE_COLUMN);
                boolean completed = todoCursor.getInt(TodoDbAdapter.COMPLETED_COLUMN) > 0 ? true : false;
                String status = todoCursor.getString(TodoDbAdapter.STATUS_COLUMN);
                tasks.add(new TodoTask(id, description, date, completed, status));
            } while (todoCursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        if (todoDbAdapter != null)
            todoDbAdapter.close();
        super.onDestroy();
    }

    private void initListViewOnItemClick() {
        lvTodos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                TodoTask task = tasks.get(position);
                if (task.isCompleted()) {
                    todoDbAdapter.updateTodo(task.getId(), task.getDescription(), task.getDate(), false, "no");
                } else {
                    todoDbAdapter.updateTodo(task.getId(), task.getDescription(), task.getDate(), true, "no");
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

    //Obsługa przycisków
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
                    case R.id.refresh:
                        synchTask();
                        break;
                    default:
                        break;
                }
            }
        };
        ;
        btnAddNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
            }
        });
        btnClearCompleted.setOnClickListener(onClickListener);
        btnSynch.setOnClickListener(onClickListener);
    }

    //Obsługa przycisku synchronizacji
    private void synchTask () {
        syncSQLiteMySQLDB();
    }

    //Synchronizowanie z zewnęrzną bazą danych
    public void syncSQLiteMySQLDB(){
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  todoDbAdapter.getAllTasks();
        if(userList.size()!=0){
            if(todoDbAdapter.dbSyncCount() != 0){
                prgDialog.show();
                params.put("todosJSON", todoDbAdapter.composeJSONfromSQLite());
                System.out.println(todoDbAdapter.composeJSONfromSQLite());
                client.post("http://godfryd2.unixstorm.org/sqlitemysqlsync/inserttask.php", params ,new AsyncHttpResponseHandler() {


                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        prgDialog.hide();
                        try {
                            String content = new String(responseBody);
                            JSONArray arr = new JSONArray(content);

                            System.out.println("content: "+ content);
                            System.out.println("arr: "+arr);

                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                System.out.println(obj.get("_id"));
                                System.out.println(obj.get("updateStatus"));
                                todoDbAdapter.updateSyncStatus(obj.get("_id").toString(), obj.get("updateStatus").toString());
                            }
                            Toast.makeText(getApplicationContext(), "Synchronizacja się powiodła!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Wystąpił błąd (Odpowiedź z serwera jest nieprawidłowa)!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

             @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // TODO Auto-generated method stub
                        prgDialog.hide();
                        if(statusCode == 404){
                            Toast.makeText(getApplicationContext(), "Nie znaleziono żądanego zasobu!", Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), "Coś poszło nie tak po stronie serwera!", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Wystąpił niespodziewany błąd! (Sprawdź dostęp do internetu)", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(getApplicationContext(), "Baza wewnętrzna i zewnętrzna są zsynchronizowane!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Brak danych w bazie, dodaj zadanie", Toast.LENGTH_LONG).show();
        }
    }

    //Przejście do ekranu dodawania nowego zadania
    private void addNewTask() {
        MainActivity.this.startActivity(addNewTask);
    }

    //Czyszczenie zakończonych zadań
    private void clearCompletedTasks() {
        if (todoCursor != null && todoCursor.moveToFirst()) {
            do {
                if (todoCursor.getInt(TodoDbAdapter.COMPLETED_COLUMN) == 1) {
                    long id = todoCursor.getLong(TodoDbAdapter.ID_COLUMN);
                    todoDbAdapter.deleteTodo(id);
                }
            } while (todoCursor.moveToNext());
        }
        updateListViewData();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
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