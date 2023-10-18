package com.example.todo_liste;

import java.util.Date;

public class AufgabenZeile {
    String ersteller, titel, beschreibung, status, erstellt, faellig, zustaendig, stichwort;
    int id, prio;

    public AufgabenZeile(int id, String ersteller, String titel, String berschreibung,
                         int prio, String status, String erstellt, String faellig, String zustaendig,
                         String stichwort) {
        this.id = id;
        this.ersteller = ersteller;
        this.titel = titel;
        this.beschreibung = berschreibung;
        this.prio = prio;
        this.status = status;
        this.erstellt = erstellt;
        this.faellig = faellig;
        this.zustaendig = zustaendig;
        this.stichwort = stichwort;
    }

    public String getErsteller() {
        return ersteller;
    }

    public String getTitel() {
        return titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public String getStatus() {
        return status;
    }

    public String getErstellt() {
        return erstellt;
    }

    public String getFaellig() {
        return faellig;
    }

    public String getZustaendig() {
        return zustaendig;
    }

    public String getStichwort() {
        return stichwort;
    }

    public int getId() {
        return id;
    }

    public int getPrio() {
        return prio;
    }
}
