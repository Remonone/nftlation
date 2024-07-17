package remonone.nftilation.utils.annotations;

import remonone.nftilation.utils.HttpRequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EndPointListener {
    String path();
    HttpRequestMethod method();
}
