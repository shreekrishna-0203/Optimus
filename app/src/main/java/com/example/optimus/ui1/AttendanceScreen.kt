package com.example.optimus.ui1

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.optimus.GradientBackground
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.Hashtable

val motivationalQuotes = listOf(
    "Push yourself, because no one else is going to do it for you.",
    "The body achieves what the mind believes.",
    "What seems impossible today will become your warm-up tomorrow.",
    "Don't limit your challenges, challenge your limits.",
    "No pain, no gain. Get up and train."
)

// Mutable attendance log tracking "entry" and "exit" events
val attendanceLog = mutableStateListOf<Pair<LocalDateTime, String>>()

@Composable
fun AttendanceScreen(username: String, gymId: String) {
    var currentView by remember { mutableStateOf("qrCode") } // Tracks the current view
    var currentQuote by remember { mutableStateOf(motivationalQuotes.random()) } // Dynamic quote

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (currentView) {
                "qrCode" -> GymQRCodeScreen(gymId = gymId) {
                    currentView = "scanner"
                }
                "scanner" -> GymAttendanceQRScanner(
                    onBack = {
                        currentView = "qrCode"
                        currentQuote = motivationalQuotes.random() // Update quote
                    },
                    onScanSuccess = { handleAttendance() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated motivational quote at the bottom
            AnimatedQuote(quote = currentQuote, Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun GymQRCodeScreen(gymId: String, onScanClick: () -> Unit) {
    val size = 250.dp
    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(gymId) {
        bitmap = generateQRCodeBitmap(gymId, 500, 500)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Gym Check-In QR Code",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Gym QR Code",
                modifier = Modifier
                    .size(size)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scan Gym QR Code")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun GymAttendanceQRScanner(onBack: () -> Unit, onScanSuccess: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        PermissionRequestUI(onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        })
    } else {
        CameraPreview(onBack = onBack, onScanSuccess = onScanSuccess)
    }
}

@Composable
fun PermissionRequestUI(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}



@Composable
fun CameraPreview(onBack: () -> Unit, onScanSuccess: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan Gym QR Code",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraSetup", "Camera setup failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onScanSuccess) {
            Text(text = "Simulate Scan Success")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text(text = "Back")
        }
    }
}

fun handleAttendance() {
    val now = LocalDateTime.now()
    if (attendanceLog.isNotEmpty() && attendanceLog.last().second == "entry") {
        attendanceLog.add(now to "exit")
        Log.d("Attendance", "Exit logged at $now")
    } else {
        attendanceLog.add(now to "entry")
        Log.d("Attendance", "Entry logged at $now")
    }
}

fun generateQRCodeBitmap(data: String, width: Int, height: Int): Bitmap? {
    return try {
        val hintMap = Hashtable<EncodeHintType, Any>()
        hintMap[EncodeHintType.MARGIN] = 1 // Set margin
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hintMap)
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y)) {
                    -0x1000000 // Black
                } else {
                    -0x1 // White
                }
            }
        }

        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun AnimatedQuote(quote: String, modifier: Modifier = Modifier) {
    var animatedQuote by remember { mutableStateOf("") }

    LaunchedEffect(quote) {
        animatedQuote = ""
        quote.forEachIndexed { index, char ->
            animatedQuote += char
            delay(50) // Typing animation effect
        }
    }

    Text(
        text = animatedQuote,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.padding(16.dp)
    )
}
