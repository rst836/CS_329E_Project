package com.example.testmap.fragmentsManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.testmap.loginInfo.LoginInfo
import com.example.testmap.fragmentsManage.BirdLogin.FragmentBird
import com.example.testmap.R
import com.example.testmap.fragmentsManage.LimeLogin.FragmentLime

class ManageAccountItem(private val accountData:LoginInfo, private val loginType:String):Fragment(R.layout.fragment_manage_app_item) {

    private var showContent = false;

    private fun handleShowContent (view:View): View {
        if (showContent) {
            view.visibility = View.GONE
            showContent = false;
        } else {
            view.visibility = View.VISIBLE
            showContent = true
        }
        return view
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_app_item, container, false)

        val companyDropdown = view.findViewById<Button>(R.id.companyDropdown)
        companyDropdown.text = accountData.app.replaceFirstChar { char -> char.uppercase() }

        companyDropdown.setOnClickListener{
            val dropdownContent = view.findViewById<ConstraintLayout>(R.id.companyDropdownContent)
            handleShowContent(dropdownContent)
        }

        val accountEmail = view.findViewById<TextView>(R.id.appEmailLogin)
        if (accountData.email != null) {
            accountEmail.text = accountData.email
        } else {
            accountEmail.setText(R.string.loginNoEmail)
        }

        val accountPhone = view.findViewById<TextView>(R.id.appPhoneLogin)
        if (accountData.phone != null) {
            accountPhone.text = accountData.phone.toString()
        } else {
            accountPhone.setText(R.string.loginNoPhone)
        }

        val loginBtn = view.findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {

            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.addToBackStack(null)
            if (loginType == "bird") {
                transaction.replace(R.id.manageAccountsFragment, FragmentBird())
            } else if (loginType == "lime") {
                transaction.replace(R.id.manageAccountsFragment, FragmentLime())
            }
            transaction.commit()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(loginInfo:LoginInfo) : ManageAccountItem {
            val typeOut = loginInfo.app.trim().lowercase()
            return ManageAccountItem(loginInfo, typeOut)
        }

    }
}