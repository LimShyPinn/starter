package com.google.firebase.codelab.barcode_scanning

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_main.*

class BarcodeScannerActivity : BaseCameraActivity() {

    private val qrList = arrayListOf<QrCode>()
    val adapter = QrCodeAdapter(qrList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rvQrCode.layoutManager = LinearLayoutManager(this)
        rvQrCode.adapter = adapter

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                val bitmap = jpeg?.size?.let { BitmapFactory.decodeByteArray(jpeg, 0, it) }
                bitmap?.let { runBarcodeScanner(it) }
                showPreview()
                imagePreview.setImageBitmap(bitmap)
            }

        })
    }

    private fun runBarcodeScanner(bitmap: Bitmap) {
        //Create a FirebaseVisionImage
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        //Optional : Define what kind of barcodes you want to scan
        val options = FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build()

        //Get access to an instance of FirebaseBarcodeDetector
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        detector.detectInImage(image).addOnSuccessListener {
            qrList.clear()
            adapter.notifyDataSetChanged()
            for (firebaseBarcode in it){
                when(firebaseBarcode.valueType){
                    FirebaseVisionBarcode.TYPE_URL -> qrList.add(QrCode("URL",firebaseBarcode.displayValue))

                    FirebaseVisionBarcode.TYPE_CONTACT_INFO -> qrList.add(QrCode("Contact",firebaseBarcode.contactInfo?.title))

                    FirebaseVisionBarcode.TYPE_WIFI -> qrList.add(QrCode("WiFi",firebaseBarcode.wifi?.ssid))

                    FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> qrList.add(QrCode("Driver License",firebaseBarcode.driverLicense?.licenseNumber))

                    else -> qrList.add(QrCode("Generic",firebaseBarcode.displayValue))
                }
            }
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
                .addOnFailureListener{
                    progressBar.visibility = View.GONE
                    Toast.makeText(baseContext,"Sorry, something went wrong!",Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener{
                    progressBar.visibility = View.GONE
                }
    }

    override fun onClick(v: View?) {
        progressBar.visibility = View.VISIBLE
        cameraView.captureSnapshot()
    }
}