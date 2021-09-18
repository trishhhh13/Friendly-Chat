package com.trishala13kohad.friendlychat


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.trishala13kohad.friendlychat.daos.UserDao
import com.trishala13kohad.friendlychat.models.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var signInButton: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>
    private lateinit var gso: GoogleSignInOptions

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        progressBar = findViewById(R.id.progessBar)
        signInButton = findViewById(R.id.signInButton)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                    handleSignInResults(task)

                }
            }
        auth = Firebase.auth
        // Configure Google Sign In
         gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton.setOnClickListener {

            signIn()
        }
    }

    @DelicateCoroutinesApi
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in.
            updateUI(currentUser)
        }

    }

    @DelicateCoroutinesApi
    private fun handleSignInResults(task: Task<GoogleSignInAccount>) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            Log.d("Handle result", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w("Handle result", "Google sign in failed", e)
        }
    }

    @DelicateCoroutinesApi
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val fireBaseUser = auth.user

            withContext(Dispatchers.Main){
                    updateUI(fireBaseUser)
            }

        }
    }

    @DelicateCoroutinesApi
    private fun updateUI(fireBaseUser: FirebaseUser?) {

        if(fireBaseUser != null){
            val user = User(fireBaseUser.uid, fireBaseUser.displayName,
            fireBaseUser.photoUrl.toString())

            val userDao = UserDao()
            userDao.addUser(user)
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
        else {
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    @DelicateCoroutinesApi
    private fun signIn() {

        googleSignInClient.signOut()
        val i = googleSignInClient.signInIntent
        GlobalScope.launch(Dispatchers.IO) {
            resultLauncher.launch(i)
        }
    }
}