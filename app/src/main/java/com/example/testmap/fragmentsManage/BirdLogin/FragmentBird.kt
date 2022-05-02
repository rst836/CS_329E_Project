package com.example.testmap.fragmentsManage.BirdLogin

import android.app.Activity
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
import com.example.testmap.MapsActivity
import com.example.testmap.R
import com.example.testmap.api.ClientListener
import com.example.testmap.api.HttpClient


class FragmentBird: Fragment(R.layout.fragment_bird_login_main) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bird_login_main, container, false)

        // put the bird login1 fragment into the container
        activity?.supportFragmentManager!!.commit {
            replace<BirdLogin1Fragment>(R.id.birdLoginContainer)
        }

        // set the button action
        val continueBtn = view.findViewById<Button>(R.id.birdLoginContinueBtn)
        continueBtn.setOnClickListener {
            // -- HANDLE THE FIRST SUBMIT ACTION
            // read in the email
            val emailText:String = view.findViewById<EditText>(R.id.enterBirdEmail).text.toString()

            if (emailText.isValidEmail()){
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
                        activity?.runOnUiThread {
                            activity?.supportFragmentManager!!.popBackStack()
                            Toast.makeText(context, R.string.bird_failed_login, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailedLimeAccess() {
                    }

                })
                // make the first request
                HttpClient.loginBird1(emailText)

                // put the bird login2 fragment into the container
                activity?.supportFragmentManager!!.commit {
                    replace<BirdLogin2Fragment>(R.id.birdLoginContainer)
                }

                // update the button action
                continueBtn.setOnClickListener {
                    // -- HANDLE THE SECOND SUBMIT ACTION
                    // read in the token
                    val tokenText:String = view.findViewById<EditText>(R.id.enterBirdToken).text.toString()

                    // make the second request
                    HttpClient.loginBird2(tokenText)

                    // put the end fragment into the container
                    activity?.supportFragmentManager!!.commit {
                        replace<BirdLogin3Fragment>(R.id.birdLoginContainer)
                    }

                    // disable the btn until further notice
                    continueBtn.setOnClickListener{}
                    val birdListener = object : ClientListener {
                        override fun onUpdateBirdResults() { }

                        override fun onUpdateLimeResults() { }

                        override fun onUpdateBirdAccess() {
                            activity?.runOnUiThread {
                                val message = view.findViewById<TextView>(R.id.birdLoginMessage)
                                val parActivity: Activity? = activity
                                if (parActivity != null && parActivity is MapsActivity) {
                                    val myActivity: MapsActivity = parActivity
                                    myActivity.viewModel.currBird.value = true
                                }
                                message.setText(R.string.birdLoginCompleteText)
                                continueBtn.setOnClickListener {
                                    activity?.supportFragmentManager!!.popBackStack()
                                    HttpClient.unsubscribe(this)
                                }
                            }
                        }

                        override fun onUpdateLimeAccess() {}
                        override fun onFailedBirdAccess() {
                            activity?.runOnUiThread{
                                val message = view.findViewById<TextView>(R.id.birdLoginMessage)
                                message.setText(R.string.birdLoginErrorText)
                                continueBtn.setOnClickListener {
                                    activity?.supportFragmentManager!!.popBackStack()
                                    HttpClient.unsubscribe(this)
                                }
                            }
                        }

                        override fun onFailedLimeAccess() { }
                    }

                    // listen for changes to the access token
                    HttpClient.subscribe(birdListener)
                }
            } else{
                Toast.makeText(context, R.string.invalidEmail, Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}