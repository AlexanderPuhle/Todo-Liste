package com.example.todo_liste;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class AufgabenAdapter extends RecyclerView.Adapter<AufgabenAdapter.AufgabenViewHolder>{

    private Context context;
    private List<AufgabenZeile> aufgabenListe;

    public AufgabenAdapter(Context context, List<AufgabenZeile> aufgabenListe) {
        this.context = context;
        this.aufgabenListe = aufgabenListe;
        Log.d("Adapter", "aufgabenListe: " + aufgabenListe);
    }

    @NonNull
    @Override
    public AufgabenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater inflater = LayoutInflater.from(context);
        //View view = inflater.inflate(R.layout.aufgaben_zeile, null);
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.aufgaben_zeile, parent, false);
        return new AufgabenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AufgabenViewHolder holder, int position) {
        AufgabenZeile zeile = aufgabenListe.get(position);

        Log.d("Adapter", "zeile: " + zeile.getTitel());
        holder.editTitel.setText(zeile.getTitel());
        holder.editPrio.setText(String.valueOf(zeile.getPrio()));
        holder.editFaellig.setText(zeile.getFaellig());
    }

    @Override
    public int getItemCount() {
        return aufgabenListe.size();
    }

    class AufgabenViewHolder extends RecyclerView.ViewHolder {

        TextView editTitel, editPrio, editFaellig;
        public AufgabenViewHolder(@NonNull View itemView) {
            super(itemView);

            editTitel = itemView.findViewById(R.id.textTitel);
            editPrio = itemView.findViewById(R.id.textPrio);
            editFaellig = itemView.findViewById(R.id.textFaellig);
        }
    }
}
