package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.BuildConfig;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.util.Utility;

public class AboutFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Resources resources = getActivity().getResources();
        String version = resources.getText(R.string.version) + " " + BuildConfig.VERSION_NAME;

        TextView aboutText = view.findViewById(R.id.about_version);
        aboutText.setText(version);

        final Button rateAppButton = (Button) view.findViewById(R.id.button_rate_app);
        rateAppButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                rateApp();
            }
        });


        Button feedbackButton = (Button) view.findViewById(R.id.button_feedback);
        feedbackButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        setHasOptionsMenu(false);

        return view;
    }

    private void sendFeedback() {
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        startActivity(Intent.createChooser(intent, getString(R.string.mail_feedback_title)));
    }

    private void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.sjmeunier.arborfamiliae"));
        if (!startMarketActivity(intent)) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.sjmeunier.arborfamiliae"));
            if (!startMarketActivity(intent)) {
                Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.error_market_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean startMarketActivity(Intent aIntent) {
        try
        {
            startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }
}
