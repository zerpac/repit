/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.pdf;

import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.Dialog;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.CheckBoxField;
import ch.repit.rwt.client.ui.form.Field.FieldChangeHandler;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.form.FieldValidator;
import ch.repit.rwt.client.ui.form.IntegerField;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.TextField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tc149752
 */
public class StickersPrintingPage extends FormPage {

    private ObjectRef restrict2bento = null;

    public StickersPrintingPage(Page topPage)
    {
        super(StickerConfigDef.TYPE, null, topPage);
        setTitle("Etiquettes");
        setShowPath(false);
    }


    public StickersPrintingPage(Page topPage, ObjectRef restrict2bento)
    {
        super(StickerConfigDef.TYPE, null, topPage);
        setTitle("Etiquettes");
        setShowPath(false);
        this.restrict2bento = restrict2bento;
    }




    @Override
    protected void init() {

        this.addSectionHead("Type d'étiquettes");

        final IntegerField cols = new IntegerField(StickerConfigDef.ATTR_CUSTOMCOLS, "nombre de colonnes");
        final IntegerField rows = new IntegerField(StickerConfigDef.ATTR_CUSTOMROWS, "nombre de lignes");
        final TextField height = new TextField(StickerConfigDef.ATTR_CUSTOMHEIGHT, "hauteur [mm]");
        final TextField width = new TextField(StickerConfigDef.ATTR_CUSTOMWIDTH, "largeur [mm]");
        height.setColumns(6);
        width.setColumns(6);
        height.addValidator(dimensionValidator);
        width.addValidator(dimensionValidator);

        // now only one select field...
        final SelectField stickerType = new SelectField(StickerConfigDef.ATTR_STICKERTYPE, false);
        TreeMap<String,String> typeMap = new TreeMap<String,String>();
        typeMap.put("-- Veuillez sélectionner --", null);
        for (StickerType t : StickerType.values())
            typeMap.put(t.getBrand() + " " + t.getModel(), t.name());
        stickerType.setValueMap(typeMap);
        stickerType.addChangeHandler(new FieldChangeHandler() {
            public void onChange() {
                if (stickerType.getValue() != null && 
                        ((String)stickerType.getValue()).trim().length() > 0 )
                {
                    LogManager.get().debug("StickersPrintingPage - onChange:" + stickerType.getValue());
                    StickerType type = StickerType.valueOf((String)stickerType.getValue());
                    if (type == StickerType.CUSTOM) {
                        cols.setReadOnly(false);
                        rows.setReadOnly(false);
                        height.setReadOnly(false);
                        width.setReadOnly(false);
                    } else if (type != null) {
                        cols.setReadOnly(true);
                        rows.setReadOnly(true);
                        height.setReadOnly(true);
                        width.setReadOnly(true);
                        cols.setDefaultValue(type.getColumns());
                        rows.setDefaultValue(type.getRows());
                        height.setDefaultValue(type.getHeight());
                        width.setDefaultValue(type.getWidth());
                    }
                }
            }
        });
        stickerType.addValidator(new FieldValidator() {
            public boolean onValidate(List<String> messages, Object value, String attributeTitle) {
                if (value == null || ((String)value).trim().length() == 0) {
                    messages.add(attributeTitle + " doit être sélectionné");
                    return false;
                }
                return true;
            }
        } );
        this.addSingleFieldRow("Marque et Modèle", "Contactez-moi si votre format habituel ne figure pas dans la liste",
                stickerType);
        this.addFieldRow(new FieldRow("Quantité d'étiquettes par page",
                "Ne sont requis que si le modèle d'étiquettes est personnalisé. Sinon est rempli automatiquement",
                cols, rows));
        this.addFieldRow(new FieldRow("Dimension d'une étiquette", "idem", height, width));


        this.addSectionHead("Paramètres fins");
        this.addSingleFieldRow("Nombre d'étiquette à laisser vide",
                "Permet d'économiser des pages d'étiquettes déjà commencées en 'sautant' les étiquettes déjà utilisées",
                new IntegerField(StickerConfigDef.ATTR_CELLS2SKIP, "0 pour commencer au début") );
        this.addSingleFieldRow("Inclure une bordure",
                "Ajoute une bordure aux étiquettes. Surtout pour les tests, ou si les étiquettes ne sont pas pré-découpées",
                new CheckBoxField(StickerConfigDef.ATTR_SHOWBORDER) );

        TreeMap<String,String> fontMap = new TreeMap();
        fontMap.put( "Grande (12p)", StickerConfigDef.BIG_FONT);
        fontMap.put( "Normale (10p)", StickerConfigDef.NORMAL_FONT);
        fontMap.put( "Petite (9p)", StickerConfigDef.SMALL_FONT);
        SelectField fontSelect = new SelectField(StickerConfigDef.ATTR_FONTSIZE, false);
        fontSelect.setValueMap(fontMap);
        this.addSingleFieldRow("Taille de la fonte",
                "Si des adresses sont longues et les étiquettes petites, il peut être utile de spécifier la petite fonte",
                 fontSelect);

        if (restrict2bento == null) {
            this.addSectionHead("Sélection des utilisateurs");
            this.addSingleFieldRow("Inclure tous les utilisateurs",
                    "Si pas coché, seuls seront inclus dans les étiquettes les membres n'ayant" +
                    " pas coché l'option 'Communications oficielles par email'",
                    new CheckBoxField(StickerConfigDef.ATTR_INCLUDEEMAILUSERS) );
        } else {
            this.addSectionHead("Filtre");
            TextField restrict = new TextField(StickerConfigDef.ATTR_RESTRICT2BENTO,null);
            restrict.setText(restrict2bento.toString());
            restrict.setReadOnly(true);
            this.addSingleFieldRow("Objet inclus",null,restrict);
        }
        
        super.init();
    }


    // override default buttons
    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets)
    {
        Button generate = new Button("Générer PDF");
        generate.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (validateForm()) {
                    // read the modifs
                    Bento readObj = readForm();
                    // validate after reading the obj
                    if (validateObject(readObj)) {
                        // clear the messages
                        LogManager.get().info("Etiquettes en préparation. " +
                                "Attention d'imprimer le document sans mettre à l'echelle");

                        // removes warning when leaving page
                        getPageNav().setPageEdited(false);

                        // generate the link (would be easier via GWT-RPC)
                        String link = "/pdf/etiquettes.pdf?";
                        link += StickerConfigDef.ATTR_STICKERTYPE + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_STICKERTYPE);
                        if (readObj.getDisplayValue(StickerConfigDef.ATTR_STICKERTYPE).equals(StickerType.CUSTOM.name())) {
                            link += "&" + StickerConfigDef.ATTR_CUSTOMCOLS + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_CUSTOMCOLS);
                            link += "&" + StickerConfigDef.ATTR_CUSTOMROWS + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_CUSTOMROWS);
                            link += "&" + StickerConfigDef.ATTR_CUSTOMWIDTH + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_CUSTOMWIDTH);
                            link += "&" + StickerConfigDef.ATTR_CUSTOMHEIGHT + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_CUSTOMHEIGHT);
                        }
                        link += "&"+StickerConfigDef.ATTR_INCLUDEEMAILUSERS + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_INCLUDEEMAILUSERS);
                        link += "&"+StickerConfigDef.ATTR_CELLS2SKIP + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_CELLS2SKIP);
                        link += "&"+StickerConfigDef.ATTR_SHOWBORDER + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_SHOWBORDER);
                        link += "&"+StickerConfigDef.ATTR_FONTSIZE + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_FONTSIZE);
                        link += "&"+StickerConfigDef.ATTR_RESTRICT2BENTO + "=" +
                                readObj.getDisplayValue(StickerConfigDef.ATTR_RESTRICT2BENTO);
                        Window.open(link, "pdfTab", null);

                        // display info to print PDF without scaling
                        Dialog.alert("Les étiquettes vont être téléchargées. Attention de les imprimer sans options de mise à l'échelle");
                    }
                }
            }
        } );
        leftWidgets.add(generate);
    }


    @Override
    protected void validateAfterRead(Bento bento, List<String> validationErrors) {
        super.validateAfterRead(bento, validationErrors);

        if (bento.get(StickerConfigDef.ATTR_STICKERTYPE).getString().equals(StickerType.CUSTOM.name())) {
            if (Float.parseFloat(bento.get(StickerConfigDef.ATTR_CUSTOMHEIGHT).getString())
                    > StickerConfigDef.A4HEIGHT_MM / bento.get(StickerConfigDef.ATTR_CUSTOMROWS).getInteger()) {
                validationErrors.add("La hauteur est trop grande au vu du nombre de lignes; le maximum serait " +
                        StickerConfigDef.A4HEIGHT_MM / bento.get(StickerConfigDef.ATTR_CUSTOMROWS).getInteger());
            }
            if (Float.parseFloat(bento.get(StickerConfigDef.ATTR_CUSTOMWIDTH).getString())
                    > StickerConfigDef.A4WIDTH_MM / bento.get(StickerConfigDef.ATTR_CUSTOMCOLS).getInteger()) {
                validationErrors.add("La largeur est trop grande au vu du nombre de colonnes; le maximum serait " +
                        StickerConfigDef.A4WIDTH_MM / bento.get(StickerConfigDef.ATTR_CUSTOMCOLS).getInteger());
            }
        }

        /*
        if (restrict2bento != null) {
            BentoAttribute restrict = new BentoAttribute(StickerConfigDef.ATTR_RESTRICT2BENTO, AttributeType.STRING);
            restrict.set(restrict2bento.toString());
            bento.put(StickerConfigDef.ATTR_RESTRICT2BENTO, restrict);
        } */
        LogManager.get().debug("validateAfterRead: bento="+bento);
    }


    private FieldValidator dimensionValidator = new FieldValidator() {
            public boolean onValidate(List<String> messages, Object value, String attributeTitle) {
                if (value == null) {
                    return false;
                }
                float val = 0f;
                try {
                    val = Float.parseFloat((String) value);
                } catch (NumberFormatException e) {
                    messages.add("La valeur du champ n'est pas un nombre");
                    return false;
                }
                if (val < 10f) {
                    messages.add("La valeur doit être au moins 10 mm (et encore)");
                    return false;
                }
                return true;
            }
        };

}
