package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import serial.Serial;
import sql.DbUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Main extends Application {
    private DbUtil db;
    Scene startScene, sceneMonitor;
    //PhotoResistorFields
    private LineChart<Number,Number> chart;
    private XYChart.Series<Number,Number> luxSeries;
    private XYChart.Series<Number,Number> aveLuxSeries5;
    private XYChart.Series<Number,Number> aveLuxSeries10;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private final int tamMax=300; // 30'' / 0.1''/reading = 300 readings
    /*the maximum number of points in each of them will be 300, relative to the last 30 seconds. For
    the moving averages, they will be calculated based on periods of 5 seconds and 10 seconds.*/
    private final int tamAve5=50; // 5''
    private final int tamAve10=100; // 10''
    private final Serial serial=new Serial();
    private ChangeListener<String> listener;//functional interface
    private final BooleanProperty connection=new SimpleBooleanProperty(false);
    private Map<Long,Double> buff=new HashMap<>();
    private final int BUFFSIZE=300;
    private  final Node rootIcon=new ImageView(new Image("graph.png"));//Button();

    public void start(Stage primaryStage) throws Exception {

        //MainScene
        Label labelStartScene=new Label("Chose a scene");
        Button bMonitor=new Button("Monitor");
        bMonitor.setOnAction(event -> {
            primaryStage.setScene(sceneMonitor);
            startSerial();
        });
        VBox layoutStartScene=new VBox(20);
        layoutStartScene.getChildren().addAll(labelStartScene,bMonitor);
        startScene=new Scene(layoutStartScene,300,250);

        //*****************-monitorScene-*******************\\
        xAxis=new NumberAxis();
        xAxis.setLabel("Time");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        /*
        * For the xAxis to show formatted
            data, we’ll override the toString() method, so every long value in milliseconds will be written in
                HH:mm:ss format:
        * */
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            public String toString(Number t) {
                return new SimpleDateFormat("HH:mm:ss").format(new Date(t.longValue()));
            }
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        yAxis=new NumberAxis();
        chart=new LineChart<>(xAxis,yAxis);
        /*
        * setCreateSymbols(true)
        will plot a big white circle on every point of the series. For now we set it to false, so it won’t create a
        node on the data
        * */
        chart.setCreateSymbols(false);
        /*
        * setAnimated(false) avoids the animation of the chart after adding any new point.
        Because we are plotting points at high frequency, it’s better this way. For lower frequencies, on the
        other hand, we will set it to true to create a smooth transition effect
        * */
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setTitle("Monitor");
        luxSeries = new XYChart.Series<>();
        luxSeries.setName("Light Level (lux)");
        /*
        aveLuxSeries5 = new XYChart.Series<>();
        aveLuxSeries5.setName("Moving Average 5'' (lux)");
        aveLuxSeries10 = new XYChart.Series<>();
        aveLuxSeries10.setName("Moving Average 10'' (lux)");*/
        chart.getData().addAll(luxSeries);//,aveLuxSeries5,aveLuxSeries10);
        /*
        * a listener will be created to bind the presence of any new line on the serial port (corresponding
        to a reading of the sensor coming from the Arduino) with the action of adding this value as a new
        pair to the series. For that we set the x coordinate value as the time when we add the reading in
        milliseconds (on the chart it will be formatted to HH:mm:ss) and the y coordinate value is a double
        measurement of the level of light reported by the Arduino in the String t1. As the series data could
        grow indefinitely, to avoid memory issues and to keep track only of the data of the last 30 seconds,
        we set a tamMax value of 300 points and remove the first values of the series if we exceed that limit.
        * */
        listener=(ov, oldData, newData) -> {
            Platform.runLater(()->{
                     try{
                    String[] data=newData.split(Serial.SEPARATOR);

                    if(data.length==2){
                        long time=new Long(data[0]);
                        buff.put(new Long(data[0]),new Double(data[1]));
                        luxSeries.getData().add(new XYChart.Data(time,new Double(data[1])));
                        /*if(luxSeries.getData().size()>tamMax){
                            luxSeries.getData().remove(0);
                        }*/
                        /*if(luxSeries.getData().size()>=tamAve5){
                            aveLuxSeries5.getData().add(new XYChart.Data<>(time, average(tamAve5)));
                            if(aveLuxSeries5.getData().size()>tamMax){
                                aveLuxSeries5.getData().remove(0);
                            }
                        }
                        if(luxSeries.getData().size()>=tamAve10){
                            aveLuxSeries10.getData().add(new XYChart.Data<>(time, average(tamAve10)));
                            if(aveLuxSeries10.getData().size()>tamMax){
                                aveLuxSeries10.getData().remove(0);
                                               }
                        }*/
                        if(buff.size()==BUFFSIZE){
                            db=new DbUtil("jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC","root","root");
                            db.write(buff);
                            System.out.println("SQL");
                            buff.clear();
                        }
                    }
                } catch(NumberFormatException nfe){
                    System.out.println("NFE: "+newData+" "+nfe.toString());
                }
            });
        };
        /*
        * Observe that by using Platform.run later(), not only do we place the task of filling the series
        with the incoming data in the JavaFX thread, but also we give to the Scene graph the required time to
        render the chart.
        * */
        serial.getLine().addListener(listener);
        //******************- ArchiveMode-***************************\\

        //DIALOG\\

        CheckBoxTreeItem<String> rootItem=
                new CheckBoxTreeItem<>("Choose a graph");
        rootItem.setExpanded(true);
        CheckBoxTreeItem<String> temp=new CheckBoxTreeItem<>("Temperature");
        CheckBoxTreeItem<String> lux=new CheckBoxTreeItem<>("LightLevel");
        rootItem.getChildren().addAll(temp,lux);
        final TreeView treeView=new TreeView();
        treeView.setRoot(rootItem);
        treeView.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
        Dialog dialog=new Dialog();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.setTitle("pick a chart");
        dialog.setHeaderText("some text");
        StackPane dialogStack=new StackPane();
        dialogStack.getChildren().addAll(treeView);
        dialog.getDialogPane().setContent(dialogStack);

        //*********


        Button buttonArchive=new Button("ArchiveMode");
        buttonArchive.setStyle("-fx-border-radius: 50;");
        buttonArchive.setOnAction(event -> {
            luxSeries.getData().removeAll();
            System.out.println("buttonArchive");
            Map<Long,Double> map=new DbUtil("jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC","root","root").read();
            for(Map.Entry<Long,Double> entry:map.entrySet()){
            luxSeries.getData().add(new XYChart.Data<>(entry.getKey(),entry.getValue().doubleValue()));}
            System.out.println("sql has been completed");
            System.out.println(map);
        });

        DatePicker dpStartDate=new DatePicker();
        DatePicker dpEndDate=new DatePicker();


        Button bDialog=new Button("dialog");
        bDialog.setOnAction(event -> {dialog.show();});
        HBox hBox=new HBox(buttonArchive,dpStartDate,dpEndDate,bDialog);
        //************************
        BorderPane root = new BorderPane();
        StackPane stack =new StackPane();
        stack.setPadding(new Insets(5,0,5,0));
        stack.getChildren().add(chart);
        root.setCenter(stack);
        root.setTop(hBox);

        Label lbl=new Label("Not connected");
        connection.addListener((ov, b, b1)->lbl.setText(b1?
                "Connected to: "+serial.getPortName():"Not connected"));
        root.setBottom(lbl);
        root.setStyle("-fx-background-color: derive(goldenrod,60%); "
                + "-fx-font: 16 \"Courier New\";");
        sceneMonitor =new Scene(root,800,600);



        //primaryStage
        primaryStage.setTitle("ArduinoApp");
        primaryStage.setScene(startScene);
        primaryStage.show();
    }
    public void stop(){
        System.out.println("Closing serial port");
        serial.getLine().removeListener(listener);
        stopSerial();
    }
    private void startSerial(){
        serial.connect();
        connection.set(!serial.getPortName().isEmpty());
    }
    private void stopSerial(){
        if(connection.get()){
            serial.disconnect();
            connection.set(false);
        }
    }
    private double average(int tam){
        return luxSeries.getData()
                .stream()
                .skip(luxSeries.getData().size()-tam)
                .mapToDouble(d->d.getYValue().doubleValue())
                .sum()/tam;
    }
    public static void main(String[] args) {
launch(args);
    }
}
