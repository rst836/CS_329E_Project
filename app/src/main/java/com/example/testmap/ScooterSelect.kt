package com.example.testmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Button
import androidx.fragment.app.Fragment


/**
 * A simple [Fragment] subclass.
 * Use the [ScooterSelect.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScooterSelect : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scooter_select, container, false)
        val but = view.findViewById<Button>(R.id.button)
        val gesture = GestureDetector(activity, object : SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    activity?.supportFragmentManager?.popBackStack()
                    return super.onDoubleTap(e);
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    return false;
                }
            }
        )

        but.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://go.bird.co"))
            startActivity(i)
        }
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                return gesture.onTouchEvent(event);
            }
        })
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ScooterSelect().apply{
            }
    }
}