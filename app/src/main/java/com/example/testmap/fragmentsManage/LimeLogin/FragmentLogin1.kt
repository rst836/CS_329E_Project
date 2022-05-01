package com.example.testmap.fragmentsManage.LimeLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.testmap.R


class FragmentLogin1 : Fragment(R.layout.fragment_lime_login1){

    var loginMethod:String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lime_login1, container, false)

        // set the radio buttons
        val radioPhone = view.findViewById<RadioButton>(R.id.radioPhone)
        radioPhone.setOnClickListener {
            loginMethod = "phone"
        }

        val radioEmail = view.findViewById<RadioButton>(R.id.radioEmail)
        radioEmail.setOnClickListener {
            loginMethod = "email"
        }

        return view
    }
}