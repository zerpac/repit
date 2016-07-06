package ch.repit.rwt.server.user;

import ch.repit.rwt.client.pdf.StickerType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.pdf.StickerConfigDef;
import ch.repit.rwt.client.util.CountryCodes;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.client.user.UserPrefDef;
import ch.repit.rwt.server.pdf.AbstractPdfReport;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.util.Logging;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class UserAddressStickersPdf extends AbstractPdfReport {

    private static Logging LOG = new Logging(UserAddressStickersPdf.class.getName());


    public static float INCH2DOT = 72f;
    public static float MM2INCH = 1f/25.4f;
    

    private StickerType stickerType;
    private boolean showBorder = false;
    private boolean forAllUsers = false;
    protected ObjectRef restrict2bento = null;

    private int skipCells = 0;

    protected Font font, fontBold;

    private int cols;
    private float height;
    float hsep;

    
    public void addContent(Document pdfDoc, Map params) throws DocumentException
    {
        String method = "addContent";
        LOG.enter(method);


        // also read who must be in the report

        // 2. define table
        PdfPTable table = new PdfPTable(cols);
        table.setHeaderRows(0);
        table.setWidthPercentage(100f);
        float cellfh = height * MM2INCH*INCH2DOT;

        // skip cells
        int cellCount = 0;
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorderWidth(0);
        emptyCell.setFixedHeight(cellfh);
        for (int i = 0; i < skipCells; i++) {
            table.addCell(emptyCell);
            cellCount++;
        }

        // 3. iterate on objects
        for (Bento bento : filterBentos())
        {
            Phrase address = formatSticker(bento);
            PdfPCell cell = new PdfPCell(address);
            cell.setFollowingIndent(20);
            cell.setExtraParagraphSpace(2f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setPadding(0);
            cell.setPaddingLeft(10);
            cell.setPaddingRight(10 + (hsep * MM2INCH*INCH2DOT));
            if (showBorder) {
                cell.setBorderColor(repitOliveDark);
                cell.setBorderWidth(1);
            } else
                cell.setBorderWidth(0);

            cell.setFixedHeight(cellfh);
            table.addCell(cell);
            cellCount++;
        }

        // "close" the last line
        if (cellCount % cols > 0)
            for (int i = 0; i < cols - (cellCount % cols); i++)
                table.addCell(emptyCell);
        table.setComplete(true);

        pdfDoc.add(table);

        LOG.leave(method);
    }


    @Override
    protected void initDocument(Document document, Map params) {

        LOG.enter("initDocument", "params="+params);

        // 1. read config
        String stickTypeStr = readParam(params, StickerConfigDef.ATTR_STICKERTYPE, null);
        stickerType = StickerType.valueOf(stickTypeStr);
        if (stickerType == null) {
            stickerType = StickerType.ZWECKFORM_3481;  // should come from config
            // TBD: throw anything ???
        }

        float width;
        int rows;
        if (stickerType == StickerType.CUSTOM) {
            cols = Integer.valueOf(readParam(params, StickerConfigDef.ATTR_CUSTOMCOLS, "1"));
            rows = Integer.valueOf(readParam(params, StickerConfigDef.ATTR_CUSTOMROWS, "1"));
            height = Float.valueOf(readParam(params, StickerConfigDef.ATTR_CUSTOMHEIGHT, "100.0"));
            width = Float.valueOf(readParam(params, StickerConfigDef.ATTR_CUSTOMWIDTH, "100.0"));
            hsep = 0f;
        } else {
            cols = stickerType.getColumns();
            rows = stickerType.getRows();
            height = stickerType.getHeight();
            width = stickerType.getWidth();
            hsep = stickerType.getHorizontalSep();
        }

        String showBorderStr = readParam(params,StickerConfigDef.ATTR_SHOWBORDER, "false");
        if (showBorderStr != null) {
            showBorder = Boolean.parseBoolean(showBorderStr);
        }

        String forAllUsersStr = readParam(params,StickerConfigDef.ATTR_INCLUDEEMAILUSERS, "false");
        if (forAllUsersStr != null) {
            forAllUsers = Boolean.parseBoolean(forAllUsersStr);
        }
        
        String restrictStr = readParam(params,StickerConfigDef.ATTR_RESTRICT2BENTO, null);
        if (restrictStr != null) {
            restrict2bento = new ObjectRef(restrictStr);
        }

        String skipCellsStr = readParam(params,StickerConfigDef.ATTR_CELLS2SKIP, null);
        if (skipCellsStr != null) {
            try {
                skipCells = Integer.parseInt(skipCellsStr);
                skipCells = skipCells % (cols * rows);
            } catch (NumberFormatException e) {
                // TBD: throw anything ???
            }
        }

        // fonts
        String fontStr = readParam(params,StickerConfigDef.ATTR_FONTSIZE, StickerConfigDef.NORMAL_FONT);
        if (fontStr.equals(StickerConfigDef.NORMAL_FONT)) {
            font = super.smallFont;
            fontBold = super.smallFontBold;
        } else if (fontStr.equals(StickerConfigDef.BIG_FONT)) {
            font = super.normalFont;
            fontBold = super.normalFontBold;
        } else { // mini
            font = super.miniFont;
            fontBold = super.miniFontBold;
        }

        // init margins
        float hmargin = ( StickerConfigDef.A4WIDTH_MM - (cols*width) - ((cols-1)*hsep) ) * MM2INCH*INCH2DOT / 2;
        float vmargin = ( StickerConfigDef.A4HEIGHT_MM - (rows*height) ) * MM2INCH*INCH2DOT / 2;
        document.setMargins(hmargin, hmargin-hsep, vmargin, vmargin);

        // doc meta data
        document.addTitle("Etiquettes imprimables");
        document.addSubject("Format "+stickerType.getBrand() + " " + stickerType.getModel());
    }


    /**
     * This method can be overriden depending on the filter we implement
     * @return
     */
    protected List<Bento> filterBentos() {
        BentoDef userDef = BentoDefFactory.get().getDef(UserDef.TYPE);
        Map<String,String> qp = new HashMap();
        qp.put("ordering", UserPrefDef.ATTR_OFFICIAL_COMM_VIA_EMAIL + " asc, lastName asc, firstName asc");
        if (!forAllUsers)
            qp.put("filter", " " + UserPrefDef.ATTR_OFFICIAL_COMM_VIA_EMAIL + " != true ");
        List<Bento> bentoList = JdoHelper.get().listActiveBentos(userDef, qp);
        return bentoList;
    }


    protected Phrase formatSticker(Bento bento) {
        Phrase address = new Phrase(bento.getDisplayValue(UserDef.ATTR_FIRSTNAME) + " ",font);
        address.add(new Phrase(bento.getDisplayValue(UserDef.ATTR_LASTNAME) + "\n", fontBold));
        if (bento.get(UserDef.ATTR_ADDRESSLINE) != null && bento.get(UserDef.ATTR_ADDRESSLINE).getString() != null )
            address.add(new Phrase(bento.get(UserDef.ATTR_ADDRESSLINE).getString() + "\n", font));
        if (bento.get(UserDef.ATTR_ZIP) != null && bento.get(UserDef.ATTR_ZIP).getString() != null )
            address.add(new Phrase(bento.get(UserDef.ATTR_ZIP).getString() + " ", font));
        if (bento.get(UserDef.ATTR_LOCALITY) != null && bento.get(UserDef.ATTR_LOCALITY).getString() != null )
            address.add(new Phrase(bento.get(UserDef.ATTR_LOCALITY).getString(), fontBold));
        if (bento.get(UserDef.ATTR_COUNTRY) != null && bento.get(UserDef.ATTR_COUNTRY).getString() != null )
            if (!CountryCodes.SUISSE.equals(bento.get(UserDef.ATTR_COUNTRY).getString()))
                address.add(new Phrase("\n" + bento.get(UserDef.ATTR_COUNTRY).getString(), font));
        return address;
    }



    private String readParam(Map params, String key, String defVal) {
        String[] vals = (String[])params.get(key);
        if (vals != null && vals.length > 0) {
            return (String)vals[0];
        }
        return defVal;
    }
}
