package com.randomsymphony.games.ochre.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.randomsymphony.games.ochre.CardTableActivity;
import com.randomsymphony.games.ochre.R;

/**
 * Created by cyngus on 1/17/16.
 */
public class JoinDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder bob = new AlertDialog.Builder(getActivity());
        LayoutInflater helium = getActivity().getLayoutInflater();

        final View content = helium.inflate(R.layout.dialog_join, null);
        final EditText url = (EditText) content.findViewById(R.id.game_url);
        bob.setView(content);
        bob.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JoinDialog.this.getDialog().cancel();
            }
        });

        bob.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity activity = getActivity();
                if (activity instanceof CardTableActivity) {
                    ((CardTableActivity) activity).joinGame(url.getText().toString());
                }
            }
        });

        return bob.create();
    }
}
