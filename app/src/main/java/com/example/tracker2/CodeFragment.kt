package com.example.tracker2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CodeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_code, container, false)
        val buttonSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val textField = view.findViewById<EditText>(R.id.editTextTextPersonName)
        buttonSubmit.setOnClickListener(View.OnClickListener {
            val code = textField.text.toString()
            sendCode(code)
        })

        return view
    }

    private fun sendCode(code: String) {
        val id = MainActivity.id
        if (id>0){
            val url = "https://rtqtybnff0.execute-api.eu-west-3.amazonaws.com/dev/traps"
            val json = "{\"code\":\"$code\", \"trackerId\":${MainActivity.id}}"
            Log.d("JSON CODE", json)
            Log.d("URL CODE", url)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            val request = Request.Builder()
                .method("PATCH", requestBody)
                .url(url)
                .build()
            MainActivity.okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("CODE_FAIL", "This is a failure")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    try {
                        var json = JSONObject(responseData)
                        println("Code Request Successful!!")
                        Log.d("CODE_SUCCESS", json.toString())
                        //Toast.makeText(context, json.toString(), Toast.LENGTH_LONG).show()
                        Intent().also { intent ->
                            intent.setAction("show")
                            intent.putExtra("msg", json.toString())
                            context?.sendBroadcast(intent)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CodeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CodeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}