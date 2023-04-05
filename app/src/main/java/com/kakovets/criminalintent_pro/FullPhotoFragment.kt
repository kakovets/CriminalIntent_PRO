package com.kakovets.criminalintent_pro

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

class FullPhotoFragment: DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @Suppress("DEPRECATION")
        val photoFile: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("photo", File::class.java)
        } else {
            arguments?.getSerializable("photo") as File
        }
        val view = layoutInflater.inflate(R.layout.full_photo_fragment, null)
        val fullPhotoView: ImageView = view.findViewById(R.id.full_photo_view)
        var bit = BitmapFactory.decodeFile(photoFile?.path)
        val matrix = Matrix()
        matrix.postRotate(90f)
        bit = Bitmap.createBitmap(bit, 0, 0, bit.width, bit.height, matrix, true)

        fullPhotoView.setImageBitmap(bit)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)

        return builder.create()
    }

    companion object {
        fun newInstance(photoFile: File): FullPhotoFragment {
            val args = Bundle().apply {
                putSerializable("photo", photoFile)
            }
            return FullPhotoFragment().apply {
                arguments = args
            }
        }
    }
}