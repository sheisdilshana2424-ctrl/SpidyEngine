package com.spidy.engine

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.spidy.engine.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedApkUri: Uri? = null
    private var selectedObbUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addGameButton.setOnClickListener {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${packageName}")
                startActivity(intent)
                return
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
                ActivityCompat.requestPermissions(this, permissions, 100)
                return
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
                return
            }
        }

        pickApkFile()
    }

    private val apkPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedApkUri = result.data?.data
            pickObbFile()
        }
    }

    private val obbPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedObbUri = result.data?.data
            processInstallation()
        }
    }

    private fun pickApkFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        apkPicker.launch(intent)
    }

    private fun pickObbFile() {
        Toast.makeText(this, "Now select the OBB file", Toast.LENGTH_LONG).show()
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        obbPicker.launch(intent)
    }

    private fun processInstallation() {
        val apkUri = selectedApkUri ?: return
        val obbUri = selectedObbUri ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.statusTextView.text = "Processing game files..."
        binding.addGameButton.isEnabled = false

        Thread {
            try {
                // 1. Get Package Name from APK (Simplified for this version)
                // In a real scenario, we'd use PackageManager.getPackageArchiveInfo
                // For now, we'll ask the user or try to infer. 
                // Let's assume the OBB filename contains the package name (common for OBBs)
                val obbFileName = getFileName(obbUri)
                val packageName = inferPackageName(obbFileName)

                if (packageName == null) {
                    runOnUiThread {
                        Toast.makeText(this, "Could not infer package name from OBB", Toast.LENGTH_LONG).show()
                        resetUI()
                    }
                    return@Thread
                }

                // 2. Copy OBB
                val obbDir = File(Environment.getExternalStorageDirectory(), "Android/obb/$packageName")
                if (!obbDir.exists()) obbDir.mkdirs()
                
                val destObbFile = File(obbDir, obbFileName)
                copyUriToFile(obbUri, destObbFile)

                // 3. Install APK
                runOnUiThread {
                    binding.statusTextView.text = "Installing APK..."
                    installApk(apkUri)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    resetUI()
                }
            }
        }.start()
    }

    private fun inferPackageName(obbFileName: String): String? {
        // OBB files are usually named main.version.package.name.obb
        val parts = obbFileName.split(".")
        if (parts.size < 4) return null
        // Find where "main" or "patch" is and take the rest until "obb"
        val startIndex = parts.indexOfFirst { it == "main" || it == "patch" }
        if (startIndex == -1) return null
        
        // Usually: main.12345.com.example.game.obb
        // parts[startIndex] = main
        // parts[startIndex+1] = 12345
        // parts[startIndex+2...last-1] = package name
        return parts.subList(startIndex + 2, parts.size - 1).joinToString(".")
    }

    private fun copyUriToFile(uri: Uri, destFile: File) {
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(8192)
                var length: Int
                while (input.read(buffer).also { length = it } > 0) {
                    output.write(buffer, 0, length)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "game.obb"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun installApk(uri: Uri) {
        // Copy APK to internal cache to get a file path for FileProvider
        val tempApk = File(cacheDir, "temp_install.apk")
        copyUriToFile(uri, tempApk)
        
        val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", tempApk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
        resetUI()
    }

    private fun resetUI() {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.statusTextView.text = "Ready to install your classic games"
            binding.addGameButton.isEnabled = true
        }
    }
}
