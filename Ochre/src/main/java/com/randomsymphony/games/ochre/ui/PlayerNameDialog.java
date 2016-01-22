package com.randomsymphony.games.ochre.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.randomsymphony.games.ochre.R;

public class PlayerNameDialog extends DialogFragment {

    public static final String ARG_START_NAME = "name";

    public interface Listener {
        public void onNameSet(String name);
    }

    public static PlayerNameDialog create(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_START_NAME, name);
        PlayerNameDialog dialog = new PlayerNameDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private Listener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater helium =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = helium.inflate(R.layout.dialog_set_name, null);
        final EditText playerName = (EditText) content.findViewById(R.id.player_name);
        String name = getArguments().getString(ARG_START_NAME);
        playerName.setText(name);
        playerName.setSelection(name.length());

        AlertDialog.Builder bob = new AlertDialog.Builder(getActivity());
        bob.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlayerNameDialog.this.dismiss();
            }
        });

        bob.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onNameSet(playerName.getText().toString());
                }
                PlayerNameDialog.this.dismiss();
            }
        });

        bob.setView(content);

        return bob.create();
    }

    public void registerListener(Listener listener) {
        mListener = listener;
    }
}