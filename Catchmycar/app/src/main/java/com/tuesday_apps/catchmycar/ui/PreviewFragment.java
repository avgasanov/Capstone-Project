package com.tuesday_apps.catchmycar.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewFragment extends DialogFragment {

    private static final String ARG_FILENAME = "param1";

    private String mFileName;

    private OnFragmentInteractionListener mListener;

    public PreviewFragment() {
        // Required empty public constructor
    }

    public static PreviewFragment newInstance(String fileName) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILENAME, fileName);
        fragment.setArguments(args);
        return fragment;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mFileName = getArguments().getString(ARG_FILENAME);
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      View view = inflater.inflate(R.layout.fragment_preview, null);
      ImageView imageView = view.findViewById(R.id.preview_iv);
      //imageView.setImageBitmap(BitmapUtils.getBitmapFromFile(getContext(), mFileName));
        Glide.with(this).load(mFileName).into(imageView);

        builder.setTitle(R.string.preview)
                .setView(view)
                .setPositiveButton(R.string.approve, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(PreviewFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(PreviewFragment.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
