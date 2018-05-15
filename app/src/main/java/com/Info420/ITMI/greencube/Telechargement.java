package com.Info420.ITMI.greencube;
//TODO - changer le nom du package

//TODO - commenter l'ensemble du fichier

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
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

/**
 * Created by boual on 2018-04-18.
 */

//TODO - Faire l'en-tête de fichier


//TODO - vérifier les variables non-utilisées

public class Telechargement extends AppCompatActivity
{
    //TODO - enlever une fois les logs supprimés
    private final static String TAG = "Téléchargement";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liste_telechargement);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        myToolbar.setTitle(R.string.PrefsTitle);
        myToolbar.setTitleTextColor(Color.WHITE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        File[] fichiers = getFilesDir().listFiles();

        ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

        Fichier_Adapteur adaptateur = new Fichier_Adapteur(this, liste_fichier);

        ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

        list_view.setAdapter(adaptateur);

        for(int i = 0; i < fichiers.length; i++)
        {
            if(i != 0)
            {
                String tempo = fichiers[i].getName();

                if (tempo.charAt(tempo.length() - 1) == 'u')
                {
                    tempo = tempo.substring(0, tempo.length() - 1);
                }

                Nom_Fichier fichier = new Nom_Fichier(fichiers[i].getName());
                adaptateur.add(fichier);
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private class Nom_Fichier
    {
        public String fichier;

        public int compteur;

        public Nom_Fichier(String fichier)
        {
            this.fichier = fichier;
            this.compteur = 0;
        }
    }


    private class Fichier_Adapteur extends ArrayAdapter<Nom_Fichier>
    {
        //TODO - changer le path + mettre en variable global
        static final String pathLocal = "/data/data/com.Info420.ITMI.greencube/files/";

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            final Nom_Fichier nom_fichier = getItem(position);

            if(convertView == null)
            {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row, parent, false);
            }

            final TextView txt_fichier = (TextView) convertView.findViewById(R.id.nom_telechargement);

            txt_fichier.setText(nom_fichier.fichier);

            Button bouton_email = (Button) convertView.findViewById(R.id.bt_envoyer);
            Button bouton_supprimer = (Button) convertView.findViewById(R.id.bt_delete);

            bouton_email.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final String username = prefs.getString("sourceUsername", "");
                    final String password = prefs.getString("sourcePassword", "");
                    final String usernameDest = prefs.getString("destinationUsername", "");

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
                                message.setRecipient(Message.RecipientType.TO, new InternetAddress(usernameDest));
                                message.setSubject("Transfert des données du GreenCube fichier : " + txt_fichier.getText());

                                MimeBodyPart messageBodyPart = new MimeBodyPart();
                                Multipart multipart = new MimeMultipart();

                                String file;
                                String attachement;

                                if (nom_fichier.compteur < 1)
                                {
                                    File fichier = new File(getFilesDir(), nom_fichier.fichier + 'u');
                                    File fichierNouveau = new File(getFilesDir(), nom_fichier.fichier);

                                    fichier.renameTo(fichierNouveau);
                                    file = pathLocal + txt_fichier.getText();
                                    attachement = txt_fichier.getText().toString()+".csv";
                                    nom_fichier.compteur++;
                                }
                                else
                                {
                                    file = pathLocal + txt_fichier.getText();
                                    attachement = txt_fichier.getText().toString()+".csv";
                                }


                                FileDataSource source = new FileDataSource(file);
                                messageBodyPart.setDataHandler(new DataHandler(source));
                                messageBodyPart.setFileName(attachement);
                                multipart.addBodyPart(messageBodyPart);

                                message.setContent(multipart);

                                Transport.send(message);

                                //TODO : changer l'adresse affichée dans le toast

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast mailOK = Toast.makeText(getApplicationContext(),
                                                "Courriel envoyé à GIROUX avec succès !", Toast.LENGTH_SHORT);
                                        mailOK.show();
                                    }
                                });
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
            });

            bouton_supprimer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //TODO - supprimer le log
                    Log.d(TAG, "Supprimer " + getItem(position).toString());

                    AlertDialog.Builder alert = new AlertDialog.Builder(Telechargement.this);

                    alert.setTitle("Supprimer le fichier");

                    alert.setMessage("Êtes-vous sûr de vouloir supprimer ce fichier?");

                    alert.setCancelable(false);

                    alert.setPositiveButton("Oui", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int l)
                        {
                            File fichier_supprimer = new File(getFilesDir(), txt_fichier.getText().toString());
                            fichier_supprimer.delete();

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Fichier supprimé.", Toast.LENGTH_SHORT);
                            toast.show();


                            File[] fichiers = getFilesDir().listFiles();

                            ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

                            Fichier_Adapteur adaptateur = new Fichier_Adapteur(getApplicationContext(), liste_fichier);

                            ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

                            list_view.setAdapter(adaptateur);

                            for(int i = 0; i < fichiers.length; i++)
                            {
                                if(i != 0)
                                {
                                    Nom_Fichier fichier = new Nom_Fichier(fichiers[i].getName());
                                    adaptateur.add(fichier);
                                }
                            }
                        }
                    });

                    alert.setNegativeButton("Non", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.cancel();
                        }
                    });

                    alert.create();
                    alert.show();
                }
            });
            return convertView;
        }

        public Fichier_Adapteur(Context context, ArrayList<Nom_Fichier> nom_fichier)
        {
            super(context, 0, nom_fichier);
        }
    }
}

