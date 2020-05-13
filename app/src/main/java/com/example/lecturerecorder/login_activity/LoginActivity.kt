package com.example.lecturerecorder.login_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.AuthCredentials
import com.example.lecturerecorder.model.ErrorResponse
import com.example.lecturerecorder.model.LoginResponse
import com.example.lecturerecorder.model.RegisterResponse
import com.example.lecturerecorder.utils.RestClient
import com.example.lecturerecorder.utils.parseHttpErrorMessage
import com.example.lecturerecorder.utils.storeAuthToken
import com.example.lecturerecorder.view.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception


class LoginActivity : AppCompatActivity() {

    private lateinit var compositeDisposable: CompositeDisposable

    companion object {
        var RC_SIGN_IN: Int = 1111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        compositeDisposable = CompositeDisposable()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        sign_in_button.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        findViewById<Button>(R.id.register_button).setOnClickListener {
            register()
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            login()
        }
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            updateUI(account)
        } catch (e: ApiException) {
            Toast.makeText(
                this, resources.getString(R.string.login_failed),
                Toast.LENGTH_LONG
            ).show()
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account == null) {
            tv_checking_sign_in.visibility = View.GONE
            sign_in_button.visibility = View.VISIBLE
        } else {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }

    private fun register() {
        val username = findViewById<EditText>(R.id.register_login).text.toString()
        val password = findViewById<EditText>(R.id.register_password).text.toString()

        compositeDisposable.add(
            RestClient.authService.register(AuthCredentials(username, password))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::registerResponse, this::registerError))
    }

    private fun registerResponse(resp: RegisterResponse) {
        findViewById<EditText>(R.id.register_login).text.clear()
        findViewById<EditText>(R.id.register_password).text.clear()

        Snackbar.make(findViewById(R.id.register_button), "Registered Successfully", Snackbar.LENGTH_LONG).show()
    }

    private fun registerError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(findViewById(R.id.register_button), message, Snackbar.LENGTH_LONG).show()
    }

    private fun login() {
        val username = findViewById<EditText>(R.id.login_login).text.toString()
        val password = findViewById<EditText>(R.id.login_password).text.toString()

        compositeDisposable.add(
            RestClient.authService.login(AuthCredentials(username, password))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::loginResponse, this::loginError)
        )
    }

    private fun loginResponse(resp: LoginResponse) {
        storeAuthToken(resp.token)
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

    private fun loginError(error: Throwable) {
        val message = parseHttpErrorMessage(error)
        Snackbar.make(findViewById(R.id.login_button), message, Snackbar.LENGTH_LONG).show()
    }
}
