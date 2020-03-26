package cash.z.ecc.android.ui.scan

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentScanBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.di.viewmodel.viewModel
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.ext.onClickNavTo
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.Tap.*
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.send.SendViewModel
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScanFragment : BaseFragment<FragmentScanBinding>() {
    override val screen = Report.Screen.SCAN
    private val viewModel: ScanViewModel by viewModel()

    private val sendViewModel: SendViewModel by activityViewModel()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun inflate(inflater: LayoutInflater): FragmentScanBinding =
        FragmentScanBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonReceive.onClickNavTo(R.id.action_nav_scan_to_nav_receive) { tapped(SCAN_RECEIVE) }
        binding.backButtonHitArea.onClickNavBack() { tapped(SCAN_BACK) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!allPermissionsGranted()) getRuntimePermissions()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            bindPreview(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        Preview.Builder().setTargetName("Preview").build().let { preview ->
            preview.setSurfaceProvider(binding.preview.previewSurfaceProvider)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), QrAnalyzer { q, i ->
                onQrScanned(q, i)
            })
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        }

    }

    private fun onQrScanned(qrContent: String, image: ImageProxy) {
        resumedScope.launch {
            if (viewModel.isNotValid(qrContent)) image.close() // continue scanning
            else {
                sendViewModel.toAddress = qrContent
                mainActivity?.safeNavigate(R.id.action_nav_scan_to_nav_send)
            }
        }
    }

//    private fun updateOverlay(detectedObjects: DetectedObjects) {
//        if (detectedObjects.objects.isEmpty()) {
//            return
//        }
//
//        overlay.setSize(detectedObjects.imageWidth, detectedObjects.imageHeight)
//        val list = mutableListOf<BoxData>()
//        for (obj in detectedObjects.objects) {
//            val box = obj.boundingBox
//            val name = "${categoryNames[obj.classificationCategory]}"
//            val confidence =
//                if (obj.classificationCategory != FirebaseVisionObject.CATEGORY_UNKNOWN) {
//                    val confidence: Int = obj.classificationConfidence!!.times(100).toInt()
//                    "$confidence%"
//                } else {
//                    ""
//                }
//            list.add(BoxData("$name $confidence", box))
//        }
//        overlay.set(list)
//    }







    //
    // Permissions
    //

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = mainActivity?.packageManager
                    ?.getPackageInfo(mainActivity?.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info?.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(mainActivity!!, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(mainActivity!!, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            requestPermissions(allNeededPermissions.toTypedArray(), CAMERA_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted()) {
//            view!!.postDelayed(
//                {
//                    onStartCamera()
//                },
//                2000L
//            ) // TODO: remove this temp hack to sidestep crash when permissions were not available
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1002

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}