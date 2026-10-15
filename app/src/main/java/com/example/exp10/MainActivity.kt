package com.example.exp10

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class Contact(val name: String, val number: String)

class MainActivity : AppCompatActivity() {

    private lateinit var etNewName: EditText
    private lateinit var etNewPhone: EditText
    private lateinit var btnSaveContact: Button
    private lateinit var btnFetchContacts: Button
    private lateinit var etSearchContact: EditText
    private lateinit var listViewContacts: ListView
    private var adapter: ContactAdapter? = null
    private val contactsList = ArrayList<Contact>()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] ?: false

        if (readGranted) {
            loadContacts()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }

        if (!writeGranted) {
            Toast.makeText(this, getString(R.string.write_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNewName = findViewById(R.id.etNewName)
        etNewPhone = findViewById(R.id.etNewPhone)
        btnSaveContact = findViewById(R.id.btnSaveContact)
        btnFetchContacts = findViewById(R.id.btnFetchContacts)
        etSearchContact = findViewById(R.id.etSearchContact)
        listViewContacts = findViewById(R.id.listViewContacts)

        adapter = ContactAdapter(
            this,
            contactsList,
            onItemClick = { contact -> showContactDetailsDialog(contact) },
            onShareClick = { contact -> shareContactData(contact) }
        )
        listViewContacts.adapter = adapter

        listViewContacts.setOnItemClickListener { _, _, position, _ ->
            val contact = adapter?.getItem(position)
            if (contact != null) {
                showContactDetailsDialog(contact)
            }
        }

        btnSaveContact.setOnClickListener {
            val name = etNewName.text.toString().trim()
            val phone = etNewPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_details), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                saveContactToProvider(name, phone)
            } else {
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                    )
                )
            }
        }

        btnFetchContacts.setOnClickListener {
            checkPermissionsAndFetchContacts()
        }

        etSearchContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        checkPermissionsAndFetchContacts()
    }

    private fun checkPermissionsAndFetchContacts() {
        val hasRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasWrite = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasRead && hasWrite) {
            loadContacts()
        } else {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    private fun saveContactToProvider(name: String, phone: String) {
        try {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .build()
            )

            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

            Toast.makeText(this, getString(R.string.contact_saved_success), Toast.LENGTH_SHORT).show()

            etNewName.text.clear()
            etNewPhone.text.clear()

            loadContacts()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving contact: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        contactsList.clear()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val name = if (nameIndex != -1) it.getString(nameIndex) ?: "Unknown" else "Unknown"
                    val number = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""
                    if (name.isNotEmpty() || number.isNotEmpty()) {
                        contactsList.add(Contact(name, number))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (contactsList.isEmpty()) {
            contactsList.addAll(
                listOf(
                    Contact("Alice Smith", "+1 555-0101"),
                    Contact("Bob Johnson", "+1 555-0102"),
                    Contact("Charlie Brown", "+1 555-0103"),
                    Contact("Diana Prince", "+1 555-0104"),
                    Contact("Evan Wright", "+1 555-0105")
                )
            )
            Toast.makeText(this, getString(R.string.sample_contacts_loaded), Toast.LENGTH_SHORT).show()
        }

        adapter?.updateData(ArrayList(contactsList))

        val currentQuery = etSearchContact.text.toString()
        if (currentQuery.isNotEmpty()) {
            adapter?.filter?.filter(currentQuery)
        }
    }

    private fun showContactDetailsDialog(contact: Contact) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contact_details, null)

        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailName)
        val tvPhone = dialogView.findViewById<TextView>(R.id.tvDetailPhone)
        val btnCall = dialogView.findViewById<Button>(R.id.btnDetailCall)
        val btnShare = dialogView.findViewById<Button>(R.id.btnDetailShare)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDetailClose)

        tvName.text = contact.name
        tvPhone.text = contact.number

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.details_dialog_title))
            .setView(dialogView)
            .create()

        btnCall.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, "tel:${contact.number}".toUri())
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, "Unable to launch dialer", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            dialog.dismiss()
            shareContactData(contact)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shareContactData(contact: Contact) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Contact Details:\nName: ${contact.name}\nPhone: ${contact.number}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_contact_title))
        startActivity(shareIntent)
    }
}

class ContactAdapter(
    context: Context,
    private var allContacts: List<Contact>,
    private val onItemClick: (Contact) -> Unit,
    private val onShareClick: (Contact) -> Unit
) : ArrayAdapter<Contact>(context, 0, allContacts), Filterable {

    private var filteredContacts: List<Contact> = allContacts

    override fun getCount(): Int = filteredContacts.size

    override fun getItem(position: Int): Contact? = filteredContacts.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_contact, parent, false
        )
        val contact = getItem(position) ?: return view

        val tvName = view.findViewById<TextView>(R.id.tvContactName)
        val tvNumber = view.findViewById<TextView>(R.id.tvContactNumber)
        val btnShare = view.findViewById<ImageButton>(R.id.btnShareContact)

        tvName.text = contact.name
        tvNumber.text = contact.number

        btnShare.setOnClickListener {
            onShareClick(contact)
        }

        view.setOnClickListener {
            onItemClick(contact)
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim().orEmpty()
                val filtered = if (query.isEmpty()) {
                    allContacts
                } else {
                    allContacts.filter {
                        it.name.lowercase().contains(query) || it.number.contains(query)
                    }
                }
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredContacts = results?.values as? List<Contact> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    fun updateData(newContacts: List<Contact>) {
        allContacts = newContacts
        filteredContacts = newContacts
        notifyDataSetChanged()
    }
}
