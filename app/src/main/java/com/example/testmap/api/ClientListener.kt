package com.example.testmap.api

interface ClientListener {
    fun onUpdateBirdResults()

    fun onUpdateLimeResults()

    fun onUpdateBirdAccess()

    fun onUpdateLimeAccess()

    fun onFailedBirdAccess()

    fun onFailedLimeAccess()
}