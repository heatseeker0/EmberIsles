package us.embercraft.emberisles.util;

public class RandomStringGenerator {
    public static enum Mode {
        ALPHA("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        ALPHANUMERIC("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"),
        NUMERIC("1234567890");

        Mode(final String charSet) {
            this.charSet = charSet;
        }

        public String getCharSet() {
            return charSet;
        }

        final private String charSet;
    }

    public static String generateRandomString(final int length, final Mode mode) {
        final String charSet = mode.getCharSet();
        final int charactersLength = charSet.length();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(charSet.charAt((int) (Math.random() * charactersLength)));
        }
        return buffer.toString();
    }
}
