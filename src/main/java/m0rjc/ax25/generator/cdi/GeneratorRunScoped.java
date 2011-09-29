package m0rjc.ax25.generator.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
 
import javax.enterprise.context.NormalScope;

/**
 * CDI Bean Scope for a Generator Run
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@NormalScope
public @interface GeneratorRunScoped
{
}
