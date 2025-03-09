package hu.dancsomarci.signbuddy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SignBuddyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        //authService = FirebaseAuthService(FirebaseAuth.getInstance())
    }

//    companion object{
//        lateinit var authService: AuthService
//    }
}