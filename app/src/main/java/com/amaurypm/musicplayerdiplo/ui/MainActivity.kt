package com.amaurypm.musicplayerdiplo.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.amaurypm.musicplayerdiplo.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Instanciamos el NavHost
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)

        if(navHostFragment != null){
            navController = navHostFragment.findNavController()

            //Para que se genere la action bar para la navegación
            NavigationUI.setupActionBarWithNavController(this, navController)
            //Para ponerle color personalizado al action bar
            supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.actionBarColor)))

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }
}