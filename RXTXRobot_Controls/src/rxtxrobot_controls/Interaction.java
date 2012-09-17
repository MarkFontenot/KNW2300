package rxtxrobot_controls;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rxtxrobot.RXTXRobot;

public class Interaction implements Runnable
{
    public static final int RUN_MOTOR = 1;
    public static final int MOVE_SERVO = 2;
    public static final int MOVE_BOTH_SERVOS = 5;
    public static final int READ_ANALOG = 3;
    public static final int READ_DIGITAL = 4;
    RXTXRobot robot;
    private boolean running;
    private String port;
    private PrintStream out;
    private PrintStream err;
    //private int[] execArgs;
    private ArrayList<int[]> execArgs;
    private MainWindow parent;
    public Interaction(MainWindow par, String p, PrintStream out, PrintStream err)
    {
        parent = par;
        port = p;
        this.out = out;
        this.err = err;
        execArgs = null;
    }
    
    @Override
    public void run()
    {
        running = true;
        this.connect();
        while (running)
        {
            synchronized(this)
            {

                    if (execArgs.isEmpty())
                        try {
                            System.out.println("waiting...");
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Interaction.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (!execArgs.isEmpty())
                {
                    try
                    {
                        int[] exec = execArgs.remove(0);
                        switch(exec[0])
                        {
                            case Interaction.MOVE_SERVO:
                                robot.moveServo(exec[1], exec[2]);
                                break;
                            case Interaction.MOVE_BOTH_SERVOS:
                                robot.moveBothServos(exec[1],exec[2]);
                                break;
                            case Interaction.RUN_MOTOR:
                                robot.runMotor(exec[1],exec[2], exec[3]);
                                break;
                            case Interaction.READ_ANALOG:
                                int[] pins = robot.getAnalogPins();
                                String set = "";
                                for (int x=0;x<pins.length;++x)
                                {
                                    if (x!=0)
                                        set += ", ";
                                    set += pins[x];
                                }
                                parent.analog_textbox.setText(set);
                                break;
                            case Interaction.READ_DIGITAL:
                                int[] pins1 = robot.getDigitalPins();
                                String set1 = "";
                                for (int x=0; x < pins1.length; ++x)
                                {
                                    if (x!=0)
                                        set1 += ", ";
                                    set1 += pins1[x];
                                }
                                parent.digital_textbox.setText(set1);
                                break;
                            default:
                                break;
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("An error occured with executing command");
                    }
                }
            }
        }
        disconnect();
    }
    public void execute(int... args)
    {
        execArgs.add(args);
    }
    private void connect()
    {
        parent.arduino_connect_btn.setText("Connecting...");
        parent.arduino_connect_btn.setEnabled(false);
        robot = new RXTXRobot(port,true);
        robot.setErrStream(err);
        robot.setOutStream(out);
        if (robot.isConnected())
        {
            parent.arduino_connect_btn.setText("Disconnect");
            parent.arduino_connect_btn.setEnabled(true);
            parent.connection_status.setText("Connected");
            parent.connection_status.setForeground(new Color(0,190,0));
            parent.enableAll(true);
        }
        else
        {
            parent.arduino_connect_btn.setText("Connect");
            parent.arduino_connect_btn.setEnabled(true);
            parent.connection_status.setText("Error");
            parent.connection_status.setForeground(new Color(255,0,0));
            parent.enableAll(false);
        }
    }
    public void stopRunning()
    {
        running = false;
    }
    public void disconnect()
    {
        parent.arduino_connect_btn.setText("Connect");
        parent.arduino_connect_btn.setEnabled(true);
        parent.connection_status.setText("Disconnected");
        parent.connection_status.setForeground(new Color(255,0,0));
        robot.close();
        robot = null;
        parent.enableAll(false);
    }
    public RXTXRobot getRXTXRobot()
    {
        return robot;
    }
}
