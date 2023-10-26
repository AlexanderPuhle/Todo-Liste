package com.example.todo_liste;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AufgabeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = "AufgabeActivity";
    private EditText editTitel, editPrio, editFaellig, editStichwort, editBeschreibung;
    private int id;
    private TextView txtErsteller, txtErstellt;
    private String detailURL = "";
    private String bearbeitungsart;
    private String selectedStatus;
    private String selectedMitarbeiter;

    ArrayList<String> statusListe;

    ArrayList<String> zustaendigListe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe);

        //GUI-Objekte initialisieren
        editTitel = findViewById(R.id.editTitel);
        editPrio = findViewById(R.id.editPrio);
        editFaellig = findViewById(R.id.editFaelligDt);
        Spinner selectMitarbeiter = findViewById(R.id.selectMitarbeiter);
        editStichwort = findViewById(R.id.editStichwort);
        Spinner selectStatus = findViewById(R.id.selectStatus);
        editBeschreibung = findViewById(R.id.editBeschreibung);
        txtErstellt = findViewById(R.id.textErstelltDt);
        txtErsteller = findViewById(R.id.textErsteller);

        Button btnAbbruch = findViewById(R.id.btnAbbr);
        Button btnSichern = findViewById(R.id.btnSichern);

        //ArrayListen für Spinner Status und Zuständig initialisieren
        statusListe = new ArrayList<>();
        zustaendigListe = new ArrayList<>();

        //Klick-Aktion für Buttons setzen
        btnAbbruch.setOnClickListener(view -> {
            AufgabeActivity.super.onBackPressed();
            goBack();
        });

        btnSichern.setOnClickListener(v -> {
            String check = "true";
            if (bearbeitungsart.equals("update")) {
                detailURL = "upd_aufgabe.php";
            }else{
                String checkTitel = editTitel.getText().toString();
                if (checkTitel.isEmpty()){
                    runOnUiThread(() -> Toast.makeText(AufgabeActivity.this,
                            "Titel muss gefüllt sein", Toast.LENGTH_LONG).show());
                    check = "false";
                }
                detailURL = "ins_aufgabe.php";
            }
            if (check.equals("true")){
                datenSichern();
                goBack();
            }
        });

        //Inhalt des Spinners Status setzen
        ArrayAdapter<CharSequence> adatperStatusListe = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statusListe);
        adatperStatusListe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Inhalt des Spinners Zuständig setzen
        ArrayAdapter<CharSequence> adapterZustaendigListe = new ArrayAdapter(this, android.R.layout.simple_spinner_item, zustaendigListe);
        adapterZustaendigListe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        getMitarbeiter();

        Intent intent = getIntent();
        Bundle inhalt = intent.getExtras();

        //Belegung der GUI-Objekte abhängig der Bearbeitungsart (Update oder Insert)
        bearbeitungsart = inhalt.getString("bearbeitungsart");

        if (bearbeitungsart.equals("update")) {
            id = inhalt.getInt("id");

            editTitel.setText(inhalt.getString("titel"));
            editTitel.setEnabled(false);
            editPrio.setText(String.valueOf(inhalt.getInt("prio")));
            editFaellig.setText(inhalt.getString("faellig"));
            editStichwort.setText(inhalt.getString("stichwort"));
            editBeschreibung.setText(inhalt.getString("beschreibung"));
            txtErsteller.setText(inhalt.getString("ersteller"));
            txtErstellt.setText(inhalt.getString("erstellt"));
            String aktStat = inhalt.getString("status");
            String aktMitarbeiter = inhalt.getString("zustaendig");
            //Bei Update werden beide Spinner mit dem aktuellen Eintrag der Tabelle belegt
            statusListe.add("aktuell: " + aktStat);
            zustaendigListe.add("aktuell: " + aktMitarbeiter);

        }else{
            editTitel.setEnabled(true);
            Calendar kal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String aktDate = dateFormat.format(kal.getTime());
            txtErstellt.setText(aktDate);
            txtErsteller.setText(inhalt.getString("ersteller"));
            //Bei Insert wird der erste Eintrag beider Spinner mit Leer (Space) belegt
            statusListe.add("");
            zustaendigListe.add("");
        }

        //Restliche Belegung des Spinners Status ist unabhängig der Bearbeitungsart
        statusListe.add("Offen");
        statusListe.add("In Bearbeitung");

        //Spinner mit ermittelter Liste verknüpfen
        selectStatus.setAdapter(adatperStatusListe);
        selectStatus.setOnItemSelectedListener(this);

        selectMitarbeiter.setAdapter(adapterZustaendigListe);
        selectMitarbeiter.setOnItemSelectedListener(this);
    }

    private void getMitarbeiter() {
        OkHttpClient selMitarbeiter = new OkHttpClient();
        String getMitarbeiterUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/sel_kollegen.php";

        Request requestMit = new Request.Builder().url(getMitarbeiterUrl).build();

        selMitarbeiter.newCall(requestMit).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "Request fehler: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()){
                    Log.d(TAG, "Sql fehlerhaft: " + response.body().string());
                }else{
                    String resp = response.body().string();

                    try {
                        JSONArray mitarbeiterArray = new JSONArray(resp);
                        String name;

                        for (int i = 0; i < mitarbeiterArray.length(); i++) {
                            JSONObject kollegeObject = mitarbeiterArray.getJSONObject(i);
                            name = kollegeObject.getString("name") + ", " + kollegeObject.getString("vorname");
                            zustaendigListe.add(name);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        selMitarbeiter.connectionPool().evictAll();
    }

    private void goBack() {
        Intent main = new Intent(AufgabeActivity.this, MainActivity.class);
        finish();
        startActivity(main);
    }

    private void datenSichern() {
        String strPrio = editPrio.getText().toString();
        String ersteller, titel, beschreibung, status, erstellt, faellig, zustaendig, stichwort;

        int prio;
        if (strPrio.equals("")) {
            prio = 0;
        }else {
            prio = Integer.parseInt(strPrio);
        }

        ersteller = txtErsteller.getText().toString();
        titel = editTitel.getText().toString();
        beschreibung = editBeschreibung.getText().toString();
        status = selectedStatus;
        erstellt = txtErstellt.getText().toString();
        faellig = editFaellig.getText().toString();
        zustaendig = selectedMitarbeiter;
        stichwort = editStichwort.getText().toString();

        OkHttpClient client = new OkHttpClient();
        String baseUrl = "https://qu-iu-zz.beyer-its.de/TodoListe/"+detailURL;

        RequestBody reqBody = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("ersteller", ersteller)
                .add("titel", titel)
                .add("beschreibung", beschreibung)
                .add("prio", String.valueOf(prio))
                .add("status", status)
                .add("erstellt", erstellt)
                .add("faellig", faellig)
                .add("zustaendig", zustaendig)
                .add("stichwort", stichwort)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl)
                .post(reqBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AufgabeActivity.this, "Datenbankfehler: " +
                        e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()){
                    runOnUiThread(() -> Toast.makeText(AufgabeActivity.this,
                            "Fehler beim Aufnehmen: " + response, Toast.LENGTH_LONG).show());
                }else{
                    runOnUiThread(() -> Toast.makeText(AufgabeActivity.this, "Aufgabe aufgenommen",
                            Toast.LENGTH_LONG).show());
                }
            }
        });
        client.connectionPool().evictAll();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.selectStatus){
            selectedStatus = parent.getItemAtPosition(position).toString();
        } else if (parent.getId() == R.id.selectMitarbeiter){
            selectedMitarbeiter = parent.getItemAtPosition(position).toString();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}