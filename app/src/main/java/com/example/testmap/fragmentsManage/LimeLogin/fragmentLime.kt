package com.example.testmap.fragmentsManage.LimeLogin

import com.example.testmap.network.HttpClient
import com.example.testmap.network.ClientListener

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.*
import com.example.testmap.R
import com.example.testmap.fragmentsManage.LimeLogin.fragmentLogin1
import com.example.testmap.fragmentsManage.LimeLogin.fragmentLogin2
import com.example.testmap.fragmentsManage.LimeLogin.fragmentLogin3

class fragmentLime: Fragment(R.layout.fragment_lime_login_main) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lime_login_main, container, false)

        // put the lime login1 fragment into the container
        activity?.supportFragmentManager!!.commit {
            replace<fragmentLogin1>(R.id.limeLoginContainer)
        }

        // set the button action
        val continueBtn = view.findViewById<Button>(R.id.limeLoginContinueBtn)

        // update the button action
        continueBtn.setOnClickListener {
            // -- HANDLE THE FIRST SUBMIT ACTION
            // read in the email
            val phone =
                view.findViewById<EditText>(R.id.enterLimeEmail).text.toString()

            // make the first request
            HttpClient.loginLime1(phone)

            // put the bird login2 fragment into the container
            activity?.supportFragmentManager!!.commit {
                replace<fragmentLogin2>(R.id.limeLoginContainer)
            }

            // update the button action
            continueBtn.setOnClickListener {
                // -- HANDLE THE SECOND SUBMIT ACTION
                // read in the token
                val tokenText: String =
                    view.findViewById<EditText>(R.id.enterLimeToken).text.toString().trim()

                // make the second request
                HttpClient.loginLime2(tokenText)

                // put the end fragment into the container
                activity?.supportFragmentManager!!.commit {
                    replace<fragmentLogin3>(R.id.limeLoginContainer)
                }

                // disable the btn until further notice
                continueBtn.setOnClickListener {}

                val limeListener = object : ClientListener {
                    override fun onUpdateBirdResults() {}

                    override fun onUpdateLimeResults() {}

                    override fun onUpdateLimeAccess() {
                        activity?.runOnUiThread {
                            val message = view.findViewById<TextView>(R.id.limeLoginMessage)
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
        }


        return view
    }
}