package com.example.todo_liste;

import java.util.Date;

public class AufgabenZeile {
    String titel, faellig;
    int prio;

    public AufgabenZeile(String titel, int prio, String faellig) {
        this.titel = titel;
        this.prio = prio;
        this.faellig = faellig;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public String getFaellig() {
        return faellig;
    }

    public void setFaellig(String faellig) {
        this.faellig = faellig;
    }
}
