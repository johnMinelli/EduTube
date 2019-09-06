/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigation

import android.app.Activity
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.android.navigation.databinding.ActivityMainBinding
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.PersistableBundle
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.ImageDisplayConstants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.common.primitives.Ints.max
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import kotlinx.android.synthetic.main.nav_drawer_header.view.*
import java.util.*
import kotlin.math.max

class MainActivity : AppCompatActivity(){

    private val RC_SIGN_IN = 9001
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var toggle: ActionBarDrawerToggle

            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        //setup binding
        setupThemeStyle()
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        //setup drawer, navigation, appbar
        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.myNavHostFragment)
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                binding.toolbar,R.string.empty,R.string.empty
        )
        drawerLayout.addDrawerListener(toggle)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        // prevent nav gesture if not on start destination
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, bundle: Bundle? ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.toolbarSearchContainer?.setVisibility(View.GONE)
                toggle.syncState()
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = ""
            supportActionBar?.show()
        }
        NavigationUI.setupWithNavController(binding.navView, navController)
        ImageLoader.getInstance().init(getImageLoaderConfigurations(10, 50))

        setupDrawer()
        //auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val currentUser = mAuth.currentUser
        setupPref(currentUser)
        setupUI(currentUser)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Tag.GoogleAuth", "Google sign in failed", e)
                Snackbar.make(binding.drawerLayout, "Google authentication failed.", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun setupUI(user: FirebaseUser?) {
        val nav = binding.navView.getHeaderView(0)
        var role : Boolean = Globals().ROLE
        role = prefs.getBoolean(Constants().KEY_ROLE, role)
        if (user != null){
            //header
            inflateImage(nav.navHeader_imageView, user.photoUrl?.toString())
            nav.nav_header_user.text = user.displayName
            nav.nav_header_user.visibility = View.VISIBLE
            nav.sign_in_button.visibility = View.GONE
            //menu
            if(role == Constants().CREATOR){
                binding.navView.menu.setGroupVisible(R.id.menu_creator_group,true)
            }else{
                binding.navView.menu.setGroupVisible(R.id.menu_creator_group,false)
            }
            fire.collection("constants").document("admin").get().addOnSuccessListener {
                if(user.uid == it.get("uid")){
                    binding.navView.menu.setGroupVisible(R.id.menu_admin_group,true)
                }else{
                    binding.navView.menu.setGroupVisible(R.id.menu_admin_group,false)
                }
            }.addOnFailureListener{
                Log.w("Tag.MainActivity", "Get admin table failed", it)
                binding.navView.menu.setGroupVisible(R.id.menu_admin_group,false)
            }

        } else {
            //header
            inflateImage(nav.navHeader_imageView, "notavalidurl")
            nav.nav_header_user.visibility = View.GONE
            nav.sign_in_button.visibility = View.VISIBLE
            //menu
            binding.navView.menu.setGroupVisible(R.id.menu_creator_group,false)
            binding.navView.menu.setGroupVisible(R.id.menu_admin_group,false)
        }
    }

    private fun setupPref(user: FirebaseUser?){
        if(user != null){
            fire.collection("users").document(user.uid).get().addOnSuccessListener {
                if(!it.exists()){
                    fire.collection("users").document(user.uid).set(mutableMapOf(Pair("timestamp",Date()),
                            Pair("uid",user.uid),
                            Pair("displayName",user.displayName),
                            Pair("email",user.email)))
                }else {
                    (it.get("timestamp"))?.let {
                        if (!DateUtils.isToday((it as Timestamp).seconds*1000)) {
                            fire.collection("users").document(user.uid).get().addOnSuccessListener {
                                (it.get("interests"))?.let {
                                    val oldInt: MutableMap<String, Long> = it as MutableMap<String, Long>
                                    oldInt.forEach {
                                        oldInt[it.key] = max(0,it.value-2)
                                    }
                                    fire.collection("users").document(user.uid).update("interests", oldInt)
                                }
                            }
                            fire.collection("users").document(user.uid).update("timestamp", Date())
                        }
                    }
                }
            }
            prefs.edit().putBoolean(Constants().KEY_LOGGED,true)
                    .putString(Constants().UID,user.uid)
                    .putString(Constants().UNAME,user.displayName)
                    .putString(Constants().UIMAGE,user.photoUrl?.toString()).apply()
        }else{
            prefs.edit().putBoolean(Constants().KEY_LOGGED,false)
                    .putString(Constants().UID,null)
                    .putString(Constants().UNAME,null)
                    .putString(Constants().UIMAGE,null).apply()
        }
    }

    private fun setupDrawer() {
        val nav = binding.navView.getHeaderView(0)
        nav.navHeader_image.setOnClickListener { view: View ->
            if (mAuth.currentUser != null) {
                signOut()
                //revokeAccess()
            }
        }
        nav.sign_in_button.setOnClickListener { view: View ->
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        // Firebase sign out
        mAuth.signOut()
        // Google sign out
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {setupPref(null); setupUI(null) }
    }

    private fun revokeAccess() {
        // Firebase sign out
        mAuth.signOut()
        // Google revoke access
        mGoogleSignInClient!!.revokeAccess().addOnCompleteListener(this) { setupPref(null); setupUI(null) }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("Tag.GoogleAuth", "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    val user = mAuth.currentUser
                    if (task.isSuccessful && user!=null) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Tag.GoogleAuth", "signInWithCredential:success")
                        setupPref(user)
                        setupUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Tag.GoogleAuth", "signInWithCredential:failure", task.exception)
                        Snackbar.make(binding.drawerLayout, "Authentication failed.", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }

    fun setupThemeStyle(){
        var selectedTheme : String? = Globals().THEME
        selectedTheme = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants().KEY_THEME, selectedTheme)
        val style = when(selectedTheme) {
            getString(R.string.light_theme_key) -> R.style.LightSettingsTheme
            getString(R.string.dark_theme_key) -> R.style.DarkSettingsTheme
            getString(R.string.black_theme_key) -> R.style.BlackSettingsTheme
            else -> R.style.LightSettingsTheme
        }
        style.let{(this as Context).setTheme(style)}
    }

    private fun getImageLoaderConfigurations(memoryCacheSizeMb: Int,
                                             diskCacheSizeMb: Int): ImageLoaderConfiguration {
        return ImageLoaderConfiguration.Builder(this)
                .memoryCache(LRULimitedMemoryCache(memoryCacheSizeMb * 1024 * 1024))
                .diskCacheSize(diskCacheSizeMb * 1024 * 1024)
                .imageDownloader(BaseImageDownloader(applicationContext))
                .build()
    }

    fun inflateImage(view: ImageView, url: String?, needRepaint: Boolean = false){
        url?.let{
            val imageLoader = ImageLoader.getInstance()
            imageLoader.displayImage(url, view, ImageDisplayConstants.DISPLAY_USER_OPTIONS)
            if(needRepaint && !prefs.getString(Constants().KEY_THEME, Constants().LIGHT).equals(Constants().LIGHT)){
                val color = Color.WHITE
                val mode = PorterDuff.Mode.SRC_ATOP
                view.setColorFilter(color, mode)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("Tag.MainActivity", "onSupportNavigateUp()")
        //close the keyboard
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = this.currentFocus?:View(this)
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        //segue il behaviour del navigator
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onBackPressed() {
        if ((drawerLayout).isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }
}
