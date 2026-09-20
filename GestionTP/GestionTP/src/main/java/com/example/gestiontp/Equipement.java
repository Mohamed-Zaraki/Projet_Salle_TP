package com.example.gestiontp;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Equipement {
    private  StringProperty code;
    private  StringProperty marque;
    private  StringProperty ram;
    private  StringProperty se;
    private  StringProperty disque;
    private  StringProperty cpu;
    private  StringProperty salleId = new SimpleStringProperty("");

    public Equipement(String code, String marque, String ram, String se, String disque, String cpu) {
        this.code = new SimpleStringProperty(code);
        this.marque = new SimpleStringProperty(marque);
        this.ram = new SimpleStringProperty(ram);
        this.se = new SimpleStringProperty(se);
        this.disque = new SimpleStringProperty(disque);
        this.cpu = new SimpleStringProperty(cpu);
    }
    
    private boolean isNew = true;
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    public Equipement() {}

    // Code property
    public StringProperty codeProperty() {
        return code;
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    // Marque property
    public StringProperty marqueProperty() {
        return marque;
    }

    public String getMarque() {
        return marque.get();
    }

    public void setMarque(String marque) {
        this.marque.set(marque);
    }

    // RAM property
    public StringProperty ramProperty() {
        return ram;
    }

    public String getRam() {
        return ram.get();
    }

    public void setRam(String ram) {
        this.ram.set(ram);
    }

    // SE property
    public StringProperty seProperty() {
        return se;
    }

    public String getSe() {
        return se.get();
    }

    public void setSe(String se) {
        this.se.set(se);
    }

    // Disque property
    public StringProperty disqueProperty() {
        return disque;
    }

    public String getDisque() {
        return disque.get();
    }

    public void setDisque(String disque) {
        this.disque.set(disque);
    }

    // CPU property
    public StringProperty cpuProperty() {
        return cpu;
    }

    public String getCpu() {
        return cpu.get();
    }

    public void setCpu(String cpu) {
        this.cpu.set(cpu);
    }
    public StringProperty salleIdProperty() { return salleId; }
    public void setSalleId(String salleId) { this.salleId.set(salleId); }
}