package com.ubit.myapplication
import android.Manifest;
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import com.ubit.myapplication.databinding.ActivityMainBinding


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var phone: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phone = findViewById(R.id.phone)

        // Set up the result launcher for picking a contact
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val contactUri: Uri? = data.data
                    if (contactUri != null) {
                        val phoneNumber = FetchPhone(contactUri) // Call the new function
                        phone.text = "Phone Num: $phoneNumber"
                    }
                }
            }
        }

        // Set up the button to pick a contact
        val FetchContactbutton: Button = findViewById(R.id.pickContactButton)
        FetchContactbutton.setOnClickListener {
            if (hasPermission()) {
                val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                resultLauncher.launch(pickContactIntent)
            } else {
                requestPermission()
            }
        }
    }
    // Check if READ_CONTACTS permission is granted
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    // Request READ_CONTACTS permission
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                resultLauncher.launch(pickContactIntent)
            }
        }
    }

    @SuppressLint("Range")
    // fetching the phone numbers
    private fun FetchPhone(contactUri: Uri): String? {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val cursor: Cursor? = contentResolver.query(contactUri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val phoneNumberCursor: Cursor? = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // Use Phone.CONTENT_URI
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?", // Use Phone.CONTACT_ID
                    arrayOf(id),
                    null
                )

                phoneNumberCursor?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        return phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                }
            }
        }

        return null
    }
}

const val CONTACTS_PERMISSION_REQUEST = 1