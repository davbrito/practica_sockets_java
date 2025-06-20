package com.telecomsockets.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AddressModel {

    public SimpleStringProperty name = new SimpleStringProperty(this, "name", "");
    public SimpleIntegerProperty port = new SimpleIntegerProperty(this, "port", 8080);
    public SimpleStringProperty ip = new SimpleStringProperty(this, "ip", "localhost");

    public String toString() {
        return String.format("AddressModel[name=%s, port=%d, ip=%s]", name.get(), port.get(), ip.get());
    }

    static String[] words = { "pablo", "maria", "juan", "luis", "ana", "carlos", "sofia", "david", "laura", "jose",
            "marta", "javier", "elena", "andres", "lucia", "pedro", "isabel", "jorge", "paula", "victor", "carla",
            "alberto", "silvia", "daniel", "teresa", "manuel", "cristina", "raul", "veronica", "gustavo", "patricia",
            "oscar", "natalia", "martin", "beatriz", "roberto", "carmen", "alejandro", "fernando", "mariajose",
            "alicia", "ricardo", "gabriela", "claudia", "sandra", "arturo", "monica", "alejandra", "mariaelena",
            "josefina", "francisco", "liliana", "marcos", "patricio", "martina", "cristian", };

    public static String generateRandomName() {
        return words[(int) (Math.random() * words.length)] + "-" + words[(int) (Math.random() * words.length)];
    }

}
