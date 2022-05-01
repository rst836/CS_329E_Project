package com.example.testmap.fragmentsManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.example.testmap.loginInfo.*
import com.example.testmap.Main
import com.example.testmap.R

class ManageAccountFragment:Fragment(R.layout.fragment_manage_layout) {
    private val loginViewModel:LoginInfoViewModel by viewModels {
        LoginInfoViewModelFactory((activity?.application as Main).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_layout, container, false)
        loginViewModel.allLogins.observe(viewLifecycleOwner, Observer<List<LoginInfo>>{ loginList ->
            // update
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            loginList.forEach { loginInfo ->
                val fragment = ManageAccountItem.newInstance(loginInfo)
                transaction.add(R.id.appLinearLayout, fragment)
            }
            transaction.commit()

        })
//        val returnBtn = view.findViewById<Button>(R.id.returnBtn)
//        returnBtn.setOnClickListener {
//            activity?.supportFragmentManager!!.popBackStack()
//        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ManageAccountFragment().apply{
            }
    }
}