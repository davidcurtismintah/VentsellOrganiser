package com.ventsell.ventsellorganiser.authenticate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsell.ventsellorganiser.R;

public class AuthenticateWelcomeFragment extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
                // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        AppCompatButton mWelcomeCreateButton = (AppCompatButton) view.findViewById(R.id.welcome_create_button);
        mWelcomeCreateButton.setOnClickListener(this);
        AppCompatButton mWelcomeSignInButton = (AppCompatButton) view.findViewById(R.id.welcome_sign_in_button);
        mWelcomeSignInButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.authenticate_container, new AuthenticateSignInFragment(), "AuthenticateSignInFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
