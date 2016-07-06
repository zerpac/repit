/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annotation accessible à l'execution
@Retention(RetentionPolicy.RUNTIME)

// Annotation associé à un type (Classe, interface)
@Target(ElementType.TYPE)

public @interface IncludeInReports {}
