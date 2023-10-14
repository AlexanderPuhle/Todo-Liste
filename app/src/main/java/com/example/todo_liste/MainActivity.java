package com.example.todo_liste;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements AufgabenInterface{
    private static final String TAG = "MainActivity";
    RecyclerView aufgabenView;
    private AufgabenAdapter aufgabenAdapter;
    List<AufgabenZeile> aufgabenListe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //aufgabenAdapter = new AufgabenAdapter(this, aufgabenListe);
        aufgabenView = findViewById(R.id.todoListe);
        aufgabenView.setAdapter(aufgabenAdapter);
        aufgabenView.setLayoutManager(new LinearLayoutManager(this));
        aufgabenListe = new ArrayList<>();

        //Laden aller Aufgaen in die RecyclerView
        getAufgaben();

        //Löschen oder erledigt setzen von Elementen in der RecyclerView
        ItemTouchHelper itemSwipeHelper = new ItemTouchHelper(swipeHelper);
        itemSwipeHelper.attachToRecyclerView(aufgabenView);

        Button addAufgabe = findViewById(R.id.btnAddAufgabe);
        addAufgabe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addAufgabe = new Intent(MainActivity.this, AufgabeActivity.class);
                addAufgabe.putExtra("bearbeitungsart", "insert");
                startActivity(addAufgabe);
            }
        });

        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Erfolgreich abgemeldet",
                                        Toast.LENGTH_SHORT).show();
                                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                                finish();
                                startActivity(loginIntent);
                            }
                        });
            }
        });
    }

    private void getAufgaben() {
        OkHttpClient selAufgaben = new OkHttpClient();
        String getAufgabenUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/sel_aufgaben.php";

        Request request = new Request.Builder().url(getAufgabenUrl).build();

        selAufgaben.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Erste Zeile soll fehlermeldung ausgeben
                AufgabenZeile fehlerZeile = new AufgabenZeile(1, "PHP-Zugriff",
                        "Fehler beim lesen der Aufgaben","Fehlerbeschreibung: "
                        + e.getMessage(), 1, "Fehler", "MariaDB",
                        "9999-12-31", "9999-12-31", "Fehler SQL");

                aufgabenListe.add(fehlerZeile);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aufgabenAdapter = new AufgabenAdapter(MainActivity.this, aufgabenListe, MainActivity.this);
                        aufgabenView.setAdapter(aufgabenAdapter);
                    }
                });
                Log.d(TAG, "Error Request: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()) {
                    //Erste Zeile soll Fehlermeldung ausgeben
                    AufgabenZeile fehlerZeile = new AufgabenZeile(1, "PHP-Zugriff",
                            "Fehler beim lesen der Aufgaben","Fehlerbeschreibung: "
                            + response.message(), 1, "Fehler", "MariaDB",
                        "9999-12-31", "9999-12-31", "Fehler SQL");

                    aufgabenListe.add(fehlerZeile);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aufgabenAdapter = new AufgabenAdapter(MainActivity.this, aufgabenListe, MainActivity.this);
                            aufgabenView.setAdapter(aufgabenAdapter);
                        }
                    });

                    Log.d(TAG, "Error select Aufgaben: " + response.message());
                    return;
                }else{
                    try {
                        String resp = response.body().string();

                        JSONArray aufgabenArray = new JSONArray(resp);

                        for (int i = 0; i < aufgabenArray.length(); i++) {
                            JSONObject aufgObject = aufgabenArray.getJSONObject(i);

                            int id = aufgObject.getInt("id");
                            String ersteller = aufgObject.getString("ersteller");
                            String titel = aufgObject.getString("titel");
                            String beschreibung = aufgObject.getString("beschreibung");
                            int prio = aufgObject.getInt("prio");
                            String status = aufgObject.getString("status");
                            String erstellt = aufgObject.getString("erstellt");
                            String faellig = aufgObject.getString("faellig");
                            String zustaendig = aufgObject.getString("zustaendig");
                            String stichwort = aufgObject.getString("stichwort");

                            AufgabenZeile eineAufgabe = new AufgabenZeile(id, ersteller, titel, beschreibung,
                                    prio, status, erstellt, faellig, zustaendig, stichwort);
                            aufgabenListe.add(eineAufgabe);
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aufgabenAdapter = new AufgabenAdapter(MainActivity.this, aufgabenListe, MainActivity.this);
                            aufgabenView.setAdapter(aufgabenAdapter);
                        }
                    });

                }
            }
        });
        selAufgaben.connectionPool().evictAll();
    }
    ItemTouchHelper.SimpleCallback swipeHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    Log.d(TAG, "Position: " + pos);
                    deleteItem(pos);
                    aufgabenListe.remove(pos);
                    aufgabenView.getAdapter().notifyItemRemoved(pos);
                    break;
                case ItemTouchHelper.RIGHT:
                    //erledigt
                    setErledigt(pos);
                    aufgabenView.getAdapter().notifyItemChanged(pos);
                    break;
            }
        }
    };

    private void setErledigt(int pos) {
        int updID = aufgabenListe.get(pos).getId();

        OkHttpClient updAufgabe = new OkHttpClient();
        String updAufgabeUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/upd_aufgabe.php";

        RequestBody reqBody = new FormBody.Builder()
                .add("id", String.valueOf(aufgabenListe.get(pos).getId()))
                .add("titel", aufgabenListe.get(pos).getTitel())
                .add("beschreibung", aufgabenListe.get(pos).getBeschreibung())
                .add("prio", String.valueOf(aufgabenListe.get(pos).getPrio()))
                .add("status", "Erledigt")
                .add("faellig", aufgabenListe.get(pos).getFaellig())
                .add("zustaendig", aufgabenListe.get(pos).getZustaendig())
                .add("stichwort", aufgabenListe.get(pos).getStichwort())
                .build();

        Request request = new Request.Builder()
                .url(updAufgabeUrl)
                .post(reqBody)
                .build();

        updAufgabe.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Datenbankfehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String reqBody = response.body().string();

                if (!response.isSuccessful()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Fehler beim Update: " + response, Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    Log.d(TAG, "Response: " + response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Aufgabe erledigt", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        updAufgabe.connectionPool().evictAll();
    }

    private void deleteItem(int pos) {
        int delID = aufgabenListe.get(pos).getId();

        OkHttpClient delAufgabe = new OkHttpClient();
        String delAufgabeUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/del_aufgabe.php?id=" + delID;

        Request request = new Request.Builder()
                .url(delAufgabeUrl)
                .build();

        delAufgabe.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Datenbankfehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Fehler beim Löschen: " + response, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Aufgabe erfolgreich gelöscht", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        delAufgabe.connectionPool().evictAll();
    }

    @Override
    public void onAufgabeClick(AufgabenZeile aufgabenZeile) {

        Intent aufgabe = new Intent(MainActivity.this, AufgabeActivity.class);
        aufgabe.putExtra("id", aufgabenZeile.getId());
        aufgabe.putExtra("ersteller", aufgabenZeile.getErsteller());
        aufgabe.putExtra("titel", aufgabenZeile.getTitel());
        aufgabe.putExtra("beschreibung", aufgabenZeile.getBeschreibung());
        aufgabe.putExtra("prio", aufgabenZeile.getPrio());
        aufgabe.putExtra("status", aufgabenZeile.getStatus());
        aufgabe.putExtra("erstellt", aufgabenZeile.getErstellt());
        aufgabe.putExtra("faellig", aufgabenZeile.getFaellig());
        aufgabe.putExtra("zustaendig", aufgabenZeile.getZustaendig());
        aufgabe.putExtra("stichwort", aufgabenZeile.getStichwort());
        aufgabe.putExtra("bearbeitungsart", "update");
        startActivity(aufgabe);
        aufgabenAdapter.updateData(aufgabenListe);
    }
}