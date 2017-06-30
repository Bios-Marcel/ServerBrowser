
-libraryjars <java.home>/lib/rt.jar
#-injars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/ext/jfxrt.jar
-libraryjars <java.home>/lib/jce.jar

-dontskipnonpubliclibraryclassmembers
-target 1.8
-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively
-printmapping proguard.map
-overloadaggressively
-useuniqueclassmembernames
-flattenpackagehierarchy ''
-repackageclasses ''
-keepattributes SourceFile,LineNumberTable,*Annotation*
-renamesourcefileattribute SourceFile
-adaptresourcefilenames **.properties,**fxml,**.css
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF,META-INF/services/**,**fxml,**.css

#
# Native libraries need to be loaded during runtime
-keeppackagenames org.sqlite.native 

# Preserve the special static methods that are required in all enumeration
# classes.
-keepclassmembers,allowoptimization enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep - Applications. Keep all application classes, along with their 'main'
# methods.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Also keep - Serialization code. Keep all fields and methods that are used for
# serialization.
-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Also keep - Database drivers. Keep all implementations of java.sql.Driver.
-keep class * extends java.sql.Driver

# Also keep - RMI interfaces. Keep all interfaces that extend the
# java.rmi.Remote interface, and their methods.
-keep interface  * extends java.rmi.Remote {
    <methods>;
}

# Also keep - RMI implementations. Keep all implementations of java.rmi.Remote,
# including any explicit or implicit implementations of Activatable, with their
# two-argument constructors.
-keep class * extends java.rmi.Remote {
    <init>(java.rmi.activation.ActivationID,java.rmi.MarshalledObject);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,includedescriptorclasses,allowshrinking class *,* {
    native <methods>;
}

