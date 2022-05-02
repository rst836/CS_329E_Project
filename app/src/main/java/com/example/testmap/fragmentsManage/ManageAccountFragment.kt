package com.example.testmap.fragmentsManage

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.*
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

        val returnBtn = view.findViewById<Button>(R.id.returnBtn)
        returnBtn.setOnClickListener {
            activity?.supportFragmentManager!!.popBackStack()
        }
        val parActivity: Activity? = activity
        if (parActivity != null && parActivity is MapsActivity) {
            val myActivity: MapsActivity = parActivity
            if(myActivity.birdIsLoggedIn){
                view.findViewById<Button>(R.id.loginWithBirdBtn).setText(R.string.birdLoggedIn)
            }
            if(myActivity.limeIsLoggedIn){
                view.findViewById<Button>(R.id.loginWithLimeBtn).setText(R.string.limeLoggedIn)
            }
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