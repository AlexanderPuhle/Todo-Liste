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

    public void setErsteller(String ersteller) {
        this.ersteller = ersteller;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErstellt() {
        return erstellt;
    }

    public void setErstellt(String erstellt) {
        this.erstellt = erstellt;
    }

    public String getFaellig() {
        return faellig;
    }

    public void setFaellig(String faellig) {
        this.faellig = faellig;
    }

    public String getZustaendig() {
        return zustaendig;
    }

    public void setZustaendig(String zustaendig) {
        this.zustaendig = zustaendig;
    }

    public String getStichwort() {
        return stichwort;
    }

    public void setStichwort(String stichwort) {
        this.stichwort = stichwort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }
}
