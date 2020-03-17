package com.globant.brainwaves.ui;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.*;


@Component
@FxmlView("main-form.fxml")
public class MainController implements Initializable {

    private static final Random RND            = new Random();

//    @FXML
//    private Gauge  gauge31;

    private static final int MAX_DATA_POINTS = 50;
    private String xSeriesData = "";
    private XYChart.Series series1=null;

    private BlockingQueue<Number> dataQ1 = new ArrayBlockingQueue<>(1024);

    @FXML
    private Button mainButton;

    @FXML
    private CategoryAxis xAxis = new CategoryAxis();
    @FXML
    final NumberAxis yAxis = new NumberAxis();
    @FXML
    final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private ScheduledExecutorService scheduledExecutorService;

    public void initGraph(){
        if(series1==null) {
            //Graph Series
            series1 = new XYChart.Series<String, Number>();
            series1.setName("Data");
            lineChart.getData().add(series1);
            System.out.println("InitGraph");

            // setup a scheduled executor to periodically put data into the chart
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

            // put dummy data onto graph per second
            scheduledExecutorService.scheduleAtFixedRate(() -> {

                Platform.runLater(() -> {
                    Date now = new Date();
                    double value=RND.nextDouble()*1000-500;
                    series1.getData().add(new XYChart.Data<String,Number>(simpleDateFormat.format(now), value ));
                    System.out.println("Value:"+value);

                    if (series1.getData().size() > MAX_DATA_POINTS)
                        series1.getData().remove(0);

                });
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    @FXML
    private void buttonClicked() {

        System.out.println("Button clicked!");


        //gauge31.setValue(RND.nextDouble() * gauge31.getRange() + gauge31.getMinValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGraph();
    }
}
