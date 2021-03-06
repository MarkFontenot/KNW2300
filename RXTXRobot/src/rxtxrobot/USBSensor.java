package rxtxrobot;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;

/**
 * @author Chris King
 * @version 3.1.5
 */
public class USBSensor extends SerialCommunication
{
        /**
         * The maximum number of analog pins that can be read from the USB
         * Sensor (0 &le; pins &lt; NUM_DIGITAL_PINS)
         */
        public int NUM_ANALOG_PINS;
        /**
         * The maximum number of digital output pins that can be written or read
         * from the USB Sensor (0 &le; pins &lt; NUM_DIGITAL_OUTPUT_PINS)
         */
        public int NUM_DIGITAL_OUTPUT_PINS;
        /**
         * The maximum number of digital input pins that can be read from the
         * USB Sensor (0 &le; pins &lt; NUM_DIGITAL_INPUT_PINS)
         */
        public int NUM_DIGITAL_INPUT_PINS;
        private InterfaceKitPhidget inter;

        /**
         * Initialize the USB Sensor.
         *
         */
        public USBSensor()
        {
                try
                {
                        inter = new InterfaceKitPhidget();
                        NUM_ANALOG_PINS = inter.getSensorCount();
                        NUM_DIGITAL_OUTPUT_PINS = inter.getOutputCount();
                        NUM_DIGITAL_INPUT_PINS = inter.getInputCount();
                }
                catch (PhidgetException ex)
                {
                        error("Error creating InterfaceKitPhidget", "USBSensor", "Constructor");
                }
        }

        /**
         * Checks if the USB Sensor object is connected to the actual sensor.
         *
         * Returns true if the USB Sensor object is successfully connected to
         * the actual sensor. Returns false otherwise.
         *
         * @return true/false value that specifies if the USB Sensor object is
         * connected to the sensor.
         */
        @Override
        public boolean isConnected()
        {
                try
                {
                        return inter.isAttached();
                }
                catch (Exception e)
                {
                        return false;
                }
        }

        /**
         * Closes the connection to the USB Sensor.
         *
         * This method closes the serial connection to the USB Sensor. It
         * deletes the mutual exclusion lock file, which is important, so this
         * should be done before the program is terminated.
         */
        @Override
        public void close()
        {
                try
                {
                        if (inter.isAttached())
                                inter.close();
                }
                catch (Exception e)
                {
                        error("Could not close the USB Sensor! Message: " + e.getMessage(), "USBSensor", "close");
                }
        }

        /**
         * Attempts to connect to any USB Sensor.
         *
         * This method attempts to make a serial connection to ANY USB Sensor.
         * This can be problematic if there are multiple USB Sensors to be
         * connected to. If there are multiple, you should specify a serial
         * number. If there is an error in connecting, then the appropriate
         * error message will be displayed. <br><br> This function will
         * terminate runtime if an error is discovered.
         */
        @Override
        public void connect()
        {
                connect(0);
        }

        /**
         * Attempts to connect to a specific USB Sensor.
         *
         * This method attempts to make a serial connection to a specific USB
         * Sensor. The serial number supplied must be equivalent to the serial
         * number on the USB Sensor. If there is an error in connecting, then
         * the appropriate error message will be displayed. <br><br> This
         * function will terminate runtime if an error is discovered.
         *
         * @param serial Serial number of the USB Sensor to connect to.
         */
        public void connect(int serial)
        {
                this.getOutStream().print("Connecting to the USB Sensor");
                try
                {
                        if (serial == 0)
                        {
                                this.getOutStream().println("...");
                                inter.openAny();
                        }
                        else
                        {
                                this.getOutStream().println(" with serial " + serial + "...");
                                inter.open(serial);
                        }
                }
                catch (Exception e)
                {
                        error("Opening InterfaceKitPhidget error: " + e.getMessage(), "USBSensor", "connect");
                }
                this.getOutStream().println("Waiting for sensor to be attached...");
                try
                {
                        for (int x = 0; x < 50; ++x)
                        {
                                if (inter.isAttached())
                                        break;
                                this.sleep(500);
                        }
                        if (!inter.isAttached())
                        {
                                error("Could not find the USB sensor in time.  Check that you are using the correct serial number!", "USBSensor", "connect", true);
                                System.exit(1);
                        }
                }
                catch (Exception e)
                {
                        error("Could not check if the device is attached!  Message: " + e.getMessage(), "USBSensor", "connect");
                }
                try
                {
                        this.getOutStream().println("Sensor attached! (serial # " + inter.getSerialNumber() + ")");
                }
                catch (Exception e)
                {
                }
        }

        /**
         * Returns an AnalogPin object for the specified Analog pin.
         *
         * This will get the value of the pin on the USB Sensor.
         *
         * @param index The number of the pin: 0 &lt; x &lt;
         * {@link #NUM_ANALOG_PINS NUM_ANALOG_PINS}
         * @return AnalogPin object of the specified pin, or null if error.
         */
        public AnalogPin getAnalogPin(int index)
        {
                if (!isConnected())
                {
                        error("No USB Sensor is connected.", "USBSensor", "getAnalogPin");
                        return null;
                }
                try
                {
                        if (index >= inter.getSensorCount())
                        {
                                error("Invalid index of AnalogPin: " + index, "USBSensor", "getAnalogPin");
                                return null;
                        }
                }
                catch (Exception e)
                {
                        return null;
                }
                try
                {
                        return new AnalogPin(index, inter.getSensorRawValue(index));
                }
                catch (Exception e)
                {
                        error("Could not get the real sensor data!  Message: " + e.getMessage(), "USBSensor", "getAnalogPin");
                        return null;
                }
        }

        /**
         * Returns a DigitalPin object for the specified Input Digital pin.
         *
         * This will get the value of the pin on the USB Sensor.
         *
         * @param index The number of the pin: 0 &lt; x &lt;
         * {@link #NUM_DIGITAL_INPUT_PINS NUM_DIGITAL_INPUT_PINS}
         * @return DigitalPin object of the specified pin, or null if error.
         */
        public DigitalPin getDigitalInputPin(int index)
        {
                if (!isConnected())
                {
                        error("No USB Sensor is connected", "USBSensor", "getDigitalInputPin");
                        return null;
                }
                try
                {
                        if (index >= inter.getInputCount())
                        {
                                error("Invalid index of DigitalInputPin: " + index, "USBSensor", "getDigitalInputPin");
                                return null;
                        }
                }
                catch (Exception e)
                {
                        return null;
                }
                try
                {
                        return new DigitalPin(index, inter.getInputState(index) ? 1 : 0);
                }
                catch (Exception e)
                {
                        error("Could not get the real sensor data!  Message: " + e.getMessage(), "USBSensor", "getDigitalInputPin");
                        return null;
                }
        }

        /**
         * Returns a DigitalPin object for the specified Output Digital pin.
         *
         * This will get the value of the pin on the USB Sensor.
         *
         * @param index The number of the pin: 0 &lt; x &lt;
         * {@link #NUM_DIGITAL_INPUT_PINS NUM_DIGITAL_INPUT_PINS}
         * @return DigitalPin object of the specified pin, or null if error.
         */
        public DigitalPin getDigitalOutputPin(int index)
        {
                if (!isConnected())
                {
                        error("No USB Sensor is connected", "USBSensor", "getDigitalOutputPin");
                        return null;
                }
                try
                {
                        if (index >= inter.getOutputCount())
                        {
                                error("Invalid index of DigitalOutputPin: " + index, "USBSensor", "getDigitalOutputPin");
                                return null;
                        }
                }
                catch (Exception e)
                {
                        return null;
                }
                try
                {
                        return new DigitalPin(index, inter.getOutputState(index) ? 1 : 0);
                }
                catch (Exception e)
                {
                        error("Could not get the real sensor data!  Message: " + e.getMessage(), "USBSensor", "getDigitalOutputPin");
                        return null;
                }
        }

        /**
         * Sets the value of the specified Digital Output pin.
         *
         * This will set the value of the Digital Output pin.
         *
         * @param index The number of the pin: 0 &lt; x &lt;
         * {@link #NUM_DIGITAL_INPUT_PINS NUM_DIGITAL_INPUT_PINS}
         * @param value The value to write to the pin: 0 or 1
         */
        public void setDigitalOutputPin(int index, int value)
        {
                if (!isConnected())
                {
                        error("No USB Sensor is connected", "USBSensor", "setDigitalOutputPin");
                        return;
                }
                if (value != 0 && value != 1)
                {
                        error("Value must be either 0 or 1.", "USBSensor", "setDigitalOutputPin");
                        return;
                }
                try
                {
                        if (index >= inter.getOutputCount())
                        {
                                error("Invalid index of DigitalOutputPin: " + index, "USBSensor", "setDigitalOutputPin");
                                return;
                        }
                }
                catch (Exception e)
                {
                        return;
                }
                try
                {
                        inter.setOutputState(index, value == 1);
                }
                catch (Exception e)
                {
                        error("Could not set the sensor data!  Message: " + e.getMessage(), "USBSensor", "setDigitalOutputPin");
                }
        }
}
