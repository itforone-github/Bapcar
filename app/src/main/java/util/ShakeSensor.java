package util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeSensor implements SensorEventListener {
    Activity act;
    public SensorManager sensorManager;
    public Sensor stepCountSensor;
    private int mCounterSteps=0;
    public ShakeSensor(Activity act){
        this.act=act;
    }
    public void setSensorService(){
        sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }
    public void setRegister(){
        sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_GAME);
    }
    public void setUnregister(){
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("event",event.sensor.getType()+"");
        mCounterSteps++;
        Log.d("step",mCounterSteps+"");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
