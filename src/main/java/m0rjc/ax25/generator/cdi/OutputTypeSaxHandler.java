package m0rjc.ax25.generator.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import m0rjc.ax25.generator.xmlDefinitionReader.framework.NamedTagHandler;

/**
 * CDI qualifier for implementors of {@link NamedTagHandler} to use for output tags in the XML file.
 * Extension point of OutputListSaxHandler.
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD,PARAMETER,TYPE})
@Qualifier
@Documented
public @interface OutputTypeSaxHandler
{
}
