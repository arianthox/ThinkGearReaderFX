package com.globant.brainwaves;

import com.globant.brainwaves.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


public class FXStarter extends Application {


    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        applicationContext = new SpringApplicationBuilder()
                .sources(ThinkGearReaderApplication.class)
                .run(args);
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }

    @Override
    public void start(Stage stage) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(MainController.class);

        stage.setTitle(applicationContext.getEnvironment().getProperty("spring.application.name"));
        stage.setScene(new Scene(root, 950, 500));
        stage.show();
    }

}