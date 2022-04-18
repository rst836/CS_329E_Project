package com.example.testmap

import com.example.testmap.Network.BirdHttpClient
import com.example.testmap.Network.BirdListener

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.*

class BirdLoginMainFragment: Fragment(R.layout.fragment_bird_login_main) {

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

            // make the first request
            BirdHttpClient.firstAuthPost(emailText)

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
                BirdHttpClient.secondAuthPost(tokenText)

                // put the end fragment into the container
                activity?.supportFragmentManager!!.commit {
                    replace<BirdLogin3Fragment>(R.id.birdLoginContainer)
                }

                // disable the btn until further notice
                continueBtn.setOnClickListener{}
                val birdListener = object : BirdListener {
                    override fun onUpdateResults() {
                    }

                    override fun onUpdateAccess() {
                        activity?.runOnUiThread {
                            val message = view.findViewById<TextView>(R.id.birdLoginMessage)
                            message.setText(R.string.birdLoginCompleteText)
                            continueBtn.setOnClickListener {
                                activity?.supportFragmentManager!!.popBackStack()
                                BirdHttpClient.unsubscribe(this)
                            }
                        }
                    }
                }

                // listen for changes to the access token
                BirdHttpClient.subscribe(birdListener)

            }
        }

        return view
    }
}