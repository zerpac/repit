/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security.ui;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.TextField;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.ui.form.MultiSelectField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class RoleDetailsPage extends FormPage {


    public RoleDetailsPage(ObjectRef objectRef)
    {
        super(RoleDef.TYPE, objectRef, null);
    }


    @Override
    protected void init() {

        if (isCreate()) {
            setTitle("Création Rôle");
        }
        else {
            setTitle(getObject().getDisplayValue(RoleDef.ATTR_NAME));
        }
        super.setShowPath(true);

        this.addSectionHead("Définition");

        // name
        TextField name = new TextField(RoleDef.ATTR_NAME);
        this.addSingleFieldRow("Nom du Rôle", null, name);

        // category
        SelectField category = new SelectField(RoleDef.ATTR_CATEGORY, false);
        category.setValueMap(RoleDef.Category.getAllValuesMap());
        this.addSingleFieldRow("Catégorie", "Catégorie de rôle", category);

        // Description
        TextField desc = new TextField(RoleDef.ATTR_DESCRIPTION, null, 3);
        this.addSingleFieldRow("Description", null, desc);

        // permissions
        this.addSectionHead("Permissions", "Une permission définit le droit d'effectuer un ACTION sur un type d'OBJET. Une ACTION inclut toutes les actions de degré inférieur sur le même objet.");

        // roles hérités
        MultiSelectField inheritedRolesField = new MultiSelectField(RoleDef.ATTR_INHERITED_ROLES, null, "disponibles", "hérités");
        Map<String,String> roleVals = new HashMap();
        for (Bento role : CacheManager.get().getCachedObjects(RoleDef.TYPE))
            roleVals.put(role.getDisplayValue(RoleDef.ATTR_NAME), role.getRef().toString());
        inheritedRolesField.setAvailableValues(roleVals);
        addSingleFieldRow("Rôles hérités",
                "Liste les rôles que ce rôle inclut implicitement (ex: Président hérite de Comité)",
                inheritedRolesField);

        // permissions
        List<String> optionValuesAll = new ArrayList<String>();
        List<String> optionValuesOwn = new ArrayList<String>();
        //for (Action a : Action.values())
        optionValuesAll.add("*:" + Action.ADMIN.name());
        for (String t :  BentoDefFactory.get().getTypes()) {
            if (BentoDefFactory.get().getDef(t).supportedActionsAll() != null)
                for (Action a : BentoDefFactory.get().getDef(t).supportedActionsAll()) {
                    if (a!=null)
                        optionValuesAll.add(t + ":" + a.name());
                }
            if (BentoDefFactory.get().getDef(t).getOwnerPolicy() != BentoDef.OwnerPolicy.NONE
                        && BentoDefFactory.get().getDef(t).supportedActionsOwn()!=null) {
                for (Action a : BentoDefFactory.get().getDef(t).supportedActionsOwn()) {
                    if (a!=null)
                        optionValuesOwn.add(t + ":" + a.name());
                }
            }
        }
     
        // SelectField permFieldAll = new SelectField(RoleDef.ATTR_PERM_ALL, "Tenir CTRL ou CMD enfoncé pour sélection multiple", true);
        MultiSelectField permFieldAll = new MultiSelectField(RoleDef.ATTR_PERM_ALL, null, "disponibles", "assignées");
        permFieldAll.setAvailableValues(optionValuesAll);
        addSingleFieldRow("Portée globale", "Permet l'action sur tous les objects de ce type, indépendamment de leur owner", permFieldAll);
       // SelectField permFieldOwn = new SelectField(RoleDef.ATTR_PERM_OWN, "Tenir CTRL ou CMD enfoncé pour sélection multiple", true);
        MultiSelectField permFieldOwn = new MultiSelectField(RoleDef.ATTR_PERM_OWN, null, "disponibles","assignées");
        permFieldOwn.setAvailableValues(optionValuesOwn);
        addSingleFieldRow("Propres objets uniquement",
                "Permet de réduire la portée aux seuls objets détenus (en général créés) par l'utilisateur, ou l'utilisateur lui-même",
                permFieldOwn);


        super.init();
    }
    

}
