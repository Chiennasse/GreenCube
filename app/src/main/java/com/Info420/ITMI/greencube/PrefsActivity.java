package com.Info420.ITMI.greencube;

//TODO - changer le nom du package

//TODO - commenter l'ensemble du fichier

//TODO - À l'entrer du mot de passe des préférences, faire apparaître le clavier automatiquement + envoyer le mot de passe lorsque l'usager appuie sur OK de son clavier.

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PrefsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_ui);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        myToolbar.setTitle(R.string.PrefsTitle);
        myToolbar.setTitleTextColor(Color.WHITE);

        getFragmentManager().beginTransaction().add(R.id.fragmentContainer, new MyPreferenceFragment()).commit();
    }
    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_items);
        }
    }
}


