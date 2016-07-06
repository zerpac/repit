/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

/**
 *
 * @author tc149752
 */
public class Permission {

    private Action action;
    private String objectType;

    public Permission(String permSerialized) {
        String[] split = permSerialized.split(":");
        action = Action.valueOf(split[1]);
        objectType = split[0];
    }

    public Action getAction() {
        return action;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return getAction() + ":" + getObjectType();
    }

}
