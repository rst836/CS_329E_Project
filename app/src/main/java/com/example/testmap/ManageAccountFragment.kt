package com.example.testmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.example.testmap.LoginInfo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

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


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ManageAccountFragment().apply{
            }
    }
}