package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by michaelgonzalez on 3/28/18.
 */

public class ImageZoomFragment extends DialogFragment {
    public static final String ARG_IMAGE_FILE = "imageFile";
    private ImageView mImageView;
    private File mPhotoFile;

    public static ImageZoomFragment newInstance(File imageFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_FILE, imageFile); // Date received from CrimeFragment


        ImageZoomFragment fragment = new ImageZoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image_zoom, null);
        mImageView = v.findViewById(R.id.imageView_image);
        mPhotoFile = (File) getArguments().getSerializable(ARG_IMAGE_FILE);

        Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());


        mImageView.setImageBitmap(bitmap);


        // Fluent interface for constructing an AlertDialog.Builder.
        // Retrieves the selected date and calls sendResult().
        return new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen).setView(v).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

    }




}
