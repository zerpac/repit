package ch.repit.rwt.server.user;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.util.CountryCodes;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.server.pdf.AbstractPdfReport;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.util.Logging;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class UserListPdfReport extends AbstractPdfReport {

    private static Logging LOG = new Logging(UserListPdfReport.class.getName());

    private static int[] widths = new int[] {160, 160, 160, 100};
    private static int[] contactWidths = new int[] {50, 100};
    private static String[] cols = new String[] {"Nom", "Adresse", "Contacter", "Fonctions"};

    // for photo
    private static int[] widthsp = new int[] {160, 160, 160, 100, 50};
    private static String[] colsp = new String[] {"Nom", "Adresse", "Contacter", "Fonctions", ""};

    
    public void addContent(Document pdfDoc, Map params) throws DocumentException
    {
        String method = "addContent";
        LOG.enter(method);
        
        // 1. read params
        String[] columns = cols;
        int[] colWidths = widths;
        if (params.containsKey("photo")) {
            columns = colsp;
            colWidths = widthsp;
        }

        // cache roles
        BentoDef roleDef = BentoDefFactory.get().getDef(RoleDef.TYPE);
        Map<String,String> qp1 = new HashMap();
        List<Bento> roleBentoList = JdoHelper.get().listActiveBentos(roleDef, qp1);
        Map<ObjectRef,Bento> rolesMap = new HashMap();
        for (Bento role : roleBentoList)
            rolesMap.put(role.getRef(), role);
        

        // 2. define table
        PdfPTable table = new PdfPTable(columns.length);
        table.setHeaderRows(1);
        table.setWidths(colWidths);
        table.setWidthPercentage(100f);

        // 3. header row
        for (String col : columns) {
            PdfPCell header = new PdfPCell(new Phrase(col,normalFontOlive));
            if (col.length() == 0) {
                header.setBorder(0);
            } else {
                header.setBackgroundColor(repitOliveLight);
                header.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                header.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                header.setPadding(3);
                header.setBorderColor(repitOliveDark);
            }
            table.addCell(header);
        }

        // 3. iterate on objects
        BentoDef userDef = BentoDefFactory.get().getDef(UserDef.TYPE);
        Map<String,String> qp = new HashMap();
        qp.put("ordering", "lastName asc, firstName asc");
        List<Bento> bentoList = JdoHelper.get().listActiveBentos(userDef, qp);
        
        for (Bento bento : bentoList) {

            // 2. Name
            Phrase name = new Phrase(bento.getDisplayValue(UserDef.ATTR_FIRSTNAME) + " ");
            name.add(new Phrase(bento.getDisplayValue(UserDef.ATTR_LASTNAME), normalFontBold));
            PdfPCell nc = new PdfPCell(name);
            nc.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            nc.setPaddingLeft(8);
            nc.setPaddingRight(8);
            nc.setPaddingTop(8);
            nc.setPaddingBottom(8);
            nc.setBorderColor(repitOliveDark);
            table.addCell(nc);

            // 3. address
            Phrase address = new Phrase();
            if (bento.get(UserDef.ATTR_ADDRESSLINE) != null && bento.get(UserDef.ATTR_ADDRESSLINE).getString() != null ) // + lenght...
                address.add(new Phrase(bento.get(UserDef.ATTR_ADDRESSLINE).getString() + "\n", smallFont));
            if (bento.get(UserDef.ATTR_ZIP) != null && bento.get(UserDef.ATTR_ZIP).getString() != null )
                address.add(new Phrase(bento.get(UserDef.ATTR_ZIP).getString() + " ", smallFont));
            if (bento.get(UserDef.ATTR_LOCALITY) != null && bento.get(UserDef.ATTR_LOCALITY).getString() != null )
                address.add(new Phrase(bento.get(UserDef.ATTR_LOCALITY).getString(), smallFontBold));
            if (bento.get(UserDef.ATTR_COUNTRY) != null && bento.get(UserDef.ATTR_COUNTRY).getString() != null )
                if (!CountryCodes.SUISSE.equals(bento.get(UserDef.ATTR_COUNTRY).getString()))
                    address.add(new Phrase("\n" + bento.get(UserDef.ATTR_COUNTRY).getString(), smallFont));
            PdfPCell ac = new PdfPCell(address);
          //  ac.setNoWrap(true);
            ac.setFollowingIndent(20);
            ac.setExtraParagraphSpace(3f);
            ac.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            ac.setPaddingLeft(8);
            ac.setPaddingRight(8);
            ac.setPaddingTop(2);
            ac.setPaddingBottom(2);
            ac.setBorderColor(repitOliveDark);
            table.addCell(ac);

            // 4. Contacts
            PdfPTable contacts = new PdfPTable(2);
            contacts.setWidths(contactWidths);
            if (bento.get(UserDef.ATTR_EMAIL) != null && bento.get(UserDef.ATTR_EMAIL).getString() != null ) {
                PdfPCell cell = new PdfPCell(new Phrase(bento.get(UserDef.ATTR_EMAIL).getString(), smallFont));
                cell.setColspan(2);
                cell.setBorder(0);
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                contacts.addCell(cell);
            }
            for (String attr : new String[] {UserDef.ATTR_PRIVATEPHONE, UserDef.ATTR_MOBILEPHONE, UserDef.ATTR_WORKPHONE, UserDef.ATTR_FAX} ) {
                if (bento.get(attr) != null && bento.get(attr).getString() != null ) {
                    PdfPCell tit = new PdfPCell ( new Phrase(userDef.getAttributeDef(attr).getLabel(), miniFontOlive) );
                    tit.setBorder(0);
                    contacts.addCell(tit);
                    PdfPCell val  = new PdfPCell ( new Phrase(bento.get(attr).getString(), smallFont) );
                    val.setBorder(0);
                    val.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    contacts.addCell(val);
                }
            }
            PdfPCell cc = new PdfPCell(contacts);
            cc.setPadding(2);
            cc.setBorderColor(repitOliveDark);
            table.addCell(cc);

            // 5. functions
            Phrase funcs = new Phrase();
            if (bento.get(UserDef.ATTR_ROLESREF) != null && bento.get(UserDef.ATTR_ROLESREF).getStringList() != null ) {
                List<String> pRoles = new ArrayList();
                List<String> sRoles = new ArrayList();

                // fetch role names and cat
                List<String> roles = bento.get(UserDef.ATTR_ROLESREF).getStringList();
                for (String roleStr : roles) {
                    ObjectRef roleRef = new ObjectRef(roleStr);
                    Bento role = rolesMap.get(roleRef);
                    if (role != null) {
                        String roleName = role.getDisplayValue(RoleDef.ATTR_NAME);
                        RoleDef.Category cat = RoleDef.Category.valueOf(role.getDisplayValue(RoleDef.ATTR_CATEGORY));

                        if (cat == RoleDef.Category.Primary) {
                            pRoles.add(roleName);
                        } else if (cat == RoleDef.Category.Secondary) {
                            sRoles.add(roleName);
                        }
                    }
                }

                // lay them out
                if (pRoles.size() > 0) {
                    Phrase rt = new Phrase("   " + RoleDef.Category.Primary.getLabel() + "\n", miniFontOlive);
                    funcs.add(rt);
                    for (String r : pRoles) {
                        Phrase rp = new Phrase(r + "\n", smallFontBold);
                        funcs.add(rp);
                    }
                }
                if (sRoles.size() > 0) {
                    Phrase rt = new Phrase("   " + RoleDef.Category.Secondary.getLabel() + "\n", miniFontOlive);
                    funcs.add(rt);
                    for (String r : sRoles) {
                        Phrase rp = new Phrase(r + "\n", smallFont);
                        funcs.add(rp);
                    }
                }
            }
            PdfPCell fc = new PdfPCell(funcs);
            fc.setExtraParagraphSpace(3f);
            fc.setFollowingIndent(8);
            fc.setPadding(2);
            fc.setBorderColor(repitOliveDark);
            table.addCell(fc);
            
            // 1. photo 
            if (params.containsKey("photo")) {
                FileDescriptor[] af = bento.getAttachedFiles();
                PdfPCell photoCell = new PdfPCell();
                if (af != null && af.length > 0 && af[0] != null && af[0].isImage()) {
                    Image photo = null;
                    try {
                        photo = Image.getInstance(JdoHelper.get().readBlob(bento.getRef(), af[0]));
                    } catch (Exception ex) {
                        LOG.error(method, ex.getMessage(), ex);
                    }
                    if (photo != null) {
                        photoCell.addElement(photo);
                    }
                }
                photoCell.setBorder(0);
                table.addCell(photoCell);
            }
        }
        
        pdfDoc.add(table);

        LOG.leave(method);
    }
    
/*
    @Override
    protected String generateHeader(Map options,  String principalName, int pageNumber) {
        return "Page " + pageNumber;
    }
*/
    @Override
    protected String generateFooter(Map options,  String principalName, int pageNumber) {
        return "Liste g\u00E9n\u00E9r\u00E9e par "  + principalName + " le " +
                Formaters.get().formatDate(new Date(), DatePattern.DATE);
    }

}
