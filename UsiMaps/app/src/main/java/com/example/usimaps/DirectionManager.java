package com.example.usimaps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * The {@code DirectionManager} class provides a way to calculate the device's azimuth (direction).
 * It listens to changes in the accelerometer and magnetometer sensors to compute the azimuth
 * (orientation angle relative to magnetic north).
 */
public class DirectionManager {

    /** Manages sensor interactions.*/
    private SensorManager sensorManager;

    /** Accelerometer sensor for detecting gravity. */
    private Sensor accelerometer;

    /** Magnetometer sensor for detecting geomagnetic field. */
    private Sensor magnetometer;

    /** Stores accelerometer data representing gravity. */
    private float[] gravity;

    /** Stores magnetometer data representing geomagnetic field. */
    private float[] geomagnetic;

    /** Stores magnetometer data representing geomagnetic field. */
    private float azimuth;

    /**
     * Constructs a {@code DirectionManager} instance.
     *
     * @param context the application context used to access system services.
     */
    public DirectionManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    /**
     * Starts listening to accelerometer and magnetometer sensor updates.
     * This enables the calculation of the azimuth in real time.
     */
    public void startListening() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Stops listening to sensor updates to conserve resources and battery.
     */
    public void stopListening() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    /**
     * Returns the current azimuth (direction) in degrees.
     * The azimuth is the angle between the device's current orientation and magnetic north.
     *
     * @return the azimuth in degrees, normalized to the range [0, 360).
     */
    public float getAzimuth() {
        return azimuth;
    }

    /**
     * Listens for changes in accelerometer and magnetometer sensor data to calculate the azimuth.
     */
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values;
            }

            if (gravity != null && geomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = (float) Math.toDegrees(orientation[0]);
                    azimuth = (azimuth + 360) % 360;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Figure out what the hell this is
        }
    };
}

