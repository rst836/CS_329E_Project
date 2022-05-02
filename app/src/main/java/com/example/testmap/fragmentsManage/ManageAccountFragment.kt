package com.example.testmap.fragmentsManage

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.example.testmap.MapsActivity
import com.example.testmap.R
import com.example.testmap.fragmentsManage.BirdLogin.FragmentBird
import com.example.testmap.fragmentsManage.LimeLogin.FragmentLime

class ManageAccountFragment:Fragment(R.layout.fragment_manage_layout) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_layout, container, false)

        view.findViewById<Button>(R.id.loginWithBirdBtn).setOnClickListener {
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.manageAccountsFragment, FragmentBird())
            transaction.commit()
        }

        view.findViewById<Button>(R.id.loginWithLimeBtn).setOnClickListener {
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.manageAccountsFragment, FragmentLime())
            transaction.commit()
        }

        val parActivity: Activity? = activity
        if (parActivity != null && parActivity is MapsActivity) {
            val myActivity: MapsActivity = parActivity
            val limeListen = myActivity.viewModel.currLime
            val birdListen = myActivity.viewModel.currBird
            birdListen.observe(viewLifecycleOwner, Observer{
                if (birdListen.value == true) {
                    val but = view.findViewById<Button>(R.id.loginWithBirdBtn)
                    but.setText(R.string.birdLoggedIn)
                    but.isEnabled = false
                    but.isClickable = false
                    but.alpha = 0.5f
                }
            })
            limeListen.observe(viewLifecycleOwner, Observer{
                if (limeListen.value == true) {
                    val butt = view.findViewById<Button>(R.id.loginWithLimeBtn)
                    butt.setText(R.string.limeLoggedIn)
                    butt.isEnabled = false
                    butt.isClickable = false
                    butt.alpha = 0.5f
                }
            })
        }

        val returnBtn = view.findViewById<Button>(R.id.returnBtn)
        returnBtn.setOnClickListener {
            val parActivity: Activity? = activity
            if (parActivity != null && parActivity is MapsActivity) {
                val myActivity: MapsActivity = parActivity
                val count = activity?.supportFragmentManager!!.backStackEntryCount
                if (count == 1) {
                    myActivity.inManage = false
                }
            }
            activity?.supportFragmentManager!!.popBackStack()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ManageAccountFragment().apply{
            }
    }
}