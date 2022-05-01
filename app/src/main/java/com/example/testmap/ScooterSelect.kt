package com.example.testmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView


private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [ScooterSelect.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScooterSelect : Fragment() {
    private var isLime: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isLime = it.getBoolean(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scooter_select, container, false)
        val text = view.findViewById<TextView>(R.id.textView)
        val but = view.findViewById<Button>(R.id.button)
        if (isLime == true){
            text.setText(R.string.hello_blank_fragment_lime)
        }
        else{
            text.setText(R.string.hello_blank_fragment_bird)
        }
        but.setOnClickListener {
            val i: Intent
            if (isLime == true){
                i = Intent(Intent.ACTION_VIEW, Uri.parse("https://limebike.app.link/m2h6hB9qrS"))
            }
            else{
                i = Intent(Intent.ACTION_VIEW, Uri.parse("https://go.bird.co"))
            }
            startActivity(i)
        }
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                activity?.supportFragmentManager?.popBackStack()
                return true
            }
        })
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(isLimeVal: Boolean) =
            ScooterSelect().apply{
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, isLimeVal)
                }
            }
    }
}