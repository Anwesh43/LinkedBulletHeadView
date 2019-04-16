package com.anwesh.uiprojects.linkedbulletheadview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.bulletheadview.BulletHeadView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BulletHeadView.create(this)
    }
}
