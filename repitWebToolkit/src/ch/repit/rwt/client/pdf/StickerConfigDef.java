/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.pdf;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.security.Action;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tc149752
 */
public class StickerConfigDef extends BentoDef {


    public static float A4WIDTH_MM = 210f;
    public static float A4HEIGHT_MM = 297f;


    public static final String SMALL_FONT = "small";
    public static final String NORMAL_FONT = "normal";
    public static final String BIG_FONT = "big";


    public static final String TYPE = "StickerConfig";

    public static final String ATTR_STICKERTYPE = "stickerType";
    public static final String ATTR_CUSTOMCOLS = "customCols";
    public static final String ATTR_CUSTOMROWS = "customRows";
    public static final String ATTR_CUSTOMHEIGHT = "customHeight";
    public static final String ATTR_CUSTOMWIDTH = "customWidth";
    public static final String ATTR_CELLS2SKIP = "cells2Skip";
    public static final String ATTR_SHOWBORDER = "showBorder";
    public static final String ATTR_FONTSIZE = "fontSize";
    public static final String ATTR_INCLUDEEMAILUSERS = "notEmailUsers";

    public static final String ATTR_RESTRICT2BENTO = "restrict2bento";


    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_STICKERTYPE, AttributeType.STRING, "", Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CUSTOMCOLS, AttributeType.INTEGER, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CUSTOMROWS, AttributeType.INTEGER, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CUSTOMHEIGHT, AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CUSTOMWIDTH, AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CELLS2SKIP, AttributeType.INTEGER, 0));
        attrDefs.add(new AttributeDef(ATTR_SHOWBORDER, AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef(ATTR_FONTSIZE, AttributeType.STRING, SMALL_FONT));
        attrDefs.add(new AttributeDef(ATTR_INCLUDEEMAILUSERS, AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef(ATTR_RESTRICT2BENTO, AttributeType.STRING, Feature.READONLY));
    }

    public StickerConfigDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return "paramètres d'impresssion d'étiquettes";
    }

    @Override
    public String getDistinguishedAttribute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getJdoClassName() {
        return null;
    }

    @Override
    public List<Action> supportedActionsAll() {
        return null;
    }

    @Override
    public List<Action> supportedActionsOwn() {
        return null;
    }


}
