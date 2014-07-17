package us.quizz.utils;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.repackaged.gentyref.GenericTypeReflector;
import com.googlecode.objectify.stringifier.Stringifier;

import java.lang.reflect.Type;

/**
 * TODO(panos): This is a temporary hack till this class gets into Objectify 5.
 * https://code.google.com/p/objectify-appengine/source/browse/src/main/java/com/googlecode/objectify/stringifier/EnumStringifier.java
 * 
 * <p>Converts Enums back and forth with their string representation
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class EnumStringifier<E extends Enum> implements Stringifier<E>, InitializeStringifier
{
        private Class<E> enumClass;

        @Override
        public void init(ObjectifyFactory fact, Type keyType) {
                enumClass = (Class<E>)GenericTypeReflector.erase(keyType);
                assert Enum.class.isAssignableFrom(enumClass);
        }

        @Override
        public String toString(E obj) {
                return obj.name();
        }

        @Override
        public E fromString(String str) {
                return (E)Enum.valueOf(enumClass, str);
        }
}
