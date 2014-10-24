/*
 * LocalDir
 *
 * Utility class to get String and File reference to the local directory
 * that the class is executing in.
 */

package edu.jhu.icm.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;

/**
 * adapted from Mark Kozel's localDir class
 * 
 * @author cyang
 */
public class LocalDir {
    private static Logger logger = Logger.getLogger(LocalDir.class.getName());

    public static String getDir(Class me) {
        URL fullURL = getClassURL(me);
        if (fullURL.getProtocol().indexOf("jar")!=-1) {
            return getJarDir(me);
        } else {
            return getPkgDir(me);
        }
    }

    /**
     * get jar directory
     * 
     * @param me
     *            a class in the jar within a package or not
     * @return directory where jar file resides
     */
    public static String getJarDir(Class me) {
        /*
         * convert a jar url (below) to a file system directory
         * jar:file:/C:/Documents%20and%20Settings/cyang/Desktop/rsserver.jar!/edu/jhu/icm/mage/RunStatsDirectoryServer.class
         * 
         * protocol: jar file:... till the end is path
         * 
         * jar loader used by websphere 6.0
         * wsjar:file:/Z:/My Documents/IBM/ecg/aecgWebEAR/aecg.jar!/hl7OrgV3/AnnotatedECGDocument.class
         */
        String urlString = getClassURL(me).toString();

        logger.debug(getClassURL(me).getPath() + " protocol "
                + getClassURL(me).getProtocol());

        String protocol = getClassURL(me).getProtocol()+":";
        if (urlString.startsWith(protocol)) {
            String jardir = urlString.substring(protocol.length());

            logger.debug(jardir);
            jardir = jardir.substring(0, jardir.indexOf('!'));

            logger.debug(jardir);
            jardir = jardir.substring(0, jardir.lastIndexOf('/'));

            logger.debug(jardir);
            return convert(jardir);
        }

        return "";
    }

    /**
     * get the directory for the package which contains class resides; if a
     * class is not inside a package, it will call getClassDir().
     * 
     * @param me
     *            class to search
     * @return directory for package
     */
    public static String getPkgDir(Class me) {

        Package pkg = me.getPackage();
        if (pkg == null)
            return getClassDir(me);

        String pkgname = pkg.getName();
        if (pkgname != null && pkgname.length() > 0) {
            pkgname = replaceAll(pkgname, ".", "/");
            logger.debug("package name: " + pkgname);
        }
        // Open a URL to the our .class file
        java.net.URL fullURL = getClassURL(me);

        logger.debug("fullURLstring: " + fullURL.toString());

        String classDir = fullURL.toString();

        classDir = classDir.substring(0, classDir.lastIndexOf(pkgname) - 1);

        logger.debug("classDir " + classDir);
        return convert(classDir);
    }

    public static String replaceAll(String string, String oldsubstr,
            String newsubstr) {
        if (string == null || oldsubstr == null || newsubstr == null
                || oldsubstr.equalsIgnoreCase(newsubstr)) {
            return string;
        }

        int index = 0;
        while ((index = string.indexOf(oldsubstr)) != -1) {
            string = string.substring(0, index) + newsubstr
                    + string.substring(index + oldsubstr.length());
        }
        return string;
    }

    /**
     * get directory where the class is in
     * 
     * @param me
     *            class should NOT be in a jar
     * @return local file directory
     */
    public static String getClassDir(Class me) {

        // Open a URL to the our .class file
        java.net.URL fullURL = getClassURL(me);
    
            logger.debug("fullURLstring: " + fullURL.toString());

        String classDir = fullURL.toString();

        classDir = classDir.substring(0, classDir.lastIndexOf("/"));

   
            logger.debug("classdir: " + classDir);
        return convert(classDir);
    }

    /**
     * Returns the disk file name of the class that is executing.
     * 
     * @param me
     * @return Name of class that is currently executing
     */
    private static String getClassName(Class me) {
        //  package.classname
        String fullName = me.getName();

        // just classname
        String className = fullName.substring(fullName.lastIndexOf(".") + 1,
                fullName.length());
        className += ".class";

        return className;
    }

    /**
     * get URL for Class
     * 
     * @param me
     *            class name
     * @return URL for class
     */
    private static URL getClassURL(Class me) {

        return me.getResource(getClassName(me));
    }

    /**
     * convert a url string to a local file system path
     * 
     * @param url
     * @return
     */
    private static String convert(String url) {
      
         logger.debug("url is " + url);
        //return "convert()";
        /*
         * replace %20 etc; then file:/ then use file.getPath
         */
        url = replaceAll(url, "%20", " ");
        url = url.substring(new String("file:/").length());
        url = replaceAll(url, "/", File.separator);
        if (url.indexOf(':') == -1) {
            System.out.println("adding /... ");
            url = "/" + url;
        }
        return url;
        /*
         * this will not run in java 1.3 try { URI uri = new URI(url); if
         * (verbose) System.out.println("convert(), uri is " + uri.toString());
         * File file = new File(uri); return file.getPath(); } catch
         * (URISyntaxException e) { if (verbose) System.out.println("convert(), " +
         * e.getMessage()); return ""; }
         */
    }

    /**
     * Given a Class object, attempts to find its .class location [returns null
     * if no such definition can be found]. Use for testing/debugging only.
     * 
     * @return URL that points to the class definition [null if not found].
     * @author Vladimir Roubtsov
     */
    public static URL getClassLocation(final Class cls) {
        if (cls == null)
            throw new IllegalArgumentException("null input: cls");

        URL result = null;
        final String clsAsResource = cls.getName().replace('.', '/').concat(
                ".class");

        final ProtectionDomain pd = cls.getProtectionDomain();
        // java.lang.Class contract does not specify if 'pd' can ever be null;
        // it is not the case for Sun's implementations, but guard against null
        // just in case:
        if (pd != null) {
            final CodeSource cs = pd.getCodeSource();
            // 'cs' can be null depending on the classloader behavior:
            if (cs != null)
                result = cs.getLocation();

            if (result != null) {
                // Convert a code source location into a full class file
                // location
                // for some common cases:
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar")
                                || result.toExternalForm().endsWith(".zip"))
                            result = new URL("jar:".concat(
                                    result.toExternalForm()).concat("!/")
                                    .concat(clsAsResource));
                        else if (new File(result.getFile()).isDirectory())
                            result = new URL(result, clsAsResource);
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }

        if (result == null) {
            // Try to find 'cls' definition as a resource; this is not
            // documented to be legal, but Sun's implementations seem to allow
            // this:
            final ClassLoader clsLoader = cls.getClassLoader();

            result = clsLoader != null ? clsLoader.getResource(clsAsResource)
                    : ClassLoader.getSystemResource(clsAsResource);
        }

        return result;
    }
}
