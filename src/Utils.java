/*
 * 	Utilitary functions
 *
 * */
public class Utils {


    public static boolean ErrorIndexIsValid(String errorIndexStr) {
        try {

            int errorIndex = Integer.parseInt(errorIndexStr);

            return (errorIndex >= 0  && errorIndex < 1000000);
        }
        catch(Exception ex) {
            return false;
        }
    }



}
