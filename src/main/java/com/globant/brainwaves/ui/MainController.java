package com.globant.brainwaves.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@FxmlView("main-form.fxml")
public class MainController implements Initializable {

    private static final Random RND = new Random();

    private static final int MAX_DATA_POINTS = 50;

    @FXML
    private Button mainButton;

    @FXML
    private CategoryAxis yAxis;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private LineChart<String, Double> lineChart;

    private AtomicInteger counter=new AtomicInteger(0);

    private ScheduledExecutorService scheduledExecutorService;

    public void initGraph() {
        ObservableList<LineChart.Series<String, Double>> observableSerieData = FXCollections.observableArrayList();
        LineChart.Series<String, Double> seriesData = new LineChart.Series<String, Double>();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                double value = RND.nextDouble() * 1000 - 500;
                seriesData.getData().add(new LineChart.Data<String, Double>("" +counter.incrementAndGet(), value));
                if (seriesData.getData().size() > MAX_DATA_POINTS)
                    seriesData.getData().remove(0);
            });
        }, 10,100, TimeUnit.MILLISECONDS);

        observableSerieData.add(seriesData);
        lineChart.setData(observableSerieData);

    }

    @FXML
    private void buttonClicked() {
        System.out.println("Button clicked!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGraph();
    }
}
