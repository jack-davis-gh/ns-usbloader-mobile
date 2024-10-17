package com.blogspot.developersu.ns_usbloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blogspot.developersu.ns_usbloader.about.AboutScreen
import com.blogspot.developersu.ns_usbloader.about.AboutUi
import com.blogspot.developersu.ns_usbloader.about.navigateToAbout
import com.blogspot.developersu.ns_usbloader.home.HomeScreen
import com.blogspot.developersu.ns_usbloader.home.HomeUi
import com.blogspot.developersu.ns_usbloader.settings.SettingsScreen
import com.blogspot.developersu.ns_usbloader.settings.SettingsUi
import com.blogspot.developersu.ns_usbloader.settings.navigateToSettings
import com.blogspot.developersu.ns_usbloader.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.vinceglb.filekit.core.FileKit

// TODO: add ImageAsset for notification icon instead of SVG-like
// TODO NOTE: If service execution has been finished in background, then no UI updates will come
@AndroidEntryPoint
class MainActivity : AppCompatActivity() { // , NsResultReciever.Receiver,
//    private var mDataset: ArrayList<NSPElement>? = null
//
//    private var innerBroadcastReceiver: BroadcastReceiver? = null
//
//    private var usbDevice: UsbDevice? = null
//    private var isUsbDeviceAccessible = false
//
//    private var selectBtn: Button? = null
//    private var uploadToNsBtn: Button? = null
//    private var progressBarMain: ProgressBar? = null
//
//    private var nsResultReciever: NsResultReciever? = null
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelableArrayList("DATASET_LIST", mDataset)
//        outState.putParcelable("USB_DEVICE", usbDevice)
//        outState.putBoolean("IS_USB_DEV_ACCESSIBLE", isUsbDeviceAccessible)
//        if (drawerNavView!!.checkedItem != null) outState.putInt(
//            "PROTOCOL", drawerNavView!!.checkedItem!!
//                .itemId
//        )
//        else outState.putInt("PROTOCOL", R.id.nav_tf_usb)
//        outState.putParcelable("RECEIVER", nsResultReciever)
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
//    override fun onResume() {
//        super.onResume()
//        // Configure intent to receive attached NS
//        innerBroadcastReceiver = InnerBroadcastReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
//        intentFilter.addAction(NsConstants.REQUEST_NS_ACCESS_INTENT)
//        intentFilter.addAction(NsConstants.SERVICE_TRANSFER_TASK_FINISHED_INTENT)
//        registerReceiver(innerBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
//        nsResultReciever!!.setReceiver(this)
//        blockUI(isServiceActive)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(innerBroadcastReceiver)
//        val preferencesEditor = getSharedPreferences("NSUSBloader", MODE_PRIVATE).edit()
//        if (drawerNavView!!.checkedItem != null) {
//            val itemId = drawerNavView!!.checkedItem!!.itemId
//            if (itemId == R.id.nav_tf_usb) {
//                preferencesEditor.putInt("PROTOCOL", NsConstants.PROTO_TF_USB)
//            } else if (itemId == R.id.nav_tf_net) {
//                preferencesEditor.putInt("PROTOCOL", NsConstants.PROTO_TF_NET)
//            } else if (itemId == R.id.nav_gl) {
//                preferencesEditor.putInt("PROTOCOL", NsConstants.PROTO_GL_USB)
//            }
//        } else preferencesEditor.putInt("PROTOCOL", NsConstants.PROTO_TF_USB)
//        preferencesEditor.apply()
//        nsResultReciever!!.setReceiver(null)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = HomeUi) {
                    composable<HomeUi> { HomeScreen(navController::navigateToSettings, navController::navigateToAbout) }
                    composable<SettingsUi> { SettingsScreen(onBackPressed = { navController.popBackStack() }) }
                    composable<AboutUi> { AboutScreen(onBackPressed = { navController.popBackStack() }) }
                }
            }
        }
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        if (savedInstanceState != null) {
//            mDataset = savedInstanceState.getParcelableArrayList("DATASET_LIST")
//            // Restore USB device information
//            usbDevice = savedInstanceState.getParcelable("USB_DEVICE")
//            isUsbDeviceAccessible = savedInstanceState.getBoolean("IS_USB_DEV_ACCESSIBLE", false)
////            drawerNavView?.setCheckedItem(savedInstanceState.getInt("PROTOCOL", R.id.nav_tf_usb))
//            nsResultReciever = savedInstanceState.getParcelable("RECEIVER")
//        } else {
//            mDataset = ArrayList()
//            usbDevice =
//                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) // If it's started initially, then check if it's started from notification.
//            //Log.i("LPR", "DEVICE " +usbDevice);
//            if (usbDevice == null) {
//                isUsbDeviceAccessible = false
//            } else {
//                val usbManager = getSystemService(USB_SERVICE) as UsbManager
//                // If somehow we can't get system service
//                if (usbManager == null) {
//                    getAlertWindow(
//                        this,
//                        resources.getString(R.string.popup_error),
//                        "Internal issue: getSystemService(Context.USB_SERVICE) returned null"
//                    )
//                    return  // ??? HOW ???
//                }
//                isUsbDeviceAccessible = usbManager.hasPermission(usbDevice)
//            }
//            val preferences = getSharedPreferences("NSUSBloader", MODE_PRIVATE)
//            setApplicationTheme(preferences.getInt("ApplicationTheme", 0))
//
//            when (preferences.getInt("PROTOCOL", NsConstants.PROTO_TF_USB)) {
//                NsConstants.PROTO_TF_USB -> drawerNavView?.setCheckedItem(R.id.nav_tf_usb)
//                NsConstants.PROTO_TF_NET -> drawerNavView?.setCheckedItem(R.id.nav_tf_net)
//                NsConstants.PROTO_GL_USB -> drawerNavView?.setCheckedItem(R.id.nav_gl)
//                else -> {}
//            }
//            nsResultReciever =
//                NsResultReciever(Handler()) // We will set callback in onResume and unset onPause
//        }
//
//        mAdapter = NspItemsAdapter(mDataset!!)
//        recyclerView?.setAdapter(mAdapter)
//        this.setSwipeFunctionsToView()
//        // Select files button
//        selectBtn = findViewById(R.id.buttonSelect)
//        selectBtn?.setOnClickListener { e: View? ->
//            val fileChooser = Intent(Intent.ACTION_GET_CONTENT)
//            fileChooser.setType("application/octet-stream") //fileChooser.setType("*/*"); ???
//            if (fileChooser.resolveActivity(packageManager) != null) startActivityForResult(
//                Intent.createChooser(
//                    fileChooser,
//                    getString(R.string.select_file_btn)
//                ), ADD_NSP_INTENT_CODE
//            )
//            else getAlertWindow(
//                this,
//                resources.getString(R.string.popup_error),
//                resources.getString(R.string.install_file_explorer)
//            )
//        }
//        // Upload to NS button
//        uploadToNsBtn = findViewById(R.id.buttonUpload)
//
//        //check if it's from file selected
//        val intent = intent
//        val uri = intent.data
//
//        if (savedInstanceState == null && uri != null) {
//            readFile(intent)
//        }
//    }

//    private fun updateUploadBtnState() {    // TODO: this function is bad. It multiplies entropy and sorrow.
//        uploadToNsBtn!!.isEnabled = mAdapter!!.itemCount > 0
//    }
//
//    /**
//     * @see MainActivity.onCreate
//     *
//     */
//    private fun setSwipeFunctionsToView() {
//        val ithCallBack: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
//            override fun getMovementFlags(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder
//            ): Int {
//                return makeMovementFlags(
//                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//                )
//            }
//
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                mAdapter!!.move(viewHolder.adapterPosition, target.adapterPosition)
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val nspViewHolder = viewHolder as NspViewHolder
//                mDataset!!.remove(nspViewHolder.data!!)
//                mAdapter!!.notifyDataSetChanged()
//                updateUploadBtnState()
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(ithCallBack)
//        itemTouchHelper.attachToRecyclerView(recyclerView)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        //Snackbar.make(findViewById(android.R.id.content), requestCode+" "+resultCode, Snackbar.LENGTH_SHORT).show();
//        if (requestCode != ADD_NSP_INTENT_CODE || data == null) return
//
//        readFile(data)
//    }
//
//    private fun readFile(data: Intent) {
//        val uri = data.data
//
//        if (uri == null || uri.scheme == null || uri.scheme != "content") return
//
//        val fileName = getFileNameFromUri(uri, this)
//        val fileSize = getFileSizeFromUri(uri, this)
//
//        if (fileName == null || fileSize < 0) {
//            getAlertWindow(
//                this,
//                resources.getString(R.string.popup_error),
//                resources.getString(R.string.popup_incorrect_file)
//            )
//            return
//        }
//
//        val fileExtension = fileName.replace("^.*\\.".toRegex(), "").lowercase(Locale.getDefault())
//        when (fileExtension) {
//            "nsp", "nsz", "xci", "xcz" -> {}
//            else -> {
//                getAlertWindow(
//                    this,
//                    resources.getString(R.string.popup_error),
//                    resources.getString(R.string.popup_non_supported_format)
//                )
//                return
//            }
//        }
//
//        var isAlreadyAddedElement = false
//
//        for (element in mDataset!!) {
//            if (element.filename == fileName) {
//                isAlreadyAddedElement = true
//                break
//            }
//        }
//
//        if (isAlreadyAddedElement) return
//
//        val element = NSPElement(uri, fileName, fileSize)
//        if (drawerNavView!!.checkedItem != null)  // && drawerNavView.getCheckedItem().getItemId() != R.id.nav_gl
//            element.isSelected = true
//        mDataset!!.add(element)
//        mAdapter!!.notifyDataSetChanged()
//        // Enable upload button
//        updateUploadBtnState()
//    }
//
//    private fun uploadFiles() {
//        val NSPElementsToSend = ArrayList<NSPElement>()
//        for (element in mDataset!!) {
//            if (element.isSelected) NSPElementsToSend.add(element)
//        }
//        // Do we have files to send?
//        if (NSPElementsToSend.isEmpty()) {
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                getString(R.string.nothing_selected_message),
//                Snackbar.LENGTH_LONG
//            ).show()
//            return
//        }
//        // Do we have selected protocol?
//        if (drawerNavView!!.checkedItem == null) {
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                getString(R.string.no_protocol_selected_message),
//                Snackbar.LENGTH_LONG
//            ).show()
//            return
//        }
//        val serviceStartIntent = Intent(
//            this,
//            CommunicationsService::class.java
//        )
//        serviceStartIntent.putExtra(NsConstants.NS_RESULT_RECEIVER, nsResultReciever)
//        serviceStartIntent.putParcelableArrayListExtra(
//            NsConstants.SERVICE_CONTENT_NSP_LIST,
//            NSPElementsToSend
//        )
//        // Is it TF Net transfer?
//        if (drawerNavView!!.checkedItem!!.itemId == R.id.nav_tf_net) {
//            serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_PROTOCOL,
//                NsConstants.PROTO_TF_NET
//            )
//            val sp = getSharedPreferences("NSUSBloader", MODE_PRIVATE)
//
//            serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_NS_DEVICE_IP,
//                sp.getString("SNsIP", "192.168.1.42")
//            )
//            if (sp.getBoolean(
//                    "SAutoIP",
//                    true
//                )
//            ) serviceStartIntent.putExtra(NsConstants.SERVICE_CONTENT_PHONE_IP, "")
//            else serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_PHONE_IP,
//                sp.getString("SServerIP", "192.168.1.142")
//            )
//            serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_PHONE_PORT,
//                sp.getInt("SServerPort", 6042)
//            )
//            startService(serviceStartIntent)
//            blockUI(true)
//            return
//        }
//        // Ok, so it's something USB related
//        val usbManager = getSystemService(USB_SERVICE) as UsbManager
//        // Do we have manager?
//        if (usbManager == null) {
//            getAlertWindow(
//                this,
//                resources.getString(R.string.popup_error),
//                "Internal issue: getSystemService(Context.USB_SERVICE) returned null"
//            )
//            return
//        }
//        // If device not connected
//        if (usbDevice == null) {
//            val deviceHashMap: HashMap<String, UsbDevice> = usbManager.deviceList
//
//            for (device in deviceHashMap.values) {
//                if (device.vendorId == 1406 && device.productId == 12288) {
//                    usbDevice = device
//                }
//            }
//            // If it's still not connected then it's really not connected.
//            if (usbDevice == null) {
//                getAlertWindow(
//                    this,
//                    resources.getString(R.string.popup_error),
//                    resources.getString(R.string.ns_not_found_in_connected)
//                )
//                return
//            }
//            // If we have NS connected check for permissions
//            if (!usbManager.hasPermission(usbDevice)) {
//                usbManager.requestPermission(
//                    usbDevice,
//                    PendingIntent.getBroadcast(
//                        this,
//                        0,
//                        Intent(NsConstants.REQUEST_NS_ACCESS_INTENT),
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                ) // TODO Needs to be 0
//                return
//            }
//        }
//        if (!isUsbDeviceAccessible) {
//            usbManager.requestPermission(
//                usbDevice,
//                PendingIntent.getBroadcast(
//                    this,
//                    0,
//                    Intent(NsConstants.REQUEST_NS_ACCESS_INTENT),
//                    PendingIntent.FLAG_IMMUTABLE
//                )
//            ) // TODO Needs to be 0
//            return
//        }
//
//        val itemId = drawerNavView!!.checkedItem!!.itemId
//        if (itemId == R.id.nav_tf_usb) {
//            serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_PROTOCOL,
//                NsConstants.PROTO_TF_USB
//            )
//        } else if (itemId == R.id.nav_gl) {
//            serviceStartIntent.putExtra(
//                NsConstants.SERVICE_CONTENT_PROTOCOL,
//                NsConstants.PROTO_GL_USB
//            )
//        } else {
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                getString(R.string.unknown_protocol_error),
//                Snackbar.LENGTH_LONG
//            ).show() // ?_?
//        }
//
//        serviceStartIntent.putExtra(NsConstants.SERVICE_CONTENT_NS_DEVICE, usbDevice)
//        startService(serviceStartIntent)
//        blockUI(true)
//    }
//
//    private fun blockUI(shouldBlock: Boolean) {
//        if (shouldBlock) {
//            selectBtn!!.isEnabled = false
//            recyclerView!!.suppressLayout(true)
//            uploadToNsBtn!!.setCompoundDrawablesWithIntrinsicBounds(
//                null, ContextCompat.getDrawable(
//                    this, R.drawable.ic_cancel
//                ), null, null
//            )
//            uploadToNsBtn!!.setText(R.string.interrupt_btn)
//            uploadToNsBtn!!.setOnClickListener { e: View? ->
//                stopService(
//                    Intent(
//                        this,
//                        CommunicationsService::class.java
//                    )
//                )
//            }
//            progressBarMain!!.visibility = ProgressBar.VISIBLE
//            progressBarMain!!.isIndeterminate = true //TODO
//            uploadToNsBtn!!.isEnabled = true
//            return
//        }
//        selectBtn!!.isEnabled = true
//        recyclerView!!.suppressLayout(false)
//        uploadToNsBtn!!.setCompoundDrawablesWithIntrinsicBounds(
//            null,
//            ContextCompat.getDrawable(this, R.drawable.ic_upload_btn),
//            null,
//            null
//        )
//        uploadToNsBtn!!.setText(R.string.upload_btn)
//        uploadToNsBtn!!.setOnClickListener { e: View? -> this.uploadFiles() }
//        progressBarMain!!.visibility = ProgressBar.INVISIBLE
//        this.updateUploadBtnState()
//    }
//
//    /**
//     * Handle service updates
//     */
//    override fun onReceiveResults(code: Int, bundle: Bundle?) {
//        if (code == NsConstants.NS_RESULT_PROGRESS_INDETERMINATE) progressBarMain!!.isIndeterminate =
//            true
//        else {                                                                       // Else NsConstants.NS_RESULT_PROGRESS_VALUE ; ALSO THIS PART IS FULL OF SHIT
//            if (progressBarMain!!.isIndeterminate) progressBarMain!!.isIndeterminate = false
//            progressBarMain!!.progress = bundle?.getInt("POSITION") ?: 0
//        }
//    }
//
//    /**
//     * Deal with global broadcast intents
//     */
//    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == null) return
//            when (intent.action) {
//                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
//                    usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
//                    val usbManager =
//                        context.getSystemService(USB_SERVICE) as UsbManager
//                            ?: return
//                    // ???
//
//                    if (usbManager.hasPermission(usbDevice)) isUsbDeviceAccessible = true
//                    else {
//                        isUsbDeviceAccessible = false
//                        usbManager.requestPermission(
//                            usbDevice,
//                            PendingIntent.getBroadcast(
//                                context,
//                                0,
//                                Intent(NsConstants.REQUEST_NS_ACCESS_INTENT),
//                                PendingIntent.FLAG_IMMUTABLE
//                            )
//                        ) //TODO Needs to be 0
//                    }
//                }
//
//                NsConstants.REQUEST_NS_ACCESS_INTENT -> isUsbDeviceAccessible =
//                    intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
//
//                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
//                    usbDevice = null
//                    isUsbDeviceAccessible = false
//                    stopService(Intent(context, CommunicationsService::class.java))
//                }
//
//                NsConstants.SERVICE_TRANSFER_TASK_FINISHED_INTENT -> {
//                    val nspElements =
//                        intent.getParcelableArrayListExtra<NSPElement>(NsConstants.SERVICE_CONTENT_NSP_LIST)
//                            ?: return
//                    var i = 0
//                    while (i < mDataset!!.size) {
//                        for (receivedNSPe in nspElements) if (receivedNSPe.filename == mDataset!![i].filename) mDataset!![i].status =
//                            receivedNSPe.status
//                        i++
//                    }
//                    mAdapter!!.notifyDataSetChanged()
//                    blockUI(false)
//                }
//            }
//        }
//    }
//
//    companion object {
//        //TODO: FIX?
//        private const val ADD_NSP_INTENT_CODE = 1
//
//        init {
//            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
//        }
//    }
}
