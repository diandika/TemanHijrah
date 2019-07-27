package com.example.temanhijrah;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

@SuppressLint("ValidFragment")
public class AlarmFragment extends DialogFragment {
    private int positionDering = 0;
    private int positionWaktu = 0;
    String title = "";

    @SuppressLint("ValidFragment")
    public AlarmFragment(String waktu) {
        title = waktu;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String[] listJenis = getActivity().getResources().getStringArray(R.array.jenis);
        String[] listWaktu = getActivity().getResources().getStringArray(R.array.waktu);

        builder.setTitle("Adzan " + title)
                .setSingleChoiceItems(listJenis, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        positionDering = which;
                    }
                });
        return builder.create();
    }
}
