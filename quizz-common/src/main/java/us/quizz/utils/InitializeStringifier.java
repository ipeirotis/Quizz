package us.quizz.utils;

import com.googlecode.objectify.ObjectifyFactory;

import java.lang.reflect.Type;

/**
 * TODO(panos): This is a temporary hack till this class gets into Objectify 5.
 * https://code.google.com/p/objectify-appengine/source/browse/src/main/java/com/googlecode/objectify/stringifier/InitializeStringifier.java
 * 
 * 
 * <p>If a Stringifier<?> implements this interface, it will be called once just after construction
 * with the actual Type of the key to stringify.</p>
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public interface InitializeStringifier
{
        /**
         * Informs the stringifier of the actual key type.
         *
         * @param fact is just handy to have around
         * @param keyType is the declared type of the map key.
         */
        void init(ObjectifyFactory fact, Type keyType);
}