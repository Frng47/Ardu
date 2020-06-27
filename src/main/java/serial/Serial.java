package serial;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class Serial {
    private static final List<String> USUAL_PORTS = Arrays.asList(
            "/dev/tty.usbmodem", "/dev/tty.usbserial", // Mac OS X
            "/dev/usbdev","/dev/ttyUSB","/dev/ttyACM", "/dev/serial", // Linux
            "COM3","COM4","COM5","COM6" // Windows
    );
    private final String ardPort;
    private SerialPort serPort;
    public static final String SEPARATOR= ";";
    private StringBuilder sb=new StringBuilder();
    private final StringProperty line=new SimpleStringProperty("");
    SimpleDateFormat sdf=new SimpleDateFormat("d.MM 'at' kk:mm:ss");

    public Serial(){
        ardPort="";
    }
    public Serial(String port){
        ardPort=port;
    }



    public boolean connect(){
        Arrays.asList(SerialPortList.getPortNames())
                .stream()
                .filter(name->
                        ((!ardPort.isEmpty() && name.equals(ardPort)) ||
                                (ardPort.isEmpty() &&
                                        USUAL_PORTS.stream()
                                                .anyMatch(p -> name.startsWith(p)))))
                .findFirst()
                .ifPresent(name -> {
                    try {
                        serPort = new SerialPort(name);
                        System.out.println("Connecting to "+serPort.getPortName());
                        if(serPort.openPort()){
                            serPort.setParams(SerialPort.BAUDRATE_9600,
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);
                            serPort.setEventsMask(SerialPort.MASK_RXCHAR);
                            serPort.addEventListener(event -> {
                                if(event.isRXCHAR()){
                                    try {
                                        sb.append(serPort.readString(event.getEventValue()));
                                        String ch=sb.toString();
                                        if(ch.endsWith("\r\n")){
                                            // add timestamp   Long.toString(System.currentTimeMillis())
                                            //line.set(sdf.format(System.currentTimeMillis()) //  formated time
                                            line.set(Long.toString(System.currentTimeMillis())   //  unformated time
                                                    .concat(SEPARATOR)
                                                    .concat(ch.substring(0,
                                                            ch.indexOf("\r\n"))));
                                            //sb.append(line);
                                            //sb.append(Long.toString(System.currentTimeMillis()));
                                            System.out.println(line);
                                            sb=new StringBuilder();
                                        }
                                    } catch (SerialPortException e) {
                                        System.out.println("SerialEvent error:"+ e.toString());
                                    }
                                }
                            });
                        }
                    } catch (SerialPortException ex) {
                        System.out.println("ERROR: Port '" + name + "': "+ex.toString());
                    }
                });
        return serPort!=null;
    }
    public void disconnect(){
        if (serPort != null) {
            try {
                serPort.removeEventListener();
                if(serPort.isOpened()){
                    serPort.closePort();
                }
            } catch (SerialPortException ex) {
                System.out.println("ERROR closing port exception: "+ex.toString());
            }
            System.out.println("Disconnecting: comm port closed.");
        }
    }
    public StringProperty getLine(){
        return line;
    }
    public String getPortName(){
        return serPort!=null?serPort.getPortName():"";
    }

    static void writeChar(char symbol, String portName, TextArea textArea){
        SerialPort port=new SerialPort(portName);
        try{
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            try{
                port.writeByte((byte) symbol);
                textArea.appendText("byte has been sent"+'\n');
            }finally {
                port.closePort();
            }
        }catch(SerialPortException e){System.out.println(e.getMessage()+'\n');}
    }
    static void readChar(String portName,TextArea textArea){
        SerialPort port=new SerialPort(portName);
        char c;
        try{
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            try{
                //while((c=port.readBytes())!=null)
                port.readBytes();
                textArea.appendText("byte has been sent"+'\n');
            }finally {
                port.closePort();
            }
        }catch(SerialPortException e){System.out.println(e.getMessage()+'\n');}
    }

    public static void main(String[] args) {
        new Serial().connect();
    }
}

