package sql;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.SimpleDateFormat;
import java.util.*;

public class FX extends Application {
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private XYChart.Series<Number,Number> series;
    private LineChart<Number,Number> chart;
    private StackPane stackPane;
    private Scene mainScene;
    public void start(Stage primaryStage) throws Exception {
        /////////////////////////////////
        xAxis=new NumberAxis();
        xAxis.setLabel("xAxis");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            public String toString(Number t) {
                return new SimpleDateFormat("HH:mm:ss").format(new Date(t.longValue()));
            }
            public Number fromString(String string) {
                throw new UnsupportedOperationException("not supported yet");
            }
        });
        yAxis=new NumberAxis();
        yAxis.setLabel("yAxis");
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        series=new XYChart.Series();
        series.setName("seriesName");
        for (int i = 0; i <100 ; i++) {
            series.getData().addAll(new XYChart.Data<>(i,i*1.55));
        }
        ////////////////////////////////
        chart=new LineChart<>(xAxis,yAxis);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setTitle("chartTitle");
        chart.getData().addAll(series);
        //chart.getData().remove(series);
        ///////////////////////////////
        stackPane=new StackPane();
        stackPane.setPadding(new Insets(10,30,10,10));
        stackPane.getChildren().addAll(chart);
        ///////////////////////////////
        Button button=new Button("Button");
        button.setOnAction(event -> {
            System.out.println("bAction");
            chart.getData().removeAll();
            for(Map.Entry<Long,Double> entry:new DbUtil("jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC","root","root").read().entrySet()){
                series.getData().add(new XYChart.Data<>(entry.getKey(),entry.getValue().doubleValue()));}
                   });
        VBox vBox=new VBox(button);
        TextField tfStartDate=new TextField();
        TextField tfEndDate=new TextField();
        Button buttonArchive=new Button("Archive");
        HBox hBox=new HBox(tfStartDate,tfEndDate,buttonArchive);
        //////////////////////////////
        BorderPane borderPane=new BorderPane();
        borderPane.setCenter(stackPane);
        borderPane.setLeft(vBox);
        borderPane.setTop(hBox);
        borderPane.setStyle("-fx-background-color: derive(goldenrod,60%); "
                + "-fx-font: 16 \"Courier New\";");
        mainScene=new Scene(borderPane,500,300);
        //***********************
        primaryStage.setTitle("TITLE");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
