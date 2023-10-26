package com.example.todo_liste;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class AufgabenAdapter extends RecyclerView.Adapter<AufgabenAdapter.AufgabenViewHolder>{

    private static final String TAG = "AufgabenAdapter";
    private Context context;
    private List<AufgabenZeile> aufgabenListe;
    private AufgabenInterface aufgInter;

    public AufgabenAdapter(Context context, List<AufgabenZeile> aufgabenListe, AufgabenInterface aufgabenInterface) {
        this.context = context;
        this.aufgabenListe = aufgabenListe;
        this.aufgInter = aufgabenInterface;
    }

    @NonNull
    @Override
    public AufgabenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.aufgaben_zeile, parent, false);
        return new AufgabenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AufgabenViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AufgabenZeile zeile = aufgabenListe.get(position);

        holder.aufgZeile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aufgInter.onAufgabeClick(aufgabenListe.get(position));
            }
        });
        holder.editTitel.setText(zeile.getTitel());
        holder.editPrio.setText(String.valueOf(zeile.getPrio()));
        holder.editFaellig.setText(zeile.getFaellig());
        holder.editZustaendig.setText(zeile.getZustaendig());
        holder.editErstellt.setText(zeile.getErstellt());
        holder.editStatus.setText(zeile.getStatus());
    }

    @Override
    public int getItemCount() {
        return aufgabenListe.size();
    }

    public void updateData(List<AufgabenZeile> newData) {
        aufgabenListe.clear();
        aufgabenListe.addAll(newData);
        notifyDataSetChanged();
    }

    public void List(List<AufgabenZeile> sortedAufgabenListe){
        this.aufgabenListe = sortedAufgabenListe;
        notifyDataSetChanged();
    }

   public static class AufgabenViewHolder extends RecyclerView.ViewHolder {
        public CardView aufgZeile;
        TextView editTitel, editPrio, editFaellig, editZustaendig, editErstellt, editStatus;
        public AufgabenViewHolder(@NonNull View itemView) {
            super(itemView);
            aufgZeile = itemView.findViewById(R.id.aufgZeile);
            editTitel = itemView.findViewById(R.id.textTitel);
            editPrio = itemView.findViewById(R.id.textPrio);
            editFaellig = itemView.findViewById(R.id.textFaellig);
            editZustaendig = itemView.findViewById(R.id.textZustaendig);
            editErstellt = itemView.findViewById(R.id.textErstellt);
            editStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
