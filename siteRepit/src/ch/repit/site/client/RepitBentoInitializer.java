/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client;

import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoInitializer;
import ch.repit.rwt.client.pdf.StickerConfigDef;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.client.user.UserPrefDef;
import ch.repit.site.client.blog.BlogOfficialDef;
import ch.repit.site.client.blog.BlogPublicDef;
import ch.repit.site.client.calendar.BookingDef;
import ch.repit.site.client.calendar.CalendarEventDef;
import ch.repit.site.client.contact.YellowPagesEntryDef;

/**
 *
 * @author tc149752
 */
public class RepitBentoInitializer implements BentoInitializer {

    public void registerCustomDefs() {
        BentoDefFactory factory = BentoDefFactory.get();
        factory.registerDefs(new UserDef(),
                             new UserPrefDef(),
                             new StickerConfigDef(),
                             new BookingDef(),
                             new CalendarEventDef(),
                             new BlogOfficialDef(),
                             new BlogPublicDef(),
                             new YellowPagesEntryDef());
    }
}
