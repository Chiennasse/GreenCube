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

    Description :               Fichier contenant les fonctions permettant de s'authentifier
                                au serveur FTP, de récupérer le fichier de données, le copier sur la
                                mémoire interne de l'appareil mobile et de fermer la connexion.

    Liens GitHub :              //TODO - push le code sur le nouveau git et mettre l'url

    Note * :                    Notez que le code sur GitHub est affiché de façon publique, donc tout le monde
                                peut avoir accès au code. Cela est dû au compte gratuit de Git. Il faut débourser un montant
                                par mois afin de rendre le projet "privé".

*/

package com.Info420.ITMI.greencube;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class Telechargement extends AppCompatActivity
{
    ArrayList<String> nom_partager = new ArrayList<String>();

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liste_telechargement);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        myToolbar.setTitle(R.string.titleTelechargement);
        myToolbar.setTitleTextColor(Color.WHITE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        File[] fichiers = getFilesDir().listFiles();

        ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

        Fichier_Adapteur adaptateur = new Fichier_Adapteur(this, liste_fichier);

        ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

        list_view.setAdapter(adaptateur);

        nom_partager.clear();

        for(int i = 0; i < fichiers.length; i++)
        {
            if(i != 0)
            {
                String tempo = fichiers[i].getName();
                nom_partager.add(tempo);

                tempo = tempo.substring(0, tempo.length() - 1);

                Nom_Fichier fichier = new Nom_Fichier(tempo);
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
            final TextView txt_envoie = (TextView) convertView.findViewById(R.id.envoieView);

            txt_fichier.setText(nom_fichier.fichier);

            if(nom_partager.get(position).charAt(nom_partager.get(position).length() - 1) == 'e')
            {
                txt_envoie.setText(getApplicationContext().getString(R.string.messageEnvoye));
            }
            else if(nom_partager.get(position).charAt(nom_partager.get(position).length() - 1) == 'u')
            {
                txt_envoie.setText(getApplicationContext().getString(R.string.messageNonEnvoye));
            }

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
                                message.setSubject(getApplicationContext().getString(R.string.ObjetMessage) + "[" + txt_fichier.getText() + "]");

                                MimeBodyPart messageBodyPart = new MimeBodyPart();
                                Multipart multipart = new MimeMultipart();

                                String file;
                                String attachement;

                                if (nom_fichier.compteur < 1)
                                {
                                    file = pathLocal + txt_fichier.getText() + 'e';
                                    attachement = txt_fichier.getText() +".csv";
                                    nom_fichier.compteur++;
                                }
                                else
                                {
                                    file = pathLocal + txt_fichier.getText() + "e";
                                    attachement = txt_fichier.getText() +".csv";

                                }


                                FileDataSource source = new FileDataSource(file);
                                messageBodyPart.setDataHandler(new DataHandler(source));
                                messageBodyPart.setFileName(attachement);
                                multipart.addBodyPart(messageBodyPart);

                                message.setContent(multipart);

                                Transport.send(message);

                                if (nom_fichier.compteur == 1)
                                {
                                    File fichier = new File(getFilesDir(), nom_fichier.fichier + 'u');
                                    File fichierNouveau = new File(getFilesDir(), nom_fichier.fichier + 'e');

                                    fichier.renameTo(fichierNouveau);
                                }

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast mailOK = Toast.makeText(getApplicationContext(),
                                                (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) + " [" + usernameDest + "] "
                                                 + (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2))), Toast.LENGTH_SHORT);
                                        mailOK.show();

                                        txt_envoie.setText(getApplicationContext().getString(R.string.messageEnvoye));
                                    }
                                });
                            }

                            catch(MessagingException e)
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast mailFail = Toast.makeText(getApplicationContext(),
                                        getApplicationContext().getString(R.string.MessageErreur), Toast.LENGTH_SHORT);
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(Telechargement.this);

                    alert.setTitle(getApplicationContext().getString(R.string.PopUpTitre));

                    alert.setMessage(getApplicationContext().getString(R.string.PopUpConfirm));

                    alert.setCancelable(false);

                    alert.setPositiveButton(getApplicationContext().getString(R.string.PopUpTrue), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int l)
                        {
                            File fichier_supprimer = new File(getFilesDir(), nom_partager.get(position));
                            fichier_supprimer.delete();

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getString(R.string.PopUpValide), Toast.LENGTH_SHORT);
                            toast.show();

                            File[] fichiers = getFilesDir().listFiles();

                            ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

                            Fichier_Adapteur adaptateur = new Fichier_Adapteur(getApplicationContext(), liste_fichier);

                            ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

                            list_view.setAdapter(adaptateur);

                            nom_partager.clear();

                            for(int i = 0; i < fichiers.length; i++)
                            {
                                if(i != 0)
                                {
                                    String tempo = fichiers[i].getName();
                                    nom_partager.add(tempo);

                                    tempo = tempo.substring(0, tempo.length() - 1);

                                    Nom_Fichier fichier = new Nom_Fichier(tempo);
                                    adaptateur.add(fichier);
                                }
                            }
                        }
                    });

                    alert.setNegativeButton(getApplicationContext().getString(R.string.PopUpFalse), new DialogInterface.OnClickListener()
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

