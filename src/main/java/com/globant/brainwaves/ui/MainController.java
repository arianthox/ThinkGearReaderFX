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
import com.globant.brainwaves.commons.model.*;
import com.globant.brainwaves.commons.utils.CommonUtil;
import com.google.gson.Gson;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import lombok.extern.java.Log;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Component
@Log
@FxmlView("main-form.fxml")
public class MainController implements Initializable {

    private static final Random RND = new Random();

    private static final int MAX_DATA_POINTS = 100;
    final ObservableList<LineChart.Series<Integer, Integer>> observableSeriesData = FXCollections.observableArrayList();
    final Map<String, LineChart.Series<Integer, Integer>> seriesMap = new TreeMap<>();

    final ObservableList<LineChart.Series<Integer, Integer>> observableSeriesChannelData = FXCollections.observableArrayList();
    final Map<String, LineChart.Series<Integer, Integer>> seriesChannelMap = new TreeMap<>();

    private final KafkaConsumer kafkaConsumer;
    private final transient Gson gson;
    private final transient ActorSystem system;
    @FXML
    private Button mainButton;

    @FXML
    private LineChart<Integer, Integer> lineChart;

    @FXML
    private LineChart<Integer, Integer> lineChannelChart;


    private AtomicInteger counter = new AtomicInteger(0);


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

    private void addRawGraphPoint(String series, int... values) {
        Platform.runLater(() -> {
            int position = counter.getAndIncrement();
            if (position >= MAX_DATA_POINTS) {
                seriesMap.values().stream().forEach(p->p.getData().clear());
                seriesChannelMap.values().stream().forEach(p->p.getData().clear());
                counter.set(0);
                position = 0;
            }
            for (int value : values) {
                seriesMap.get(series).getData().add(new LineChart.Data<>(position, value));
            }
        });

    }

    private void addChannelGraphPoint(ChannelPacket packet) {
        Platform.runLater(() -> {
            int position = counter.get();
            seriesChannelMap.get("delta").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getDelta()));
            seriesChannelMap.get("theta").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getTheta()));
            seriesChannelMap.get("lowAlpha").getData().add(new LineChart.Data<>(position,(int)packet.getEegPower().getLowAlpha()));
            seriesChannelMap.get("highAlpha").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getHighAlpha()));
            seriesChannelMap.get("lowBeta").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getLowBeta()));
            seriesChannelMap.get("highBeta").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getHighBeta()));
            seriesChannelMap.get("lowGamma").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getLowGamma()));
            seriesChannelMap.get("highGamma").getData().add(new LineChart.Data<>(position, (int)packet.getEegPower().getHighGamma()));

        });

    }

    private void registerSeries(String name, String key){
        LineChart.Series<Integer,Integer> s=new LineChart.Series<>();
        s.setName(name);

        seriesMap.put(key,s);
    }


    private void registerChannelSeries(String name, String key){
        LineChart.Series<Integer,Integer> s=new LineChart.Series<>();
        s.setName(name);
        seriesChannelMap.put(key,s);
    }

    private void enableLegendAction(LineChart<Integer, Integer> lineChart){
        for (Node n : lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (XYChart.Series<Integer, Integer> s : lineChart.getData()) {
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            li.getSymbol().setOnMouseClicked(me -> {
                                if (me.getButton() == MouseButton.PRIMARY) {
                                    s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line
                                    for (XYChart.Data<Integer, Integer> d : s.getData()) {
                                        if (d.getNode() != null) {
                                            d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                            d.setExtraValue("Prueba");
                                        }
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        registerSeries("BrainWave","raw");
        registerSeries("Blink","blink");
        registerSeries("Mental Effort","mentalEffort");
        registerSeries("Familiarity","familiarity");
        registerSeries("Attention","attention");
        registerSeries("Meditation","meditation");

        registerChannelSeries("Delta","delta");
        registerChannelSeries("Theta","theta");
        registerChannelSeries("LowAlpha","lowAlpha");
        registerChannelSeries("HighAlpha","highAlpha");
        registerChannelSeries("LowBeta","lowBeta");
        registerChannelSeries("HighBeta","highBeta");
        registerChannelSeries("LowGamma","lowGamma");
        registerChannelSeries("HighGamma","highGamma");






        log.info("Initializing Kafka Consumer");
        this.kafkaConsumer.consume(ConsumerID.THINK_GEAR_FX, TopicID.THINK_GEAR_READER, record -> {
            final Source<WavePacket, NotUsed> flow = Source.single(record)
                    .map(param -> gson.fromJson(param.value(), WavePacket.class))
                    .withAttributes(ActorAttributes.withSupervisionStrategy(CommonUtil.decider));
            final Sink<WavePacket, CompletionStage<Done>> sink = Sink.foreach(packet -> {
                log.info("Receiving Packet: " + packet.getPacket().getClass() + " - " + packet.getPacket().toString());
                if (packet.getPacket() instanceof BufferRawPacket) {
                    List<Double> signal = IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).asDoubleStream().boxed().collect(Collectors.toList());
                    Map.Entry<List<Double>, List<Double>> nsignal = NFourierTransform.computeFFT(signal);
                    log.info("SIGNAL ORIG:" + Collections.singletonList(signal).toString());
                    log.info("SIGNAL REAL:" + Collections.singletonList(nsignal.getKey()).toString());
                    log.info("SIGNAL IMAG:" + Collections.singletonList(nsignal.getValue()).toString());
                    IntStream.of(((BufferRawPacket) packet.getPacket()).getBufferRawEeg()).reduce((a, b) -> (a + b) / 2).stream().forEach(value -> addRawGraphPoint("raw", value));
                } else if (packet.getPacket() instanceof BlinkPacket) {
                    int value=((BlinkPacket)packet.getPacket()).getBlinkStrength();
                    addRawGraphPoint("blink",  value);
                } else if (packet.getPacket() instanceof MentalEffortPacket) {
                    double value=((MentalEffortPacket)packet.getPacket()).getMentalEffort();
                    addRawGraphPoint("mentalEffort", (int)value);
                } else if (packet.getPacket() instanceof FamiliarityPacket) {
                    double value=((FamiliarityPacket)packet.getPacket()).getFamiliarity();
                    addRawGraphPoint("familiarity", (int)value);
                } else if (packet.getPacket() instanceof ChannelPacket) {
                    int value1=((ChannelPacket)packet.getPacket()).getESense().getAttention();
                    addRawGraphPoint("attention",  value1);
                    int value2=((ChannelPacket)packet.getPacket()).getESense().getMeditation();
                    addRawGraphPoint("meditation", value2);

                    addChannelGraphPoint(((ChannelPacket)packet.getPacket()));

                }



            });

            return flow.runWith(sink, system);

        });

        observableSeriesData.addAll(seriesMap.entrySet().stream().map(p -> p.getValue()).collect(Collectors.toList()));
        lineChart.setData(observableSeriesData);
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        lineChart.setCache(true);



        observableSeriesChannelData.addAll(seriesChannelMap.entrySet().stream().map(p -> p.getValue()).collect(Collectors.toList()));
        lineChannelChart.setData(observableSeriesChannelData);
        lineChannelChart.setCreateSymbols(true);
        lineChannelChart.setAnimated(true);
        lineChannelChart.setCache(true);

        enableLegendAction(lineChart);
        enableLegendAction(lineChannelChart);



    }
}
