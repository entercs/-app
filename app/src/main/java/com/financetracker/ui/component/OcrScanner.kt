package com.financetracker.ui.component

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.financetracker.ui.theme.Green500
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
@Composable
fun OcrScanButton(onResult: (VoiceParser.Result) -> Unit) {
    val context = LocalContext.current
    val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@rememberLauncherForActivityResult
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.textBlocks.joinToString(" ") { it.text }
                    val parsed = VoiceParser().parse(text)
                    onResult(parsed)
                }
                .addOnFailureListener { }
        } catch (_: Exception) { }
    }

    IconButton(onClick = {
        galleryLauncher.launch("image/*")
    }) {
        Icon(Icons.Filled.CameraAlt, contentDescription = "拍照识别", tint = Green500, modifier = Modifier.size(24.dp))
    }
}
