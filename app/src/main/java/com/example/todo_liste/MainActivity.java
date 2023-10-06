package com.example.todo_liste;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    RecyclerView aufgabenView;
    AufgabenAdapter aufgabenAdapter;
    List<AufgabenZeile> aufgabenListe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aufgabenView = findViewById(R.id.todoListe);
        //aufgabenView.setHasFixedSize(true);
        aufgabenView.setAdapter(aufgabenAdapter);
        aufgabenView.setLayoutManager(new LinearLayoutManager(this));
        aufgabenListe = new ArrayList<>();

        getAufgaben();
    }

    private void getAufgaben() {
        OkHttpClient selAufgaben = new OkHttpClient();
        String getAufgabenUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/sel_aufgaben.php";

        Request request = new Request.Builder().url(getAufgabenUrl).build();

        selAufgaben.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Erste Zeile soll fehlermeldung ausgeben
                Log.d(TAG, "Error Request: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()) {
                    //Erste Zeile soll Fehlermeldung ausgeben
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

                            AufgabenZeile eineAufgabe = new AufgabenZeile(titel, prio, faellig);
                            aufgabenListe.add(eineAufgabe);
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aufgabenAdapter = new AufgabenAdapter(MainActivity.this, aufgabenListe);
                            Log.d(TAG, "aufgabenAdapter gesetzt");
                            aufgabenView.setAdapter(aufgabenAdapter);
                            Log.d(TAG, "AufgabenView gesetzt");
                        }
                    });

                }
            }
        });
    }
}