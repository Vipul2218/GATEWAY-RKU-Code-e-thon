package com.example.gateway.activity

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.hardware.Sensor
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gateway.R

class SensorActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalStep: Float = 0f
    private var activityRecognitionGranted = false
    private var tvStepCount: TextView? = null

    //  Static data
    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION = 2
        private const val PERMISSIONS_REQUEST_ACCESS_ACTIVITY_LOCATION = 3
        private const val TAG = "CountStep"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        tvStepCount = findViewById(R.id.textView)


        //Counter monitor
        loadData()
        resetStep()
        initData()

    }

    /*
    Initialize counter and sensors
     */
    private fun initData() {
        tvStepCount!!.text = ("${totalStep.toInt()}")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    /*
    Ask for permission which allow to use sensor
     */
    private fun activityRecognitionPermission() {
        Log.v(
            TAG,
            "version sdk : ${Build.VERSION.SDK_INT} and version code : ${Build.VERSION_CODES.Q}"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_DENIED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                    PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION
                )
            } else {
                activityRecognitionGranted = true
            }
        } else {
            Log.v(
                TAG, "seftpermis ${
                    ContextCompat.checkSelfPermission(
                        this,
                        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                    )
                }"
            )
            if (ContextCompat.checkSelfPermission(
                    this,
                    "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                ) == PackageManager.PERMISSION_DENIED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),
                    PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION
                )
            } else {
                activityRecognitionGranted = true
            }
        }
    }

    /*
    On permission request
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        activityRecognitionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activityRecognitionGranted = true
                    startSensor()
                } else {
                    Toast.makeText(this, "This activity need permission to use", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun onResume() {
        super.onResume()
        running = true
        activityRecognitionPermission()
        Log.v(TAG, "permission : $activityRecognitionGranted")

        startSensor()
    }

    /*
    Start sensor on resume
     */
    private fun startSensor() {
        if (activityRecognitionGranted) {
            val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            if (stepSensor == null) {
                Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
            } else {
                sensorManager?.registerListener(
                    this,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                Toast.makeText(this, "Set up monitor!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        saveData()
        Toast.makeText(this, "Pause!!!", Toast.LENGTH_SHORT).show()
        Log.v(TAG, "Activity on pause, data updating!!!")
    }

    /*
    Call when sensor change motion
     */
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (running) {
                totalStep += p0.values[0]

                tvStepCount!!.text = ("${totalStep.toInt()}")
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun resetStep() {
        if(tvStepCount !=null){

        }
        tvStepCount!!.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }
        tvStepCount!!.setOnLongClickListener {
            totalStep = 0f
            tvStepCount!!.text = "0"
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        // Save step
        val editor = sharedPreferences.edit()
        editor.putFloat("previousTotalSteps", totalStep)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val saveNumber = sharedPreferences.getFloat("previousTotalSteps", 0f)
        Log.v(TAG, "$saveNumber")
        totalStep = saveNumber

    }
}