package com.example.testmap.fragmentsManage

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
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
            val parActivity: Activity? = activity
            if (parActivity != null && parActivity is MapsActivity) {
                val myActivity: MapsActivity = parActivity
                myActivity.viewModel.nextFrag.value = true
            }
        }

        view.findViewById<Button>(R.id.loginWithLimeBtn).setOnClickListener {
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.manageAccountsFragment, FragmentLime())
            transaction.commit()
            val parActivity: Activity? = activity
            if (parActivity != null && parActivity is MapsActivity) {
                val myActivity: MapsActivity = parActivity
                myActivity.viewModel.nextFrag.value = true
            }
        }

        val parActivity: Activity? = activity
        if (parActivity != null && parActivity is MapsActivity) {
            val myActivity: MapsActivity = parActivity
            val limeListen = myActivity.viewModel.currLime
            val birdListen = myActivity.viewModel.currBird
            val nextFrag = myActivity.viewModel.nextFrag
            val but = view.findViewById<Button>(R.id.loginWithBirdBtn)
            val butt = view.findViewById<Button>(R.id.loginWithLimeBtn)
            nextFrag.observe(viewLifecycleOwner, Observer{
                if (nextFrag.value == true){
                    but.isEnabled = false
                    but.isClickable = false
                    butt.isEnabled = false
                    butt.isClickable = false
                } else{
                    if (birdListen.value == false){
                        but.isEnabled = true
                        but.isClickable = true
                    }
                    if (limeListen.value == false){
                        butt.isEnabled = true
                        butt.isClickable = true
                    }
                }
            })
            birdListen.observe(viewLifecycleOwner, Observer{
                if (birdListen.value == true) {
                    but.setText(R.string.birdLoggedIn)
                    but.isEnabled = false
                    but.isClickable = false
                    but.alpha = 0.5f
                }
            })
            limeListen.observe(viewLifecycleOwner, Observer{
                if (limeListen.value == true) {
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
                    myActivity.viewModel.inManage.value = false
                    myActivity.mMap.uiSettings.setAllGesturesEnabled(true)
                } else if (count == 2) {
                    myActivity.viewModel.nextFrag.value = false
                }
            }
            activity?.supportFragmentManager!!.popBackStack()
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                val parActivity: Activity? = activity
                if (parActivity != null && parActivity is MapsActivity) {
                    val myActivity: MapsActivity = parActivity
                    val count = activity?.supportFragmentManager!!.backStackEntryCount
                    if (count == 1) {
                        myActivity.viewModel.inManage.value = false
                        myActivity.mMap.uiSettings.setAllGesturesEnabled(true)
                    } else if (count == 2) {
                        myActivity.viewModel.nextFrag.value = false
                    }
                }
                activity?.supportFragmentManager!!.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ManageAccountFragment().apply{
            }
    }
}