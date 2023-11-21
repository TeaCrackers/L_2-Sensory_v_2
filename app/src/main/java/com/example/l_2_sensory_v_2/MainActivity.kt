package com.example.l_2_sensory_v_2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.l_2_sensory_v_2.ui.theme.L_2Sensory_v_2Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            L_2Sensory_v_2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyAppContent()
                }
            }
        }
    }
}

class SharedViewModel : ViewModel() {
    var counter by mutableStateOf(0)
        private set

    fun incrementCounter() {
        counter++
    }
}

class SensorViewModel(applicationContext: Context) : ViewModel(), SensorEventListener {
    private val sensorManager =
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    var accelerometerReading: FloatArray = floatArrayOf(0f, 0f, 0f)
        internal set

    var lightLevel by mutableStateOf(0f)
        private set

    init {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading = event.values.clone()
        } else if (event.sensor.type == Sensor.TYPE_LIGHT) {
            lightLevel = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing for now
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val applicationContext = LocalContext.current.applicationContext
    NavGraph(navController = navController, applicationContext = applicationContext)

}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "screen1",
    applicationContext: Context
) {
    val sharedViewModel: SharedViewModel = viewModel()
    val sensorViewModel: SensorViewModel = viewModel(factory = SensorViewModelFactory(applicationContext))

    NavHost(navController = navController, startDestination = startDestination) {
        addScreens(navController, sharedViewModel, sensorViewModel)
    }
}


private fun NavGraphBuilder.addScreens(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    sensorViewModel: SensorViewModel
) {
    composable("screen1") {
        Screen1(navController = navController, sharedViewModel = sharedViewModel, sensorViewModel = sensorViewModel)
    }

    composable(
        "screen2",
    ) {
        Screen2(
            navController = navController,
            x = sensorViewModel.accelerometerReading[0],
            y = sensorViewModel.accelerometerReading[1],
            z = sensorViewModel.accelerometerReading[2],
            sensorViewModel = sensorViewModel
        )
    }
    composable(
        "screen3",
    ) {
        Screen3(
            lightLevel = sensorViewModel.lightLevel
        )
    }
}

@Composable
fun Screen1(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    sensorViewModel: SensorViewModel
) {
    var inputValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // UI for Screen1

        // Button to navigate to Screen2 with arguments
        Button(
            onClick = {
                sharedViewModel.incrementCounter()
                val counterValue = sharedViewModel.counter
                navController.navigate(
                    "screen2"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Navigate to Screen 2")
        }
    }
}

@Composable
fun Screen2(
    navController: NavHostController,
    x: Float,
    y: Float,
    z: Float,
    sensorViewModel: SensorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // UI for Screen2

        // Display real-time accelerometer readings
        Text("Real-time Accelerometer X: $x")
        Text("Real-time Accelerometer Y: $y")
        Text("Real-time Accelerometer Z: $z")

        // Button to refresh the accelerometer readings
        Button(
            onClick = {
                navController.navigate("screen2")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh")
        }
        // Button to navigate to Screen3
        Button(
            onClick = {
                navController.navigate("screen3")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Navigate to Screen 3")
        }
    }
}

@Composable
fun Screen3(lightLevel: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // UI for Screen3

        // Display the light level as a progress bar
        LinearProgressIndicator(
            progress = lightLevel / 600, // Assuming the light level is in a range of 0 to 100
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Display the light level value
        Text("Light Level: $lightLevel")
    }
}

@Composable
fun MyAppContent() {
    L_2Sensory_v_2Theme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyApp()
        }
    }
}


