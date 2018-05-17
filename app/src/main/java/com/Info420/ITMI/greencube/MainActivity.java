/*
    Auteurs / Concepteurs :     BOUDREAULT, Alex
                                CHIASSON, Maxyme    -   www.linkedin.com/in/maxyme-chiasson/
                                LEBLANC, William    -   www.linkedin.com/in/william-leblanc/

    Dernière modification :     16 mai 2018

    Dans le cadre du cours :    420-W63-SI & 420-G64-SI
    Professeurs :               Guy Toutant - Yves Arsenault

    Projet remis à :            Institut Technologique de Maintenance Industrielle (ITMI)

    Copyright :                 Tous droits réservés. Le produit final est la propriété du Cégep de Sept-Îles,
                                de l'Institut Technologique de Maintenance Industrielle (ITMI) ainsi que les concepteurs
                                mentionnés ci-haut. Ceux-ci ont le droit d'alterér, distribuer, reproduire le produit final,
                                dans un éducatif et de recherche. Pour plus d'informations, veuillez contacter les concepteurs.

    Description :               Fichier noyau de l'application, dans lequel se retrouve toutes les fonctions de l'appli.

                                Lorsque l'usager lance l'application, l'écran principale s'affiche, contenant des informations
                                sur l'état de l'application et du GreenCube (s'il l'appareil est connecté au réseau sans-fil
                                du GreenCube) et un bouton permettant de télécharger les données à partir de l'ordinateur emmbarqué
                                du GreenCube.

                                Pour comprendre le fonctionnement et avoir une meilleure compréhension de l'appareil GreenCube,
                                pensez à consulter le guide d'utilisateur officiel sur Git.

    Liens GitHub :              //TODO - push le code sur le nouveau git et mettre l'url

    Note * :                    Notez que le code sur GitHub est affiché de façon publique, donc tout le monde
                                peut avoir accès au code. Cela est dû au compte gratuit de Git. Il faut débourser un montant
                                par mois afin de rendre le projet "privé".

*/




package com.Info420.ITMI.greencube;


//TODO - Commenter l'ensemble du fichier

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    Button buttonDownload;

    SharedPreferences prefs;

    private boolean modeAdmin = false;

    protected boolean envoieAuto = false;

    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(WifiStateChangedReceiver, intentFilter);
    }

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
                            wifiState.setText(getApplicationContext().getString(R.string.wifiState));
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
                            }
                        });

                        if (wifiName.equals('"' + prefs.getString("SSID", "") + '"'))
                        {
                            //Active le bouton
                            buttonDownload.setEnabled(true);
                        }

                        else
                        {
                            //Désactive le bouton
                            buttonDownload.setEnabled(false);
                            File[] fichiers = getFilesDir().listFiles();

                            String choixPrefs = prefs.getString("delay", "c");

                            if (choixPrefs.equals("a") || choixPrefs.equals("b"))
                            {
                                boolean confirmEnvoie = false;

                                for(int i = 0; i < fichiers.length; i++)
                                {
                                    if(i != 0 && fichiers[i].getName().charAt(fichiers[i].getName().length() -1) == 'u')
                                    {
                                        //Appel de la fonction d'envoie automatique
                                        envoieAutomatique(fichiers[i]);
                                        confirmEnvoie = true;
                                    }
                                }

                                if (confirmEnvoie)
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Toast message = Toast.makeText(getApplicationContext(), (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) +
                                                    " [" + prefs.getString("destinationUsername", "")) + "] " +
                                                    (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2)), Toast.LENGTH_SHORT );
                                        }
                                    });
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
                                //Affiche le SSID du réseau comme étant invalide, en jaune
                                TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                                nomssid.setText(getApplicationContext().getString(R.string.wifiInvalide));
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
                            //Désactive le bouton
                            buttonDownload.setEnabled(false);

                            //Affiche l'état de connexion à l'écran en jaune
                            TextView wifiState = (TextView)findViewById(R.id.wifiState);
                            wifiState.setText("LTE / 4G");
                            wifiState.setTextColor(Color.YELLOW);

                            //Efface le contenu des textview pour ne pas afficher de l'information erroné
                            TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                            nomssid.setText("");
                            TextView downState = (TextView)findViewById(R.id.downState);
                            downState.setText("");
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("");
                        }
                    });

                    File[] fichiers = getFilesDir().listFiles();
                    String choixPrefs = prefs.getString("delay", "c");

                    //Valide si l'admin à autorisé le transfert automatique par LTE
                    if (choixPrefs.equals("a"))
                    {
                        boolean confirmEnvoie = false;

                        for(int i = 0; i < fichiers.length; i++)
                        {
                            if(i != 0 && fichiers[i].getName().charAt(fichiers[i].getName().length() -1) == 'u')
                            {
                                //Appel de la fonction d'envoie automatique
                                envoieAutomatique(fichiers[i]);
                                confirmEnvoie = true;
                            }
                        }

                        if (confirmEnvoie)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Toast message = Toast.makeText(getApplicationContext(), (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) +
                                            " [" + prefs.getString("destinationUsername", "")) + "] " +
                                            (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2)), Toast.LENGTH_SHORT );
                                    message.show();
                                }
                            });
                        }
                    }
                }
            }

            //Aucune connexion détecté, autant par Wi-Fi que par données mobiles.
            else
            {
                //Ouvre un thread
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

                        //Efface le contenu des textview pour ne pas afficher de l'information erroné
                        TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                        nomssid.setText("");
                        TextView downState = (TextView)findViewById(R.id.downState);
                        downState.setText("");
                        TextView conxState = (TextView)findViewById(R.id.serState);
                        conxState.setText("");

                        //Affiche un message à l'usager indiquant qu'aucune connexion n'a été détectée
                        Toast toast = Toast.makeText(getApplicationContext(),
                                (getApplicationContext().getString(R.string.NoConnection)), Toast.LENGTH_SHORT);
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
        DateFormat date = new SimpleDateFormat("yyyy.MM.dd " + "HH:mm:ss"); //TODO - changer le format ?

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
                final String adresse = prefs.getString("adresse", "192.168.1.2");
                final String ssid = prefs.getString("SSID", "Green Cube 2.4GHz");
                final String username = prefs.getString("username", "administrateur");
                final String password = prefs.getString("password", "Ubuntu2018");
                final String filename = prefs.getString("filename", "test.csv");
                final String Path = prefs.getString("filepath", "/administrateur/home/");

                BufferedInputStream buffIn;

                FTPClient ftp = new FTPClient();

                try
                {
                    //Connexion au server ftp avec le port 21
                    ftp.connect(adresse, 21);

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

                    OutputStream os = new FileOutputStream(new File(getFilesDir(), filename));

                    boolean resultat = ftp.retrieveFile(filename, os);

                    if(resultat == true)
                    {
                        File original = new File(getFilesDir(), prefs.getString("filename", ""));
                        //On rajoute la lettre "u" à la fin du fichier pour "unsend", le fichier n'étant pas envoyé.
                        final File renommer = new File(getFilesDir(), getDate(timestamp) + "u");

                        original.renameTo(renommer);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                TextView downState = (TextView)findViewById(R.id.downState);
                                downState.setText("OK !");
                                downState.setTextColor(Color.GREEN);

                                //Affiche à l'usager un message indiquant que le fichier est bel et bien télécharger, en lui indiquant le nom du fichier en question
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        (getApplicationContext().getString(R.string.MessageTelecharger1)) +
                                                " ['" + renommer.getName().substring(0, renommer.getName().length() - 1) + "'] " +
                                                (getApplicationContext().getString(R.string.MessageTelecharger2)), Toast.LENGTH_SHORT);
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
                                        getApplicationContext().getString(R.string.ErrorDownload), Toast.LENGTH_SHORT);
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

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this).setMessage(getApplicationContext().getString(R.string.MessageMDP));
                    alertBuilder.setView(view);
                    final EditText userInput = (EditText) view.findViewById(R.id.userinput);
                    userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    alertBuilder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {

                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final String mdp =  prefs.getString("adminPassword", "");
                            final String saisie = (userInput.getText().toString());

                            if (saisie.equals(mdp))
                            {
                                modeAdmin = true;

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
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
                                        //TODO - TOAST MODÈLE
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                getApplicationContext().getString(R.string.MauvaisMDP), Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    });

                    Dialog dialog = alertBuilder.create();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
        final String adresse = prefs.getString("adresse", "");
        final String username = prefs.getString("sourceUsername", "");
        final String password = prefs.getString("sourcePassword", "");
        final String filename = prefs.getString("filename", "");
        final String Path = prefs.getString("filepath", "");
        final String usernameDest = prefs.getString("destinationUsername", "");
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
                        return new PasswordAuthentication(username, password);
                    }
                });

                try
                {
                    Message message = new MimeMessage(session);
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress("testitmi2@gmail.com"));
                    message.setSubject((getApplicationContext().getString(R.string.ObjetMessage)) + " [" + file.substring(0, file.length() - 1) + "]");

                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    Multipart multipart = new MimeMultipart();

                    File fichier = new File(getFilesDir(), file);


                    String file = "/data/data/com.Info420.ITMI.greencube/files/" + fichier.getName();
                    String attachement = fichier.getName().substring(0, fichier.getName().length() - 1) +".csv";

                    FileDataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachement);
                    multipart.addBodyPart(messageBodyPart);

                    message.setContent(multipart);

                    Transport.send(message);

                    envoieAuto = true;
                    //TODO - GROSSE MARDE

                    File fichierNouveau = new File(getFilesDir(), fichier.getName().substring(0, fichier.getName().length() - 1) + "e");

                    fichier.renameTo(fichierNouveau);
                }

                catch(MessagingException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast mailFail = Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getString(R.string.mailFail), Toast.LENGTH_SHORT);
                            mailFail.show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void confirmEnvoieAuto()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast mailOK = Toast.makeText(getApplicationContext(),
                        (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) + " [" + prefs.getString("destinationUsername","") + "] "
                                + (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2))), Toast.LENGTH_SHORT);
                mailOK.show();
            }
        });
    }
}
