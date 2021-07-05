package com.chelgames.photolab5

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.IOException


class MainActivity : AppCompatActivity(){
    private val GALLERY_REQUEST = 1
    lateinit var image: InputImage
    lateinit var bitmap: Bitmap
    var photoIsLoad = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun clickOnLoad(view: View){





        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST)
    }

    fun clickOnSearch(view: View){
        if(photoIsLoad == 0){
            Toast.makeText(this, "Загрузите фото для обнаружения лиц", Toast.LENGTH_LONG).show()
            return
        }

        val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        val detector = FaceDetection.getClient(highAccuracyOpts)
        val result = detector.process(image)
                .addOnSuccessListener { faces ->
                    var countFaces = 0
                    val myImageView = findViewById<ImageView>(R.id.Photo)
                    val myBitmap = Bitmap.createBitmap(bitmap)
                    val tempBitmap = Bitmap.createBitmap(myBitmap.width, myBitmap.height, Bitmap.Config.RGB_565)
                    val options = BitmapFactory.Options()
                    options.inMutable = true

                    val myRectPaint = Paint()
                    myRectPaint.strokeWidth = 5f
                    myRectPaint.color = Color.RED
                    myRectPaint.style = Paint.Style.STROKE

                    val tempCanvas = Canvas(tempBitmap)
                    tempCanvas.drawBitmap(myBitmap, 0f, 0f, null)
                    for (face in faces) {
                        val bounds = face.boundingBox
                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                        // nose available):
                        val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
                        leftEar?.let {
                            val leftEarPos = leftEar.position
                        }

                        // If contour detection was enabled:
                        val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
                        val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points

                        // If classification was enabled:
                        if (face.smilingProbability != null) {
                            val smileProb = face.smilingProbability
                        }
                        if (face.rightEyeOpenProbability != null) {
                            val rightEyeOpenProb = face.rightEyeOpenProbability
                        }

                        // If face tracking was enabled:
                        if (face.trackingId != null) {
                            val id = face.trackingId
                        }

                        tempCanvas.drawRoundRect(bounds.toRectF(), 2f, 2f, myRectPaint)

                        //Log.d("MyLog", "${bounds.bottom}_${bounds.top}_${bounds.left}_${bounds.right}")
                        //Log.d("MyLog", "Succefully!!!")
                        countFaces++
                    }
                    when(countFaces){
                        0 -> Toast.makeText(this, "На экране нет лиц", Toast.LENGTH_LONG).show()
                        1 -> Toast.makeText(this, "На экране $countFaces лицо", Toast.LENGTH_LONG).show()
                        2,3,4 -> Toast.makeText(this, "На экране $countFaces лица", Toast.LENGTH_LONG).show()
                        else ->Toast.makeText(this, "На экране $countFaces лиц", Toast.LENGTH_LONG).show()
                    }
                    myImageView.setImageDrawable(BitmapDrawable(resources, tempBitmap))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка!!!", Toast.LENGTH_LONG).show()
                    //Log.d("MyLog", "Failed!!!")
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GALLERY_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data?.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                    photoIsLoad = 1
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val imageView = findViewById<ImageView>(R.id.Photo)
                imageView.setImageBitmap(bitmap)
                image = InputImage.fromBitmap(bitmap, 0)
            }
        }
    }
}