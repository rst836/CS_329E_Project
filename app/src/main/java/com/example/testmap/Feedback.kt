package com.example.testmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 * Use the [Feedback.newInstance] factory method to
 * create an instance of this fragment.
 */
class Feedback : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.feedback, container, false)
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val fm = activity?.supportFragmentManager?.popBackStack()
                return true
            }
        })
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Feedback().apply{
            }
    }
}