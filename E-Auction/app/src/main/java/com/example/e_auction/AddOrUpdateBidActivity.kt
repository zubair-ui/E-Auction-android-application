package com.example.e_auction

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.databinding.ActivityAddOrUpdateBidBinding
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import com.example.e_auction.util.DataProvider
import retrofit2.Call
import retrofit2.Callback
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AddOrUpdateBidActivity : AppCompatActivity(), Callback<Response> {

    private lateinit var binding: ActivityAddOrUpdateBidBinding
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private var startCameraLauncher: ActivityResultLauncher<Intent>? = null
    private var startGalleryLauncher: ActivityResultLauncher<Intent>? = null
    lateinit var email: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: ProgressDialog
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private var reason: Int = 0
    private lateinit var editedBid: Bid
    private var photo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_update_bid)

        sharedPreferences = getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("please wait...")
        progressDialog.setCancelable(true)

        email = sharedPreferences.getString(Constant.KEY_USER_EMAIL, "").toString()
        reason = intent.getIntExtra(Constant.KEY_REASON, 0)

        renderUIForEdit()

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()
            val startingBid = binding.etStartingBid.text.toString().trim()
            try{
                val image = bitmapToBase64((binding.imgItem.drawable as BitmapDrawable).bitmap)
                if (title.isNotEmpty() && description.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()
                    && startingBid.isNotEmpty() && convertToComparableDate(startDate) < convertToComparableDate(endDate)) {
                    var request = Request()
                    if (reason == 2) {
                        val startBidInt = startingBid.toInt()
                        val highestBidInt = DataProvider.bid.highestBid.toInt()
                        if (startBidInt <= highestBidInt && highestBidInt!=0) {
                            request = Request(
                                action = Constant.UPDATE_BID_ITEM,
                                userEmail = email,
                                bidId = editedBid.bidId,
                                title = title,
                                description = description,
                                image = image,
                                startDate = startDate,
                                endDate = endDate,
                                startingBid = startingBid
                            )
                            progressDialog.show()
                            val callResponse = requestContract.makeApiCall(request)
                            callResponse.enqueue(this)
                        }else if (highestBidInt==0) {
                            request = Request(
                                action = Constant.UPDATE_BID_ITEM,
                                userEmail = email,
                                bidId = editedBid.bidId,
                                title = title,
                                description = description,
                                image = image,
                                startDate = startDate,
                                endDate = endDate,
                                startingBid = startingBid
                            )
                            progressDialog.show()
                            val callResponse = requestContract.makeApiCall(request)
                            callResponse.enqueue(this)
                        }else {
                            showToast("Starting bid should be less than $highestBidInt")
                        }

                    } else {
                        request = Request(
                            action = Constant.ADD_BID_ITEM,
                            userEmail = email,
                            title = title,
                            description = description,
                            image = image,
                            startDate = startDate,
                            endDate = endDate,
                            startingBid = startingBid
                        )
                        progressDialog.show()
                        val callResponse = requestContract.makeApiCall(request)
                        callResponse.enqueue(this)
                    }

                } else {
                    showToast("Please enter correct bid details")
                }
            }catch (_: Exception){
                showToast("Image cannot be empty")
            }
        }

        // Initialize the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera()
                } else {
                    // Permission denied
                }
            }

        // Initialize the camera launcher
        startCameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    photo = result.data?.extras?.get("data") as Bitmap?
                    binding.imgItem.setImageBitmap(photo)
                    // Handle the captured photo
                } else {
                    // Camera activity canceled or failed
                }
            }


        binding.imgItem.setOnClickListener {
            checkCameraPermission()
        }

        binding.etStartDate.setOnClickListener {
            showDatePickerDialog("start")
        }
        binding.etEndDate.setOnClickListener {
            showDatePickerDialog("end")
        }
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this@AddOrUpdateBidActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher?.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCameraLauncher?.launch(cameraIntent)
    }

    private fun showDatePickerDialog(startOrEnd: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)

                val selectedDate = "$selectedYear-$formattedMonth-$formattedDay"
                if(startOrEnd=="start"){
                    binding.etStartDate.setText(selectedDate)
                }else{
                    binding.etEndDate.setText(selectedDate)
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun convertToComparableDate(start:String): String {
        val convertedDate = start.replace('/','-')
        return convertedDate
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    private fun renderUIForEdit(){
        if(reason==2){
            editedBid = DataProvider.bid
            binding.etTitle.setText(editedBid.title)
            binding.etDescription.setText(editedBid.description)
            binding.imgItem.setImageBitmap(base64ToBitmap(editedBid.image))
            binding.etStartingBid.setText(editedBid.startingBid)
            binding.etStartDate.setText(editedBid.startDate)
            binding.etEndDate.setText(editedBid.endDate)
            binding.btnSubmit.text = "UPDATE"
        }
    }
    private fun base64ToBitmap(base64String: String): Bitmap? {
        // Decode Base64 string to byte array
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

        // Create Bitmap from byte array
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
        if(response.body()!=null){
            val serverResponse = response.body()
            if(serverResponse!!.status){
                showToast(serverResponse.message)
                Intent(this,UserBidsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(this)
                }
            }
            else{
                showToast(serverResponse.message)
            }
        }
        else{
            showToast("Server is not responding. Please contact your system administrator")
        }
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
        showToast("Server is not responding. Please contact your system administrator")
    }
}
