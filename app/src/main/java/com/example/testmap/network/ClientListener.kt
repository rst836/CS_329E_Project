package com.example.testmap.network

interface ClientListener {
    fun onUpdateBirdResults()

    fun onUpdateLimeResults()

    fun onUpdateBirdAccess()

    fun onUpdateLimeAccess()

    fun onFailedBirdAccess()

    fun onFailedLimeAccess()
}