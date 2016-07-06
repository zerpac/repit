/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.CompositePage;
import ch.repit.rwt.client.ui.HelpPage;
import ch.repit.rwt.client.ui.Page;

/**
 *
 * @author tc149752
 */
public class BookingsPage extends Page {

    public BookingsPage() {
        super();

        setTitle("Inscriptions");
        setShowPath(false);

        Authorizer auth = SecurityManager.get().getAuthorizer();

        super.addTab("Calendrier", new CalendarPage(this));

        super.addTab("Inscriptions futures", new BookingListPage(this, BentoStatus.ACTIVE)); //,BentoStatus.ARCHIVE));
       
        super.addTab("Evénements futurs", new CalendarEventListPage(this, BentoStatus.ACTIVE)); //,BentoStatus.ARCHIVE));

        // composite trashcan
        if (auth.isAllowed(Action.VIEW_TRASH, CalendarEventDef.TYPE)
             || auth.isAllowed(Action.VIEW_TRASH, BookingDef.TYPE)){

            super.addTab("Corbeille", new CompositePage(this, "Corbeille",
                    new BookingListPage(this, BentoStatus.TRASH),
                    auth.isAllowed(Action.VIEW_TRASH, CalendarEventDef.TYPE)?
                        new CalendarEventListPage(this, BentoStatus.TRASH):null
                    ));

        }

        super.addTab("Statistiques", new StatsPage(this));

        addTab("Aide", new HelpPage(this, "<h1>Règlement</h1>" +
                "<p>Les règles suivantes ont été décidées en assemblée (cf ...)" +
                "<ul><li>(règle à ajouter...)" +
                "" +
                "</ul>" +
                "<h1>Fonctionnement</h1>" +
                "<ul><li><b>Comment créer une inscription? </b> : 2 possibilités: " +
                "<ul><li>Depuis l'onglet <u>Calendrier</u>: Cliquer dans la case de la date de début de séjour (sans " +
                "cliquer sur une inscription ou sur un événement), la case est alors colorée. Cliquer ensuite sur la " +
                "date de fin de séjour. Pour anuler la selection de la date de début, cliquer soit une 2eme fois " +
                "sur la date de début, soit sur le bouton <u>Annuler séléection ...</u> affiché en haut de la page" +
                "<li>Depuis la liste des inscriptions. Cette méthode est moins pratique car elle force à trouver 2 fois " +
                "la date dans le petit calendrier.</ul>" +
                "" +
                "</ul>"));
    }

} 
