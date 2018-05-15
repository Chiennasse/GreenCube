package com.Info420.ITMI.greencube;
// TODO : changer la package de l'appli

//TODO - Faire l'en-tête de fichier

//TODO - Commenter l'ensemble du fichier

//TODO - changer icones fichier

//TODO - Faire l'envoie automatique

//TODO - Changer la détection de wi-fi dans le OnCreate, ne s'actualise pas pendant le changement lorsque qu'on est dans l'application

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Object;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


//TODO : Supprimer les imports non-utilisés

/*
    Détecté les changement de Network de l'appareil
        Source :        https://www.panda-os.com/blog/2014/12/android-device-internet-connection-status/
        Consulté le :   9 mai 2018

    Update les textview durant le run
        Source : https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
        Consulté le : 7 mai 2018

    Activé/Désactivé un bouton
        Source : https://stackoverflow.com/questions/4384890/how-to-disable-an-android-button
        Consulté le : 9 mai 2018
 */

//TODO : Linker le plus de sources possibles

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    Button buttonDownload;

    //TODO : supprimer la ligne ci-dessous une fois les LOG enlevés
    private static final String TAG = "MonActivité";

    //TODO : Mettre les valeurs en paramètres (préférences de l'appli)
    private static final String Path = "/home/administrateur";

    SharedPreferences prefs;

    private boolean modeAdmin = false;
    private static final String passwordPrefs = "adminPrefs";

    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(WifiStateChangedReceiver, intentFilter);
    }

    //TODO - Comprendre
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(WifiStateChangedReceiver);
    }

    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            final String action = intent.getAction();

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null)
            {
                //Wi-Fi activé
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Affiche l'état de connexion à l'écran en vert
                            TextView wifiState = (TextView)findViewById(R.id.wifiState);
                            wifiState.setText("actif");
                            wifiState.setTextColor(Color.GREEN);
                        }
                    });

                    //Détecte le nom du réseau
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    final String wifiName = wifiManager.getConnectionInfo().getSSID();
                    if (wifiName != null && !wifiName.contains("unknown ssid"))
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Affiche le SSID du réseau à l'écran en blanc
                                TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                                nomssid.setText(wifiName);
                                nomssid.setTextColor(Color.WHITE);
                                //TODO - enlever l'affichage du SSID lors d'une déconnexion
                            }
                        });

                        //TODO - mettre le nom du réseau en préférence
                        if (wifiName.equals('"' + "Green Cube 2.4GHz" + '"'))
                        {
                            //Active le bouton
                            buttonDownload.setEnabled(true);
                        }

                        else
                        {
                            //Désactive le bouton
                            buttonDownload.setEnabled(false);

                            //TODO - faire l'envoie automatique (si réglages WI-FI seulement) ET régler déjà envoyer afin de ne pas envoyer plusieurs fois le fichier
                            File[] fichiers = getFilesDir().listFiles();

                            for(int i = 0; i < fichiers.length; i++)
                            {
                                if(i != 0)
                                {
                                    envoieAutomatique(fichiers[i]);
                                }
                            }
                        }
                    }
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Affiche le SSID du réseau comme étant inconnu, en jaune
                                TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                                nomssid.setText("Invalide");
                                nomssid.setTextColor(Color.YELLOW);
                            }
                        });
                    }
                }
                //Wi-Fi désactivé, mais connecté par LTE / 4G (données mobiles)
                             /*    Des frais peuvent s'appliquer      */
                else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //TODO - à supprimer
                            Log.d(TAG, "LTE");

                            //TODO - envoie automatique (si réglages LTE) ET régler déjà envoyer afin de ne pas envoyer plusieurs fois le fichier

                            //TODO - traduire les toast

                            //Désactive le bouton
                            buttonDownload.setEnabled(false);

                            //Affiche l'état de connexion à l'écran en jaune
                            TextView wifiState = (TextView)findViewById(R.id.wifiState);
                            wifiState.setText("N/A");
                            wifiState.setTextColor(Color.YELLOW);

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Connecté par LTE / 4G", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                    File[] fichiers = getFilesDir().listFiles();

                    for(int i = 0; i < fichiers.length; i++)
                    {
                        if(i != 0)
                        {
                            envoieAutomatique(fichiers[i]);
                        }
                    }
                }
            }

            //Aucune connexion détecté, autant par Wi-Fi que par données mobiles.
            else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Désactive le bouton
                        buttonDownload.setEnabled(false);

                        //Afiche l'état de connexion à l'écran en rouge
                        TextView wifiState = (TextView)findViewById(R.id.wifiState);
                        wifiState.setText("N/A");
                        wifiState.setTextColor(Color.RED);

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Aucune connexion détecté.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.registerReceiver(this.WifiStateChangedReceiver,
                new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        buttonDownload = (Button)findViewById(R.id.button);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar myToolbar=(Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");

        setSupportActionBar(myToolbar);

        myToolbar.setTitle(R.string.app_name);
        myToolbar.setTitleTextColor(Color.WHITE);

        buttonDownload.setOnClickListener(this);
    }

    public String getDate(String timestamp)
    {
        DateFormat date = new SimpleDateFormat("yyyy.MM.dd " + "HH:mm:ss");

        Date chaine_date = (new Date(Long.parseLong(timestamp)));

        return date.format(chaine_date);
    }

    @Override
    public void onClick (View view)
    {
        final Long time = System.currentTimeMillis();
        final String timestamp = time.toString();


        new Thread()
        {
            public void run()
            {

                final String adresse = prefs.getString("adresse", "");
                final String username = prefs.getString("username", "");
                final String password = prefs.getString("password", "");
                final String filename = prefs.getString("filename", "");
                final String Path = prefs.getString("filepath", "");

                BufferedInputStream buffIn;

                //TODO - à supprimer
                Log.d(TAG, "onClick()");

                FTPClient ftp = new FTPClient();

                try
                {

                    //Connexion au server ftp avec le port 21
                    ftp.connect(adresse, 21);

                    //TODO - à supprimer
                    Log.d(TAG, "Connected to " + adresse + ".");

                    //Login au serveur avec le mot de passe et le nom d'utilisateur
                    ftp.login(username, password);

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("OK !");
                            conxState.setTextColor(Color.GREEN);
                        }
                    });

                    //On affecte le chemin au répertoire contenant le fichier
                    ftp.changeWorkingDirectory(Path);

                    //On affecte le type de fichier
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    //On fait passer le serveur FTP en mode passif
                    ftp.enterLocalPassiveMode();

                    //TODO - à supprimer
                    Log.d(TAG, "Status : " + filename);

                    OutputStream os = new FileOutputStream(new File(getFilesDir(), filename));

                    boolean resultat = ftp.retrieveFile(filename, os);

                    //TODO - à supprimer
                    Log.d(TAG, "Status : " + resultat);
                    Log.d(TAG, "reply : " + ftp.getReplyString());

                    if(resultat == true)
                    {
                        File from = new File(getFilesDir(), "test.csv");
                        //On rajoute la lettre "u" à la fin du fichier pour "unsend", le fichier n'étant pas envoyé.
                        final File to = new File(getFilesDir(), getDate(timestamp) + "u");

                        from.renameTo(to);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                TextView downState = (TextView)findViewById(R.id.downState);
                                downState.setText("OK !");
                                downState.setTextColor(Color.GREEN);

                                //TODO - enlever le u à l'affichage
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Fichier '" + to.getName() + "' téléchargé avec succès !", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }

                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                TextView downState = (TextView)findViewById(R.id.downState);
                                downState.setText("fail !");
                                downState.setTextColor(Color.RED);

                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Erreur lors du téléchargement du fichier.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                    //Logout du ftp
                    ftp.logout();
                    //On se déconnecte du serveur
                    ftp.disconnect();
                }

                catch (SocketException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("fail !");
                            conxState.setTextColor(Color.RED);
                        }
                    });
                    e.printStackTrace();
                }

                catch (IOException e)
                {
                    //TODO - gèrer l'erreur de téléchargement de fichier + supprimer LOG
                    Log.d(TAG,"IOException");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final Intent intentPrefsActivity = new Intent(this, PrefsActivity.class);
        Intent intentTelechargementActivity = new Intent(this, Telechargement.class);

        switch (item.getItemId())
        {
            case R.id.itemPreference:
                if (modeAdmin == false)
                {
                    View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_input, null);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this).setMessage("Saisissez le mot de passe:");
                    alertBuilder.setView(view);
                    final EditText userInput = (EditText) view.findViewById(R.id.userinput);
                    userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    // TODO : https://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused

                    alertBuilder.setCancelable(true)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String saisie = (userInput.getText().toString());
                                    Log.d(TAG, saisie);
                                    Log.d(TAG, passwordPrefs);
                                    if (saisie.equals(passwordPrefs))
                                    {
                                        modeAdmin = true;

                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO - changer l'ensemble des toast pour celui-ci
                                                // TODO - Source : https://stackoverflow.com/questions/7331793/android-java-using-a-string-resource-in-a-toast
                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                        getApplicationContext().getString(R.string.BonMDP), Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        });

                                        startActivity(intentPrefsActivity);
                                    }
                                    else
                                    {
                                        runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO - changer l'ensemble des toast pour celui-ci
                                                // TODO - Source : https://stackoverflow.com/questions/7331793/android-java-using-a-string-resource-in-a-toast
                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                        getApplicationContext().getString(R.string.MauvaisMDP), Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        });
                                    }
                                }
                            });

                    Dialog dialog = alertBuilder.create();
                    dialog.show();
                }

                if (modeAdmin == true)
                {
                    startActivity(intentPrefsActivity);
                }

                return true;


            case R.id.itemTelechargement:
                startActivity(intentTelechargementActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void envoieAutomatique(File tempo)
    {
        //TODO - mettre les infos en préférences
        final String adresse = prefs.getString("adresse", "");
        final String username = prefs.getString("username", "");
        final String password = prefs.getString("password", "");
        final String filename = prefs.getString("filename", "");
        final String Path = prefs.getString("filepath", "");
        final String file = tempo.getName();

        new Thread()
        {
            public void run()
            {
                Properties props = new Properties();

                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication("boudreault758@gmail.com", "12maze12");

                        //return new PasswordAuthentication(username, password);
                    }
                });

                try
                {
                    Message message = new MimeMessage(session);
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress("testitmi2@gmail.com")); //TODO - mettre l'adresse en préférence
                    message.setSubject("Transfert des données du GreenCube fichier : " + filename);

                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    Multipart multipart = new MimeMultipart();

                    File fichier = new File(getFilesDir(), file);
                    File fichierNouveau = new File(getFilesDir(), file.substring(0, file.length() - 1));

                    fichier.renameTo(fichierNouveau);

                    // TODO - problème avec le path des préférences (default value)
                    //String file = getFilesDir() + "/" + fileName;
                    String file = "/data/data/com.Info420.ITMI.greencube/files/" + fichierNouveau.getName();
                    String attachement = fichierNouveau.getName() +".csv";

                    FileDataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachement);
                    multipart.addBodyPart(messageBodyPart);

                    message.setContent(multipart);

                    Transport.send(message);

                    //TODO : changer l'adresse affichée dans le toast

//                    Toast mailOK = Toast.makeText(getApplicationContext(),
//                            "Courriel envoyé à GIROUX avec succès !", Toast.LENGTH_SHORT);
//                    mailOK.show();
                }

                catch(MessagingException e)
                {
                    //TODO - supprimer le log
                    Log.d(TAG,"Sa marche pas tbnk");

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast mailFail = Toast.makeText(getApplicationContext(),
                                    "Échec de l'envoi.", Toast.LENGTH_SHORT);
                            mailFail.show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
