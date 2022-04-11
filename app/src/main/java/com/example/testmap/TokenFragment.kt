package com.example.testmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.example.testmap.network.BirdHttpClient

class TokenFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.token, container, false)

        val btn: Button = view.findViewById<Button>(R.id.token_button)
        btn.setOnClickListener {
            val tokenInputElem: TextInputEditText = view.findViewById(R.id.token_input)
            val token = tokenInputElem.text.toString()
            BirdHttpClient.secondAuthPost(token)
            val fm = activity?.supportFragmentManager?.popBackStack()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TokenFragment().apply{}
    }
}