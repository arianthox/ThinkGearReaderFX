package com.globant.brainwaves;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ThinkGearReaderApplication {

    public static void main(String[] args) {
        System.out.println("Starting SpringBoot");
        Application.launch(FXStarter.class, args);
    }
}
