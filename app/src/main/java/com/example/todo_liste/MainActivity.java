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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements AufgabenInterface, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    RecyclerView aufgabenView;
    private AufgabenAdapter aufgabenAdapter;
    List<AufgabenZeile> aufgabenListe;
    List<AufgabenZeile> aufgabenListeAlt;
    private String ersteller, sortierenNach;
    private int lauf = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner sort = findViewById(R.id.spinnerSort);
        aufgabenView = findViewById(R.id.todoListe);
        aufgabenView.setAdapter(aufgabenAdapter);
        aufgabenView.setLayoutManager(new LinearLayoutManager(this));
        aufgabenListe = new ArrayList<>();
        aufgabenListeAlt = new ArrayList<>();

        ArrayAdapter<CharSequence>sortAdapter = ArrayAdapter.createFromResource(this, R.array.sortierung, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Laden aller Aufgaen in die RecyclerView
        getAufgaben();

        //Löschen oder erledigt setzen von Elementen in der RecyclerView
        ItemTouchHelper itemSwipeHelper = new ItemTouchHelper(swipeHelper);
        itemSwipeHelper.attachToRecyclerView(aufgabenView);

        Button addAufgabe = findViewById(R.id.btnAddAufgabe);
        getZustaendig();
        addAufgabe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent addAufgabe = new Intent(MainActivity.this, AufgabeActivity.class);
                addAufgabe.putExtra("bearbeitungsart", "insert");
                addAufgabe.putExtra("ersteller", ersteller);
                finish();
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

        sort.setAdapter(sortAdapter);
        sort.setOnItemSelectedListener(this);
    }

    private void getZustaendig() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userMail = user.getEmail();

        OkHttpClient clientZustaendig = new OkHttpClient();
        String getZustaendig = "https://qu-iu-zz.beyer-its.de/TodoListe/sel_kollegen.php?email="+userMail;

        Request requestZustaendig = new Request.Builder().url(getZustaendig).build();

        clientZustaendig.newCall(requestZustaendig).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "SQL-Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                Log.d(TAG, "Error Request: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Fehler ermitteln Zuständiger: " + response.body(), Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.d(TAG, "Fehler ermitteln Zuständiger: " + response.body().string());
                }else{
                    String resp = response.body().string();
                    try {
                        JSONArray mitarbeiterArray = new JSONArray(resp);

                        for (int i = 0; i < mitarbeiterArray.length(); i++) {
                            JSONObject zustaendigObject = mitarbeiterArray.getJSONObject(i);
                            ersteller = zustaendigObject.getString("name") + ", " + zustaendigObject.getString("vorname");
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
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

                //Aktuelle Liste für Sortierung und Filterung sichern
                aufgabenListeAlt = aufgabenListe;
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
                    deleteItem(pos);
                    aufgabenListe.remove(pos);
                    aufgabenView.getAdapter().notifyItemRemoved(pos);
                    break;
                case ItemTouchHelper.RIGHT:
                    //erledigt
                    setErledigt(pos);
                    aufgabenListe.get(pos).setStatus("Erledigt");
                    aufgabenView.getAdapter().notifyItemChanged(pos);
                    break;
            }
        }
    };

    private void setErledigt(int pos) {
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

                if (!response.isSuccessful()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Fehler beim Update: " + response, Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sortierenNach = parent.getItemAtPosition(position).toString();
        List<AufgabenZeile> filterdAufgaben = new ArrayList<>();

        if (sortierenNach.isEmpty() && lauf == 0){
            lauf = 1;
            return;
        }

        if (sortierenNach.equals("Eigene offene Aufgaben")){
            String searchErsteller = ersteller.toLowerCase().trim();

            for (AufgabenZeile zeile : aufgabenListe){
                String filterZustaendig = zeile.getZustaendig().toLowerCase().trim();
                String filterErsteller = zeile.getErsteller().toLowerCase().trim();

                if (zeile.getStatus().equals("Offen")){
                    if (searchErsteller.equals(filterZustaendig)){
                        filterdAufgaben.add(zeile);
                    }else if (filterZustaendig.isEmpty()
                          && searchErsteller.equals(filterErsteller)) {
                        filterdAufgaben.add(zeile);
                    }
                }
            }
        }else{
            for (AufgabenZeile zeile : aufgabenListeAlt){
                filterdAufgaben.add(zeile);
            }
        }

        aufgabenListe = filterdAufgaben;

        Collections.sort(aufgabenListe, new Comparator<AufgabenZeile>() {

            @Override
            public int compare(AufgabenZeile o1, AufgabenZeile o2) {
                switch (sortierenNach){
                    case "Eigene offene Aufgaben":
                        return o1.faellig.compareToIgnoreCase(o2.faellig);
                    case "Zuständig":
                        return o1.zustaendig.compareToIgnoreCase(o2.zustaendig);
                    case "Fälligkeit":
                        return o1.faellig.compareToIgnoreCase(o2.faellig);
                    case "Erstellt am":
                        return o1.erstellt.compareToIgnoreCase(o2.erstellt);
                    case "Status":
                        return o1.erstellt.compareToIgnoreCase(o2.erstellt);
                    case "Priorität":
                        return (String.valueOf(o1.prio)).compareToIgnoreCase(String.valueOf(o2.prio));
                    default:
                        return (String.valueOf(o1.id)).compareToIgnoreCase(String.valueOf(o2.id));
                }
            }
        });
        aufgabenAdapter.List(aufgabenListe);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}