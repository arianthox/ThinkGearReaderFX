package com.globant.brainwaves;

import javafx.application.Application;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ThinkGearReaderApplication {

    public static void main(String[] args) {
        System.out.println("Starting SpringBoot");
        Application.launch(FXStarter.class, args);
    }

    @Bean
    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {

        return new SpringFxWeaver(applicationContext); //(2)
    }

}
