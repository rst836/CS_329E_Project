package com.example.testmap.fragmentsManage.LimeLogin

import android.app.Activity
import com.example.testmap.api.HttpClient
import com.example.testmap.api.ClientListener

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.example.testmap.MapsActivity
import com.example.testmap.R
import java.util.regex.Pattern

const val REG = "(?<!\\d)\\d{10}(?!\\d)"
var PATTERN: Pattern = Pattern.compile(REG)

class FragmentLime: Fragment(R.layout.fragment_lime_login_main) {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lime_login_main, container, false)
        val nestedFrag1 = FragmentLogin1()
        // put the lime login1 fragment into the container
        activity?.supportFragmentManager!!.commit {
            replace(R.id.limeLoginContainer, nestedFrag1)
        }

        // set the continue button action
        val continueBtn = view.findViewById<Button>(R.id.limeLoginContinueBtn)

        val parActivity: Activity? = activity
        if (parActivity != null && parActivity is MapsActivity) {
            val myActivity: MapsActivity = parActivity
            val limeListen = myActivity.viewModel.currLime
            limeListen.observe(viewLifecycleOwner, Observer{
                if (limeListen.value == true) {
                    continueBtn.isEnabled = false
                    continueBtn.isClickable = false
                    continueBtn.alpha = 0.5f
                }
            })
        }

        // update the button action
        continueBtn.setOnClickListener {
            val loginMethod = nestedFrag1.loginMethod
            // -- HANDLE THE FIRST SUBMIT ACTION
            // read in the phone
            val idInfo:String = when (loginMethod) {
                "email" -> {
                    view.findViewById<EditText>(R.id.enterLimeEmail).text.toString()
                    // TODO: add email validation before continuing
                }
                "phone" -> {
                    view.findViewById<EditText>(R.id.enterLimePhone).text.toString()
                }
                else -> {
                    ""
                }
            }

            if ((loginMethod == "email" && idInfo.isValidEmail()) || (loginMethod == "phone" && idInfo.isPhoneNumber())) {

                HttpClient.subscribe(object:ClientListener {
                    override fun onUpdateBirdResults() {

                    }

                    override fun onUpdateLimeResults() {
                    }

                    override fun onUpdateBirdAccess() {
                    }

                    override fun onUpdateLimeAccess() {
                    }

                    override fun onFailedBirdAccess() {
                    }

                    override fun onFailedLimeAccess() {
                        activity?.runOnUiThread {
                            val message = view.findViewById<TextView>(R.id.limeLoginMessage)
                            val parActivity: Activity? = activity
                            if (parActivity != null && parActivity is MapsActivity) {
                                val myActivity: MapsActivity = parActivity
                                myActivity.viewModel.nextFrag.value = false
                            }
                            activity?.supportFragmentManager!!.popBackStack()
                            Toast.makeText(context, R.string.lime_failed_login, Toast.LENGTH_LONG).show()
                        }
                    }

                })

                // make the first request
                HttpClient.loginLime1(idInfo, loginMethod)

                // put the bird login2 fragment into the container
                activity?.supportFragmentManager!!.commit {
                    replace<FragmentLogin2>(R.id.limeLoginContainer)
                }

                // update the button action
                continueBtn.setOnClickListener {
                    println("second submit action!!")
                    // -- HANDLE THE SECOND SUBMIT ACTION
                    // read in the token
                    val tokenText: String =
                        view.findViewById<EditText>(R.id.enterLimeToken).text.toString().trim()
                    println("token text: $tokenText")
                    // make the second request
                    HttpClient.loginLime2(tokenText)
                    println("made the request")
                    // put the end fragment into the container
                    activity?.supportFragmentManager!!.commit {
                        println("replacing...")
                        replace<FragmentLogin3>(R.id.limeLoginContainer)
                    }
                    println("replaced")
                    // disable the btn until further notice
                    continueBtn.setOnClickListener {}

                    val limeListener = object : ClientListener {
                        override fun onUpdateBirdResults() {}

                        override fun onUpdateLimeResults() {}

                        override fun onUpdateLimeAccess() {
                            activity?.runOnUiThread {
                                val message = view.findViewById<TextView>(R.id.limeLoginMessage)
                                val parActivity: Activity? = activity
                                if (parActivity != null && parActivity is MapsActivity) {
                                    val myActivity: MapsActivity = parActivity
                                    myActivity.viewModel.currLime.value = true
                                }
                                message.setText(R.string.limeLoginCompleteText)
                                continueBtn.setOnClickListener {
                                    activity?.supportFragmentManager!!.popBackStack()
                                    HttpClient.unsubscribe(this)
                                }
                            }
                        }

                        override fun onUpdateBirdAccess() {}
                        override fun onFailedLimeAccess() {
                            activity?.runOnUiThread {
                                val message = view.findViewById<TextView>(R.id.limeLoginMessage)
                                val parActivity: Activity? = activity
                                if (parActivity != null && parActivity is MapsActivity) {
                                    val myActivity: MapsActivity = parActivity
                                    myActivity.viewModel.nextFrag.value = false
                                }
                                message.setText(R.string.limeLoginErrorText)
                                continueBtn.setOnClickListener {
                                    activity?.supportFragmentManager!!.popBackStack()
                                    HttpClient.unsubscribe(this)
                                }
                            }
                        }

                        override fun onFailedBirdAccess() {}
                    }

                    // listen for changes to the access token
                    HttpClient.subscribe(limeListener)

                }
            } else{
                if (loginMethod == "email"){
                    Toast.makeText(context, R.string.invalidEmail, Toast.LENGTH_LONG).show()
                } else{
                    Toast.makeText(context, R.string.invalidNumber, Toast.LENGTH_LONG).show()
                }
            }
        }


        return view
    }
    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    private fun CharSequence.isPhoneNumber() : Boolean = PATTERN.matcher(this).find()
}