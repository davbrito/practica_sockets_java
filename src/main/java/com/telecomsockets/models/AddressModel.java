package com.telecomsockets.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AddressModel {
    static String[] words = {"pablo", "maria", "juan", "luis", "ana", "carlos", "sofia", "david",
            "laura", "jose", "marta", "javier", "elena", "andres", "lucia", "pedro", "isabel",
            "jorge", "paula", "victor", "carla", "alberto", "silvia", "daniel", "teresa", "manuel",
            "cristina", "raul", "veronica", "gustavo", "patricia", "oscar", "natalia", "martin",
            "beatriz", "roberto", "carmen", "alejandro", "fernando", "mariajose", "alicia",
            "ricardo", "gabriela", "claudia", "sandra", "arturo", "monica", "alejandra",
            "mariaelena", "josefina", "francisco", "liliana", "marcos", "patricio", "martina",
            "cristian",};

    public SimpleStringProperty name = new SimpleStringProperty(generateRandomName());
    public SimpleIntegerProperty port = new SimpleIntegerProperty(8080);
    public SimpleStringProperty ip = new SimpleStringProperty("localhost");


    private static String generateRandomName() {
        return words[(int) (Math.random() * words.length)] + "-"
                + words[(int) (Math.random() * words.length)];
    }
}
