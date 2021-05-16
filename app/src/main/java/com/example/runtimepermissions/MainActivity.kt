package com.example.runtimepermissions

import android.Manifest.permission.*
import android.R
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runtimepermissions.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener


class MainActivity : AppCompatActivity(){

    val GALLERY_REQUEST_CODE = 100
    val CAMERA_REQUEST_CODE = 101

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // ГАЛЛЕРЕЯ
        binding.btnTakePictureFromGallery.setOnClickListener{
            Dexter.withContext(this)
                .withPermission(READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, GALLERY_REQUEST_CODE)
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(baseContext, "Отказ", Toast.LENGTH_SHORT).show()
                    }

                })
                .check()

        }

        // КАМЕРА
        binding.btnOpenCamera.setOnClickListener{

            val dialogPermissionListener: PermissionListener =
                DialogOnDeniedPermissionListener.Builder
                    .withContext(binding.root.context)
                    .withTitle("Доступ к камере")
                    .withMessage("необходимо разрешение")
                    .withButtonText(R.string.ok)
                    .withIcon(R.drawable.ic_menu_camera)
                    .build()

            val snackbarPermissionListener: PermissionListener =
                SnackbarOnDeniedPermissionListener.Builder
                    .with(binding.root, "Для доступа к камере необходимо разрешение")
                    .withOpenSettingsButton("Настройки")
                    .withCallback(object : Snackbar.Callback() {
                        override fun onShown(snackbar: Snackbar?) {
                            snackbar?.show()
                        }

                        override fun onDismissed(snackbar: Snackbar?, event: Int) {

                        }
                    }).build()


            val basePermissionListener: PermissionListener=
                object  : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                }

            val compositePermissionListener = CompositePermissionListener(snackbarPermissionListener, basePermissionListener)

            Dexter.withContext(this)
                .withPermission(CAMERA)
                .withListener(compositePermissionListener)
                .check()
        }

        // ЗАГРУЗКА ФАЙЛА
        val downloadLink = "https://downloader.disk.yandex.ru/disk/aebcc8f19ef34365d4ddd6ced45057a5b87186ab06fa1f9657136bf111fb686e/60a122eb/fKqInKw3d7bLFOeFnMGnhOY3mO1fYAAbCNY4qNGfXZb8ucRS9p1IvHjc0VeXvqLuQgHD5eur3dpLtcZV7puC72J4dxpTTFkgn5VlhaRhnv6r8npumZHI4midPdWhecNq?uid=1130000052701249&filename=%D0%9C%D0%B5%D0%BD%D1%8E%20%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BE%D1%81%D0%BF%D0%B8%D1%82%D0%B0%D0%BD%D0%BD%D0%B8%D0%BA%D0%BE%D0%B2%20%D0%B4%D1%81%2030.04.2021.pdf&disposition=attachment&hash=&limit=0&content_type=application%2Fpdf&owner_uid=1130000052701249&fsize=2184808&hid=0efa9d9effc10ff453b246149b5036c1&media_type=document&tknv=v2&etag=3bfbf8813806a5a15c7e9f7c4750c227"

        binding.btnDownloadPdf.setOnClickListener {

            Dexter.withContext(this)
                .withPermissions(
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(baseContext, "Идет загрузка ...", Toast.LENGTH_SHORT).show()

                            val request = DownloadManager.Request(Uri.parse(downloadLink))
                            request.setTitle("test_pdf")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            }
                            request.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS, "test.pdf")
                            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            request.setMimeType("application/pdf")
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                            downloadManager.enqueue(request)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        list: List<PermissionRequest>,
                        permissionToken: PermissionToken
                    ) {
                        permissionToken.continuePermissionRequest()
                    }
                }).withErrorListener { dexterError ->
                    Toast.makeText(
                        this,
                        dexterError.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .check()
        }

        // РЕКУРРЕНТНОЕ РАЗРЕШЕНИЕ ВСЕХ
        binding.btnGrantAllPermissions.setOnClickListener{

            val dialogMultiplePermissionsListener: MultiplePermissionsListener =
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                    .withContext(binding.root.context)
                    .withTitle("Доступ к камере и галлерее")
                    .withMessage("необходимо разрешение")
                    .withButtonText(R.string.ok)
                    .withIcon(R.drawable.ic_menu_camera)
                    .build()

            val snackbarMultiplePermissionsListener: MultiplePermissionsListener =
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                    .with(binding.root, "Необходим досутп к камере и галерее")
                    .withOpenSettingsButton("Настройки")
                    .withCallback(object : Snackbar.Callback() {
                        override fun onShown(snackbar: Snackbar) {
                            snackbar.show()
                        }

                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            // Event handler for when the given Snackbar has been dismissed
                        }
                    })
                    .build()

            val multiplePermissionListener : MultiplePermissionsListener
                = object : MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    Toast.makeText(baseContext, "Все разрешения даны!", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                }

            }


            val compositePermissionsListener = CompositeMultiplePermissionsListener(snackbarMultiplePermissionsListener, multiplePermissionListener);

            Dexter.withContext(this)
                .withPermissions(
                    READ_EXTERNAL_STORAGE,
                    CAMERA
                )
                .withListener(compositePermissionsListener)
                .check()

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK)
        {
            when (resultCode){
                GALLERY_REQUEST_CODE -> Toast.makeText(baseContext, "Галлерея открыта", Toast.LENGTH_SHORT).show()
                CAMERA_REQUEST_CODE -> Toast.makeText(baseContext, "Камера открыта", Toast.LENGTH_SHORT).show()
            }
        }
    }


}