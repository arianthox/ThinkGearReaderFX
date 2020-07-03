package com.globant.brainwaves.ui;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ActorAttributes;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.globant.brainwaves.commons.adapter.KafkaConsumer;
import com.globant.brainwaves.commons.hhm.dsp.NFourierTransform;
import com.globant.brainwaves.commons.model.BufferRawPacket;
import com.globant.brainwaves.commons.model.ConsumerID;
import com.globant.brainwaves.commons.model.TopicID;
import com.globant.brainwaves.commons.model.WavePacket;
import com.globant.brainwaves.commons.utils.CommonUtil;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import lombok.extern.java.Log;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Component
@Log
@FxmlView("main-form.fxml")
public class MainController implements Initializable {

    private static final Random RND = new Random();

    private static final int MAX_DATA_POINTS = 100;

    @FXML
    private Button mainButton;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private LineChart<Integer, Integer> lineChart;

    private final KafkaConsumer kafkaConsumer;

    private final transient Gson gson;

    private final transient ActorSystem system;

    private AtomicInteger counter = new AtomicInteger(0);

    private ScheduledExecutorService scheduledExecutorService;

    public MainController(KafkaConsumer kafkaConsumer, Gson gson) {
        this.kafkaConsumer = kafkaConsumer;
        this.gson = gson;
        system = ActorSystem.create(Behaviors.empty(), "think-gear-fx-system");
    }



    @PostConstruct
    private void initialize() {

    }


    @FXML
    private void buttonClicked() {
        System.out.println("Button clicked!");
    }


    private void addGraphPoint( int value){
        Platform.runLater(() -> {
            int position = counter.getAndIncrement();
            if (position >= MAX_DATA_POINTS) {
                seriesData.getData().clear();
                counter.set(0);
                position=0;
            }
            seriesData.getData().add(new LineChart.Data<>(position, value));
        });

    }

    final ObservableList<LineChart.Series<Integer, Integer>> observableSeriesData = FXCollections.observableArrayList();
    final LineChart.Series<Integer, Integer> seriesData = new LineChart.Series<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        log.info("Initializing Kafka Consumer");
        this.kafkaConsumer.consume(ConsumerID.THINK_GEAR_FX, TopicID.THINK_GEAR_READER, record -> {
            final Source<WavePacket, NotUsed> flow = Source.single(record)
                    .map(param -> gson.fromJson(param.value(), WavePacket.class))
                    .filter(param -> param.getPacket() instanceof BufferRawPacket)
                    .withAttributes(ActorAttributes.withSupervisionStrategy(CommonUtil.decider));
            final Sink<WavePacket, CompletionStage<Done>> sink = Sink.foreach(packet -> {
                log.info("Receiving Packet: "+packet.getPacket().getClass()+" - "+packet.getPacket().toString());


                    try {
                        List<Double> signal=IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).asDoubleStream().boxed().collect(Collectors.toList());
                        Map.Entry<List<Double>, List<Double>> nsignal = NFourierTransform.computeFFT(signal);
                        log.info("SIGNAL ORIG:" + Collections.singletonList(signal).toString());
                        log.info("SIGNAL REAL:" + Collections.singletonList(nsignal.getKey()).toString());
                        log.info("SIGNAL IMAG:" + Collections.singletonList(nsignal.getValue()).toString());
                        IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).reduce((a, b) -> (a + b) / 2).stream().forEach(value -> addGraphPoint(value));
                    }catch(Exception ex){
                        log.severe(ex.getMessage());
                        ex.printStackTrace();
                    }


//                OptionalDouble average=IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).average();
//                List<Integer> values = IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).filter(value -> value > average.getAsDouble() * 1.5 || value < average.getAsDouble() * 0.5).boxed().collect(Collectors.toList());
//                addGraphPoint(values);

//                //


            });

            return flow.runWith(sink, system);

        });
        observableSeriesData.add(seriesData);
        lineChart.setData(observableSeriesData);
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        lineChart.setCache(true);



//        String color = "Orange";
//        String lineColor  = "-fx-stroke: " + color + ";";
//
//        Set<Node> lineNode = lineChart.lookupAll(".series0");
//        for (final Node line : lineNode) {
//            line.setStyle(lineColor);
//        }
    }
}
