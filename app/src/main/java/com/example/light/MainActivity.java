package com.example.light;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.renderscript.Sampler.Value;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.provider.Settings;
import android.net.Uri;
import android.view.WindowManager;
import android.content.Intent;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensor;
    private TextView light_intensity;
    private TextView light_name;
    private TextView light_power;
    private TextView light_max_range;
    private static final int MAX_BRIGHTNESS = 255;
    private static final float MAX_LUX = 1000f; // 最大光照值，可根据需要调整

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        light_intensity = (TextView) findViewById(R.id.textView1);
        light_name = (TextView) findViewById(R.id.light_name);
        light_power = (TextView) findViewById(R.id.light_power);
        light_max_range = (TextView) findViewById(R.id.light_max_range);
        
        // 添加传感器可用性检查
        Sensor lightSensor = sensor.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            light_intensity.setText("光线传感器不可用");
            return;
        }
        
        // 检查是否有修改系统设置的权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
// TODO Auto-generated method stub
        sensor.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        sensor.unregisterListener(this);
        super.onStop();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        float[] values = event.values;
        int sensorType = event.sensor.TYPE_LIGHT;
        if (sensorType == Sensor.TYPE_LIGHT) {
            StringBuilder light_intensity_text = new StringBuilder();
            StringBuilder light_name_text = new StringBuilder();
            StringBuilder light_power_text = new StringBuilder();
            StringBuilder light_max_range_text = new StringBuilder();
            light_intensity_text.append(String.format(" %.1f lux\n", values[0]));
            light_name_text.append(String.format(" %s\n", event.sensor.getName()));
            light_power_text.append(String.format(" %.2f mA\n", event.sensor.getPower()));
            light_max_range_text.append(String.format("最大测量范围: %.1f lux\n", event.sensor.getMaximumRange()));


            light_intensity.setText(light_intensity_text.toString());
            light_name.setText(light_name_text.toString());
            light_power.setText(light_power_text.toString());
            light_max_range.setText(light_max_range_text.toString());
            
            // 调整屏幕亮度
            adjustBrightness(values[0]);
        }
    }

    private void adjustBrightness(float luxValue) {
        // 计算亮度值（0-255）
        int brightness = (int) ((luxValue / MAX_LUX) * MAX_BRIGHTNESS);
        // 确保亮度值在有效范围内
        brightness = Math.min(Math.max(brightness, 20), MAX_BRIGHTNESS);
        
        try {
            // 检查是否开启了自动亮度调节
            int autoMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (autoMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                // 关闭自动亮度调节
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
            
            // 设置屏幕亮度
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
            
            // 立即应用亮度设置
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = brightness / (float) MAX_BRIGHTNESS;
            getWindow().setAttributes(layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
